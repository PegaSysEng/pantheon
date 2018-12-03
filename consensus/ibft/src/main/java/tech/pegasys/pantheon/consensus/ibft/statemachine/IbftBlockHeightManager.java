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
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.RoundChangeMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.CommitPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.NewRoundPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangeCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.consensus.ibft.network.IbftNetworkPeers;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for starting/stopping Consensus rounds at a given block height. One of these is
 * created when a new block is imported to the chain. It immediately then creates a Round-0 object,
 * and sends a Proposal message. If the round times out prior to importing a block, this class is
 * responsible for creating a RoundChange message and transmitting it.
 *
 * <p>Responsible for tracking the best PreparedCertificate created by this node at this chain
 * height.
 */
public class IbftBlockHeightManager {

  private static final Logger LOG = LogManager.getLogger();

  private final IbftRoundFactory roundFactory;
  private final RoundChangeManager roundChangeManager;
  private final BlockHeader parentHeader;
  private final RoundTimer roundTimer;
  private final BlockTimer blockTimer;
  private final IbftNetworkPeers networkPeers;
  private final MessageFactory messageFactory;
  private final IbftImmutableState immutableState;

  private Optional<PreparedCertificate> bestPreparedCertificate = Optional.empty();

  private IbftRound currentRound;

  public IbftBlockHeightManager(
      final BlockHeader parentHeader,
      final IbftImmutableState immutableState,
      final RoundChangeManager roundChangeManager,
      final IbftRoundFactory ibftRoundFactory) {
    this.parentHeader = parentHeader;
    this.roundFactory = ibftRoundFactory;
    this.roundTimer = immutableState.getRoundTimer();
    this.blockTimer = immutableState.getBlockTimer();
    this.currentRound = roundFactory.createNewRound(parentHeader, 0);
    this.roundChangeManager = roundChangeManager;
    this.networkPeers = immutableState.getPeers();
    this.messageFactory = immutableState.getMessageFactory();
    this.immutableState = immutableState;

  }

  public void start() {
    final ConsensusRoundIdentifier roundIdentifier = currentRound.getRoundIdentifier();
    roundTimer.startTimer(roundIdentifier);
    if(immutableState.isLocalNodeIsProposerForRound(roundIdentifier)) {
      blockTimer.startTimer(roundIdentifier, parentHeader);
    }
  }

  public void roundExpired(final RoundExpiry expire) {
    if (expire.getView().equals(currentRound.getRoundIdentifier())) {
      LOG.debug("Ignoring Round timer expired which does not match current round.");
      return;
    }

    LOG.debug("Round has expired, creating PrepareCertificate and notifying peers.");
    final Optional<PreparedCertificate> preparedCertificate =
        currentRound.createPrepareCertificate();
    if (preparedCertificate.isPresent()) {
      bestPreparedCertificate = preparedCertificate;
    }

    // discard roundchange messages from finished round
    roundChangeManager.discardCompletedRound(currentRound.getRoundIdentifier());

    // Start a new round, and notify peers.
    currentRound =
        roundFactory.createNewRound(
            parentHeader, currentRound.getRoundIdentifier().getRoundNumber() + 1);

    // TODO(tmm): This should probably be moved behind a facade which wraps the msgs and everything
    // i.e. notifyPeersOfRoundChange(roundId, preparedCert)
    final SignedData<RoundChangePayload> signedPayload =
        messageFactory.createSignedRoundChangePayload(
            currentRound.getRoundIdentifier(), bestPreparedCertificate);
    final RoundChangeMessage msg = RoundChangeMessage.create(signedPayload);
    networkPeers.multicastToValidators(msg);
  }

  public void handleProposalMessage(final SignedData<ProposalPayload> msg) {
    LOG.info("Received a Proposal message.");
    currentRound.handleProposalMessage(msg);
  }

  public void handlePrepareMessage(final SignedData<PreparePayload> msg) {
    // what if this arrives before the proposal? Probably want to be careful!
    LOG.info("Received a prepare message.");
    currentRound.handlePrepareMessage(msg);
  }

  public void handleCommitMessage(final SignedData<CommitPayload> msg) {
    // what if this arrives before the proposal? Probably want to be careful!
    LOG.info("Received a commit message.");
    currentRound.handleCommitMessage(msg);
  }

  public void handleBlockTimerExpiry() {
    currentRound.createAndSendProposalMessage();
  }

  public void handleRoundChangeMessage(final SignedData<RoundChangePayload> msg) {
    final Optional<RoundChangeCertificate> result =
        roundChangeManager.appendRoundChangeMessage(msg);
    if (result.isPresent()) {

      // If here, sufficient RoundChange messages have been received to trigger a new round
      // to be created.
    }
  }

  public void handleNewRoundMessage(final SignedData<NewRoundPayload> msg) {}

  public long getChainHeight() {
    return currentRound.getRoundIdentifier().getSequenceNumber();
  }
}
