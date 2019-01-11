/*
 * Copyright 2019 ConsenSys AG.
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
package tech.pegasys.pantheon.consensus.ibft.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static tech.pegasys.pantheon.consensus.ibft.support.MessageReceptionHelpers.assertPeersReceivedExactly;
import static tech.pegasys.pantheon.consensus.ibft.support.MessageReceptionHelpers.assertPeersReceivedNoMessages;
import static tech.pegasys.pantheon.consensus.ibft.support.TestHelpers.injectEmptyNewRound;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;
import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.CommitPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.consensus.ibft.support.RoundSpecificNodeRoles;
import tech.pegasys.pantheon.consensus.ibft.support.TestContext;
import tech.pegasys.pantheon.consensus.ibft.support.TestContextFactory;
import tech.pegasys.pantheon.consensus.ibft.support.ValidatorPeer;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.ethereum.core.Block;

public class FutureRoundTest {

  final long blockTimeStamp = 100;
  private final Clock fixedClock =
      Clock.fixed(Instant.ofEpochSecond(blockTimeStamp), ZoneId.systemDefault());

  // Local node will propose the first block
  private final TestContext context =
      TestContextFactory.createTestEnvironment(4, 0, fixedClock);

  private final ConsensusRoundIdentifier roundId = new ConsensusRoundIdentifier(1, 0);
  private final RoundSpecificNodeRoles roles = context.getRoundSpecificRoles(roundId);

  private final ConsensusRoundIdentifier futureRoundId = new ConsensusRoundIdentifier(1, 4);
  private final RoundSpecificNodeRoles futureRoles = context.getRoundSpecificRoles(futureRoundId);

  private final MessageFactory localNodeMessageFactory = context.getLocalNodeMessageFactory();

  @Before
  public void setup() {
    context.getController().start();
  }

  @Test
  public void messagesForFutureRoundAreNotActionedUntilRoundIsActive() {
    final Block futureBlock = context.createBlockForProposal(futureRoundId.getRoundNumber(), 60);
    for (final ValidatorPeer peer : futureRoles.getNonProposingPeers()) {
      peer.injectPrepare(futureRoundId, futureBlock.getHash());
      peer.injectCommit(futureRoundId, futureBlock.getHash());
    }
    assertPeersReceivedNoMessages(roles.getAllPeers());
    assertThat(context.getBlockchain().getChainHeadBlockNumber()).isEqualTo(0);

    // inject a newRound to move to 'futureRoundId', and ensure localnode sends prepare, commit
    // and updates blockchain
    injectEmptyNewRound(futureRoundId, futureRoles.getProposer(), futureRoles.getAllPeers(),
        futureBlock);

    final SignedData<PreparePayload> expectedPrepare =
        localNodeMessageFactory.createSignedPreparePayload(futureRoundId, futureBlock.getHash());
    final SignedData<CommitPayload> expectedCommit =
        localNodeMessageFactory.createSignedCommitPayload(futureRoundId, futureBlock.getHash(),
            SECP256K1.sign(futureBlock.getHash(), context.getLocalNodeParams().getNodeKeyPair()));
    assertPeersReceivedExactly(futureRoles.getAllPeers(), expectedCommit, expectedPrepare);
    assertThat(context.getBlockchain().getChainHeadBlockNumber()).isEqualTo(1);
  }

  @Test
  public void priorRoundsCannotBeCompletedAfterReceptionOfNewRound() {
    final Block initialBlock = context.createBlockForProposal(roundId.getRoundNumber(), 30);
    final Block futureBlock = context.createBlockForProposal(futureRoundId.getRoundNumber(), 60);

    roles.getProposer().injectProposal(roundId, initialBlock);
    for (final ValidatorPeer peer : roles.getNonProposingPeers()) {
      peer.injectPrepare(roundId, initialBlock.getHash());
    }
    roles.getProposer().injectCommit(roundId, initialBlock.getHash());
    //At this stage, the local node has 2 commit msgs (proposer and local) so has not committed
    assertThat(context.getBlockchain().getChainHeadBlockNumber()).isEqualTo(0);

    for (final ValidatorPeer peer : roles.getAllPeers()) {
      peer.clearReceivedMessages();
    }

    injectEmptyNewRound(futureRoundId, futureRoles.getProposer(), futureRoles.getAllPeers(),
        futureBlock);

    final SignedData<PreparePayload> expectedFuturePrepare =
        localNodeMessageFactory.createSignedPreparePayload(futureRoundId, futureBlock.getHash());
    assertPeersReceivedExactly(roles.getAllPeers(), expectedFuturePrepare);


    // attempt to complete the previous round
    roles.getNonProposingPeers().get(0).injectCommit(roundId,  initialBlock.getHash());
    assertPeersReceivedNoMessages(roles.getAllPeers());
    assertThat(context.getBlockchain().getChainHeadBlockNumber()).isEqualTo(0);

  }
}
