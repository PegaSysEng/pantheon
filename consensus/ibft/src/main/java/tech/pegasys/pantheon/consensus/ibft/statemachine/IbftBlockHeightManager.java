/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.consensus.ibft.statemachine;

import tech.pegasys.pantheon.consensus.ibft.BlockTimer;
import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.RoundTimer;
import tech.pegasys.pantheon.consensus.ibft.ibftevent.RoundExpiry;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.CommitPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.NewRoundPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangeCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.consensus.ibft.validation.MessageValidatorFactory;
import tech.pegasys.pantheon.consensus.ibft.validation.NewRoundMessageValidator;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for starting/clearing Consensus rounds at a given block height. One of these is
 * created when a new block is imported to the chain. It immediately then creates a Round-0 object,
 * and sends a Proposal message. If the round times out prior to importing a block, this class is
 * responsible for creating a RoundChange message and transmitting it.
 */
public class IbftBlockHeightManager {

  private static final Logger LOG = LogManager.getLogger();

  private final IbftRoundFactory roundFactory;
  private final RoundChangeManager roundChangeManager;
  private final BlockHeader parentHeader;
  private final RoundTimer roundTimer;
  private final BlockTimer blockTimer;
  private final IbftMessageTransmitter transmitter;
  private final Map<Integer, RoundState> futureRoundStateBuffer = Maps.newHashMap();
  private final NewRoundMessageValidator newRoundMessageValidator;
  private final Clock clock;
  private final Function<ConsensusRoundIdentifier, RoundState> roundStateCreator;
  private final IbftFinalState finalState;

  private Optional<PreparedCertificate> bestPreparedCertificate = Optional.empty();

  private IbftRound currentRound;

  public IbftBlockHeightManager(
      final BlockHeader parentHeader,
      final IbftFinalState finalState,
      final RoundChangeManager roundChangeManager,
      final IbftRoundFactory ibftRoundFactory,
      final Clock clock,
      final MessageValidatorFactory messageValidatorFactory) {
    this.parentHeader = parentHeader;
    this.roundFactory = ibftRoundFactory;
    this.roundTimer = finalState.getRoundTimer();
    this.blockTimer = finalState.getBlockTimer();
    this.transmitter = finalState.getTransmitter();
    this.clock = clock;
    this.roundChangeManager = roundChangeManager;
    this.finalState = finalState;

    newRoundMessageValidator = messageValidatorFactory.createNewRoundValidator(parentHeader);

    roundStateCreator =
        (roundIdentifier) ->
            new RoundState(
                roundIdentifier,
                finalState.getQuorumSize(),
                messageValidatorFactory.createMessageValidator(roundIdentifier, parentHeader));
  }

  public void start() {
    startNewRound(0);
    if (finalState.isLocalNodeProposerForRound(currentRound.getRoundIdentifier())) {
      blockTimer.startTimer(currentRound.getRoundIdentifier(), parentHeader);
    }

  }

  public void roundExpired(final RoundExpiry expire) {
    if (!expire.getView().equals(currentRound.getRoundIdentifier())) {
      LOG.debug("Ignoring Round timer expired which does not match current round.");
      return;
    }

    LOG.debug("Round has expired, creating PreparedCertificate and notifying peers.");
    final Optional<PreparedCertificate> preparedCertificate =
        currentRound.createPrepareCertificate();

    if (preparedCertificate.isPresent()) {
      bestPreparedCertificate = preparedCertificate;
    }

    // discard roundchange messages from finished round
    roundChangeManager.discardCompletedRound(currentRound.getRoundIdentifier());

    // Start a new round, and notify peers.
    startNewRound(currentRound.getRoundIdentifier().getRoundNumber() + 1);

    // TODO(tmm): This is seems somewhat wrong, maybe the new round should not have started just yet
    final SignedData<RoundChangePayload> roundChangePayload =
        transmitter.multicastRoundChange(
            currentRound.getRoundIdentifier(), bestPreparedCertificate);
    handleRoundChangeMessage(roundChangePayload);
  }

