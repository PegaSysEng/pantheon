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

import static java.util.Optional.empty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.pegasys.pantheon.consensus.ibft.support.MessageReceptionHelpers.assertPeersReceivedExactly;
import static tech.pegasys.pantheon.consensus.ibft.support.MessageReceptionHelpers.assertPeersReceivedNoMessages;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.ibftevent.RoundExpiry;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.NewRoundPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangeCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.consensus.ibft.support.RoundSpecificNodeRoles;
import tech.pegasys.pantheon.consensus.ibft.support.TestContext;
import tech.pegasys.pantheon.consensus.ibft.support.TestContextFactory;
import tech.pegasys.pantheon.consensus.ibft.support.ValidatorPeer;
import tech.pegasys.pantheon.ethereum.core.Block;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

public class RoundChangeTest {

  private final long blockTimeStamp = 100;
  private final Clock mockBlockCreationTime = mock(Clock.class);

  // This ensures the localNode (i.e. the UUT) will not create the first block
  private final TestContext context =
      TestContextFactory.createTestEnvironment(4, 0, mockBlockCreationTime);
  private final ConsensusRoundIdentifier roundId = new ConsensusRoundIdentifier(1, 0);
  private final RoundSpecificNodeRoles roles = context.getRoundSpecificRoles(roundId);

  private final MessageFactory localNodeMessageFactory = context.getLocalNodeMessageFactory();

  private final Block blockToPropose = context.createBlockForProposal(0, 15);

  @Before
  public void setup() {
    context.getController().start();
  }

  @Test
  public void onRoundChangeTimerExpiryEventRoundChangeMessageIsSent() {

    // NOTE: The prepare certificate will be empty as insufficient Prepare msgs have been received.
    final ConsensusRoundIdentifier targetRound = new ConsensusRoundIdentifier(1, 1);
    final SignedData<RoundChangePayload> expectedTxRoundChange =
        localNodeMessageFactory.createSignedRoundChangePayload(targetRound, empty());
    context.getController().handleRoundExpiry(new RoundExpiry(roundId));
    assertPeersReceivedExactly(roles.getAllPeers(), expectedTxRoundChange);
  }

  @Test
  public void roundChangeHasEmptyCertificateIfNoPrepareMessagesReceived() {
    final ConsensusRoundIdentifier targetRound = new ConsensusRoundIdentifier(1, 1);
    final SignedData<RoundChangePayload> expectedTxRoundChange =
        localNodeMessageFactory.createSignedRoundChangePayload(targetRound, empty());

    roles.getProposer().injectProposal(roundId, blockToPropose);
    roles.getAllPeers().forEach(ValidatorPeer::clearReceivedMessages);

    context.getController().handleRoundExpiry(new RoundExpiry(roundId));
    assertPeersReceivedExactly(roles.getAllPeers(), expectedTxRoundChange);
  }

  @Test
  public void roundChangeHasPopulatedCertificateIfQuorumPrepareMessagesAreReceived() {
    final ConsensusRoundIdentifier targetRound = new ConsensusRoundIdentifier(1, 1);
    final SignedData<PreparePayload> localPrepareMessage =
        localNodeMessageFactory.createSignedPreparePayload(roundId, blockToPropose.getHash());

    final SignedData<ProposalPayload> proposal =
        roles.getProposer().injectProposal(roundId, blockToPropose);
    roles.getAllPeers().forEach(ValidatorPeer::clearReceivedMessages);

    final SignedData<PreparePayload> prepare =
        roles.getNonProposingPeers().get(0).injectPrepare(roundId, blockToPropose.getHash());
    roles.getAllPeers().forEach(ValidatorPeer::clearReceivedMessages);

    final SignedData<RoundChangePayload> expectedTxRoundChange =
        localNodeMessageFactory.createSignedRoundChangePayload(
            targetRound,
            Optional.of(
                new PreparedCertificate(
                    proposal, Lists.newArrayList(localPrepareMessage, prepare))));

    context.getController().handleRoundExpiry(new RoundExpiry(roundId));
    assertPeersReceivedExactly(roles.getAllPeers(), expectedTxRoundChange);
  }

