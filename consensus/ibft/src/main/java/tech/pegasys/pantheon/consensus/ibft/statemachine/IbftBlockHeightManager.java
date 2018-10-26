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
import tech.pegasys.pantheon.consensus.ibft.RoundTimer;
import tech.pegasys.pantheon.consensus.ibft.ibftevent.RoundExpiry;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.CommitPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.NewRoundPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for starting/stopping Consensus rounds at a given block height. One of these is
 * created when a new block is imported to the chain. It immediately then creates a Round-0 object,
 * and sends a Preprepare message. If the round times out prior to importing a block, this class is
 * responsible for creating a RoundChange message and transmitting it.
 */
public class IbftBlockHeightManager {

  private static final Logger LOG = LogManager.getLogger();

  private final IbftRoundFactory roundFactory;
  // private final IbftRoundChangeManager roundChangeManager;
  private final BlockHeader parentHeader;
  private final RoundTimer roundTimer;
  private final BlockTimer blockTimer;

  private IbftRound currentRound;

  public IbftBlockHeightManager(
      final BlockHeader parentHeader,
      final IbftImmutableState immutableState,
      // final IbftRoundChangeManager roundChangeManager,
      final IbftRoundFactory ibftRoundFactory) {
    this.parentHeader = parentHeader;
    this.roundFactory = ibftRoundFactory;
    this.roundTimer = immutableState.getRoundTimer();
    this.blockTimer = immutableState.getBlockTimer();
    this.currentRound = roundFactory.createNewRound(parentHeader, 0);
    // this.roundChangeManager = roundChangeManager;
  }

  public void start() {
    roundTimer.startTimer(currentRound.getRoundIdentifier());
  }

  public void roundExpired(final RoundExpiry expire) {
    LOG.debug("Round has expired, creating PrepareCertificate and notifying peers.");
    final Optional<PreparedCertificate> prepareCertificate =
        currentRound.createPrepareCertificate();

    currentRound =
        roundFactory.createNewRound(
            parentHeader, currentRound.getRoundIdentifier().getRoundNumber() + 1);
  }

  public void handlePreprepareMessage(final SignedData<ProposalPayload> msg) {
    LOG.error("Received a PRE-prepare message.");
    currentRound.handlePreprepareMessage(msg);
  }

  public void handlePrepareMessage(final SignedData<PreparePayload> msg) {
    // if (isValidPrepareMessage(msg)) {}
    LOG.error("Received a prepare message.");
    currentRound.handlePrepareMessage(msg);
  }

  public void handleCommitMessage(final SignedData<CommitPayload> msg) {
    LOG.error("Received a commit message.");
    currentRound.handleCommitMessage(msg);
  }

  public void handleBlockTimerExpiry() {}

  public void handleRoundChangeMessage(final SignedData<RoundChangePayload> msg) {
    /*
    final Optional<RoundChangeResult> result = roundChangeManager.appendRoundChangeMessage(msg);
    if (result.isPresent()) {
      // If here, sufficient RoundChange messages have been received to trigger a new round
      // to be created.
    }
    */
  }

  public void handleNewRoundMessage(final SignedData<NewRoundPayload> msg) {}

  public long getChainHeight() {
    return currentRound.getRoundIdentifier().getSequenceNumber();
  }
}