  public void handleProposalMessage(final SignedData<ProposalPayload> msg) {
    LOG.info("Received a Proposal message.");
    final ConsensusRoundIdentifier msgRoundId = msg.getPayload().getRoundIdentifier();
    if (msgRoundId.equals(currentRound.getRoundIdentifier())) {
      currentRound.handleProposalMessage(msg);
    } else if (msgRoundId.getRoundNumber() > currentRound.getRoundIdentifier().getRoundNumber()) {
      futureRoundStateBuffer.computeIfAbsent(
          msgRoundId.getRoundNumber(), k -> roundStateCreator.apply(msgRoundId));
      futureRoundStateBuffer.get(msgRoundId.getRoundNumber()).setProposedBlock(msg);
    }
  }

  public void handlePrepareMessage(final SignedData<PreparePayload> msg) {
    LOG.info("Received a prepare message.");
    final ConsensusRoundIdentifier msgRoundId = msg.getPayload().getRoundIdentifier();
    if (msgRoundId.equals(currentRound.getRoundIdentifier())) {
      currentRound.handlePrepareMessage(msg);
    } else if (msgRoundId.getRoundNumber() > currentRound.getRoundIdentifier().getRoundNumber()) {
      futureRoundStateBuffer.computeIfAbsent(
          msgRoundId.getRoundNumber(), k -> roundStateCreator.apply(msgRoundId));
      futureRoundStateBuffer.get(msgRoundId.getRoundNumber()).addPrepareMessage(msg);
    }
  }

  public void handleCommitMessage(final SignedData<CommitPayload> msg) {
    LOG.info("Received a commit message.");
    final ConsensusRoundIdentifier msgRoundId = msg.getPayload().getRoundIdentifier();
    if (msgRoundId.equals(currentRound.getRoundIdentifier())) {
      currentRound.handleCommitMessage(msg);
    } else if (msgRoundId.getRoundNumber() > currentRound.getRoundIdentifier().getRoundNumber()) {
      futureRoundStateBuffer.computeIfAbsent(
          msgRoundId.getRoundNumber(), k -> roundStateCreator.apply(msgRoundId));
      futureRoundStateBuffer.get(msgRoundId.getRoundNumber()).addCommitMessage(msg);
    }
  }

  public void handleBlockTimerExpiry(final ConsensusRoundIdentifier roundIdentifier) {
    if (roundIdentifier.equals(currentRound.getRoundIdentifier())) {
      currentRound.createAndSendProposalMessage(clock.millis() / 1000);
    } else {
      LOG.info(
          "BLock timer expired for a round ({}) other than current ({})",
          roundIdentifier,
          currentRound.getRoundIdentifier());
    }
  }

  public void handleRoundChangeMessage(final SignedData<RoundChangePayload> msg) {
    final Optional<RoundChangeCertificate> result =
        roundChangeManager.appendRoundChangeMessage(msg);

    if (result.isPresent()) {
      final ConsensusRoundIdentifier targetRound = msg.getPayload().getRoundIdentifier();
      startNewRound(targetRound.getRoundNumber());

      if (finalState.isLocalNodeProposerForRound(targetRound)) {
        currentRound.startRoundWith(result.get(), clock.millis() / 1000);
      }
    }
  }

  private void startNewRound(final int roundNumber) {
    if (futureRoundStateBuffer.containsKey(roundNumber)) {
      currentRound =
          roundFactory.createNewRoundWithState(
              parentHeader, futureRoundStateBuffer.get(roundNumber));
      futureRoundStateBuffer.entrySet().removeIf(k -> k.getKey() <= roundNumber);
    } else {
      currentRound = roundFactory.createNewRound(parentHeader, roundNumber);
    }

    roundTimer.startTimer(currentRound.getRoundIdentifier());
  }

  public void handleNewRoundMessage(final SignedData<NewRoundPayload> msg) {
    if (newRoundMessageValidator.validateNewRoundMessage(msg)) {
      startNewRound(msg.getPayload().getRoundIdentifier().getRoundNumber());
      currentRound.handleProposalMessage(msg.getPayload().getProposalPayload());
    }
  }

  public long getChainHeight() {
    return currentRound.getRoundIdentifier().getSequenceNumber();
  }
}