  @Test
  public void whenSufficientRoundChangeMessagesAreReceivedForNewRoundLocalNodeCreatesNewRoundMsg() {
    // Note: Round-3 is the next round for which the local node is Proposer
    final ConsensusRoundIdentifier targetRound = new ConsensusRoundIdentifier(1, 3);
    when(mockBlockCreationTime.millis()).thenReturn(blockTimeStamp * 1000);
    final Block locallyProposedBlock =
        context.createBlockForProposal(targetRound.getRoundNumber(), blockTimeStamp);

    final SignedData<RoundChangePayload> rc1 =
        roles.getNonProposingPeers().get(0).injectRoundChange(targetRound, empty());
    final SignedData<RoundChangePayload> rc2 =
        roles.getNonProposingPeers().get(1).injectRoundChange(targetRound, empty());
    final SignedData<RoundChangePayload> rc3 =
        roles.getProposer().injectRoundChange(targetRound, empty());

    final SignedData<NewRoundPayload> expectedNewRound =
        localNodeMessageFactory.createSignedNewRoundPayload(
            targetRound,
            new RoundChangeCertificate(Lists.newArrayList(rc1, rc2, rc3)),
            localNodeMessageFactory.createSignedProposalPayload(targetRound, locallyProposedBlock));

    assertPeersReceivedExactly(roles.getAllPeers(), expectedNewRound);
  }

  @Test
  public void newRoundMessageContainsBlockOnWhichPeerPrepared() {
    final long ARBITRATY_BLOCKTIME = 1500;

    final ConsensusRoundIdentifier earlierPreparedRound = new ConsensusRoundIdentifier(1, 1);
    final ConsensusRoundIdentifier bestPreparedRound = new ConsensusRoundIdentifier(1, 2);
    final ConsensusRoundIdentifier targetRound = new ConsensusRoundIdentifier(1, 3);

    final Block earlierPreparedBlock = context.createBlockForProposal(1, ARBITRATY_BLOCKTIME / 2);
    final Block bestPreparedBlock = context.createBlockForProposal(2, ARBITRATY_BLOCKTIME);

    final SignedData<RoundChangePayload> rc1 =
        roles.getNonProposingPeers().get(0).injectRoundChange(targetRound, empty());

    // Create a roundChange with a PreparedCertificate from an earlier Round (should not be used
    final SignedData<RoundChangePayload> rc2 =
        roles
            .getNonProposingPeers()
            .get(1)
            .injectRoundChange(
                targetRound,
                Optional.of(
                    new PreparedCertificate(
                        roles
                            .getNonProposingPeers()
                            .get(0)
                            .getMessageFactory()
                            .createSignedProposalPayload(
                                earlierPreparedRound, earlierPreparedBlock),
                        Lists.newArrayList(
                            roles
                                .getProposer()
                                .getMessageFactory()
                                .createSignedPreparePayload(
                                    earlierPreparedRound, earlierPreparedBlock.getHash()),
                            roles
                                .getNonProposingPeers()
                                .get(1)
                                .getMessageFactory()
                                .createSignedPreparePayload(
                                    earlierPreparedRound, earlierPreparedBlock.getHash())))));

    // Create a roundChange containing a PreparedCertificate
    final SignedData<RoundChangePayload> rc3 =
        roles
            .getProposer()
            .injectRoundChange(
                targetRound,
                Optional.of(
                    new PreparedCertificate(
                        roles
                            .getNonProposingPeers()
                            .get(1)
                            .getMessageFactory()
                            .createSignedProposalPayload(bestPreparedRound, bestPreparedBlock),
                        Lists.newArrayList(
                            roles
                                .getProposer()
                                .getMessageFactory() // note: CANNOT be nonProposer(1)!
                                .createSignedPreparePayload(
                                    bestPreparedRound, bestPreparedBlock.getHash()),
                            roles
                                .getNonProposingPeers()
                                .get(0)
                                .getMessageFactory()
                                .createSignedPreparePayload(
                                    bestPreparedRound, bestPreparedBlock.getHash())))));

    // Expected to use the block with "ARBITRARY_BLOCKTIME" (i.e. latter block) but with the target
    // round number.
    final Block expectedBlockToPropose =
        context.createBlockForProposal(targetRound.getRoundNumber(), ARBITRATY_BLOCKTIME);

    final SignedData<NewRoundPayload> expectedNewRound =
        localNodeMessageFactory.createSignedNewRoundPayload(
            targetRound,
            new RoundChangeCertificate(Lists.newArrayList(rc1, rc2, rc3)),
            localNodeMessageFactory.createSignedProposalPayload(
                targetRound, expectedBlockToPropose));

    assertPeersReceivedExactly(roles.getAllPeers(), expectedNewRound);
  }

  @Test
  public void cannotRoundChangeToAnEarlierRound() {
    // Controller always starts at 1:0. This test moves to 1:7, then attempts to move back to 1:3.
    when(mockBlockCreationTime.millis()).thenReturn(blockTimeStamp * 1000);

    final ConsensusRoundIdentifier futureRound = new ConsensusRoundIdentifier(1, 7);
    final List<SignedData<RoundChangePayload>> roundChangeMessages = Lists.newArrayList();
    for (final ValidatorPeer peer : roles.getAllPeers()) {
      roundChangeMessages.add(peer.injectRoundChange(futureRound, empty()));
    }

    final ConsensusRoundIdentifier priorRound = new ConsensusRoundIdentifier(1, 3);
    for (final ValidatorPeer peer : roles.getAllPeers()) {
      peer.injectRoundChange(priorRound, empty());
    }

    final Block locallyProposedBlock =
        context.createBlockForProposal(futureRound.getRoundNumber(), blockTimeStamp);

    final SignedData<NewRoundPayload> expectedNewRound =
        localNodeMessageFactory.createSignedNewRoundPayload(
            futureRound,
            new RoundChangeCertificate(roundChangeMessages),
            localNodeMessageFactory.createSignedProposalPayload(futureRound, locallyProposedBlock));

    assertPeersReceivedExactly(roles.getAllPeers(), expectedNewRound);
  }

  @Test
  public void multipleRoundChangeMessagesFromSamePeerDoesNotTriggerRoundChange() {
    // Note: Round-3 is the next round for which the local node is Proposer
    final ConsensusRoundIdentifier targetRound = new ConsensusRoundIdentifier(1, 3);
    when(mockBlockCreationTime.millis()).thenReturn(blockTimeStamp * 1000);

    final ValidatorPeer transmitter = roles.getNonProposingPeers().get(0);

    final SignedData<RoundChangePayload> rc1 = transmitter.injectRoundChange(targetRound, empty());
    final SignedData<RoundChangePayload> rc2 = transmitter.injectRoundChange(targetRound, empty());
    final SignedData<RoundChangePayload> rc3 = transmitter.injectRoundChange(targetRound, empty());

    assertPeersReceivedNoMessages(roles.getAllPeers());
  }

  @Test
  public void subsequentRoundChangeMessagesFromPeerDoNotOverwritePriorMessage() {
    final long ARBITRATY_BLOCKTIME = 1500;

    final ConsensusRoundIdentifier bestPreparedRound = new ConsensusRoundIdentifier(1, 2);
    final ConsensusRoundIdentifier targetRound = new ConsensusRoundIdentifier(1, 3);
    final Block bestPreparedBlock = context.createBlockForProposal(2, ARBITRATY_BLOCKTIME);

    // Create a roundChange containing a PreparedCertificate
    final SignedData<RoundChangePayload> rc1 =
        roles
            .getProposer()
            .injectRoundChange(
                targetRound,
                Optional.of(
                    new PreparedCertificate(
                        roles
                            .getNonProposingPeers()
                            .get(1)
                            .getMessageFactory()
                            .createSignedProposalPayload(bestPreparedRound, bestPreparedBlock),
                        Lists.newArrayList(
                            roles
                                .getProposer()
                                .getMessageFactory()
                                .createSignedPreparePayload(
                                    bestPreparedRound, bestPreparedBlock.getHash()),
                            roles
                                .getNonProposingPeers()
                                .get(0)
                                .getMessageFactory()
                                .createSignedPreparePayload(
                                    bestPreparedRound, bestPreparedBlock.getHash())))));

    // Attempt to override the previously received RoundChange (but now without a payload).
    roles.getProposer().injectRoundChange(targetRound, empty());

    final SignedData<RoundChangePayload> rc2 =
        roles.getNonProposingPeers().get(0).injectRoundChange(targetRound, empty());
    final SignedData<RoundChangePayload> rc3 =
        roles.getNonProposingPeers().get(1).injectRoundChange(targetRound, empty());

    final Block expectedBlockToPropose =
        context.createBlockForProposal(targetRound.getRoundNumber(), ARBITRATY_BLOCKTIME);

    final SignedData<NewRoundPayload> expectedNewRound =
        localNodeMessageFactory.createSignedNewRoundPayload(
            targetRound,
            new RoundChangeCertificate(Lists.newArrayList(rc1, rc2, rc3)),
            localNodeMessageFactory.createSignedProposalPayload(
                targetRound, expectedBlockToPropose));

    assertPeersReceivedExactly(roles.getAllPeers(), expectedNewRound);
  }

  @Test
  public void messagesFromPreviousRoundAreDiscardedOnTransitionToFutureRound() {}
}
