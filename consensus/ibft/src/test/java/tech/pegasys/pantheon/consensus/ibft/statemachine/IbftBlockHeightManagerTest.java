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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.consensus.common.VoteTally;
import tech.pegasys.pantheon.consensus.ibft.BlockTimer;
import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibft.RoundTimer;
import tech.pegasys.pantheon.consensus.ibft.blockcreation.IbftBlockCreator;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.validation.MessageValidatorFactory;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.BlockImporter;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.util.Subscribers;

import java.time.Clock;
import java.util.List;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IbftBlockHeightManagerTest {

  private KeyPair localNodeKeys = KeyPair.generate();
  private Address localAddress = Util.publicKeyToAddress(localNodeKeys.getPublicKey());
  private List<Address> validators = Lists.newArrayList();
  private final MessageFactory messageFactory = new MessageFactory(localNodeKeys);

  private final BlockHeaderTestFixture headerTestFixture = new BlockHeaderTestFixture();
  @Mock private IbftFinalState finalState;
  @Mock private IbftMessageTransmitter messageTransmitter;
  @Mock private RoundChangeManager roundChangeManager;
  @Mock private IbftRoundFactory roundFactory;
  @Mock private Clock clock;
  @Mock private MessageValidatorFactory messageValidatorFactory;
  @Mock private IbftBlockCreator blockCreator;
  @Mock private Block block;
  @Mock private BlockImporter<IbftContext> blockImporter;
  @Mock private RoundState roundState;
  @Mock private BlockTimer blockTimer;
  @Mock private RoundTimer roundTimer;

  private ProtocolContext<IbftContext> protocolContext;
  private ConsensusRoundIdentifier roundIdentifier = new ConsensusRoundIdentifier(1, 0);

  @Before
  public void setup() {
    // when(finalState.getValidators()).thenReturn(validators);
    // when(finalState.getNodeKeys()).thenReturn(localNodeKeys);
    // when(finalState.getLocalAddress()).thenReturn(localAddress);
    when(finalState.getTransmitter()).thenReturn(messageTransmitter);
    when(finalState.getBlockTimer()).thenReturn(blockTimer);
    when(finalState.getRoundTimer()).thenReturn(roundTimer);
    when(blockCreator.createBlock(anyLong())).thenReturn(block);
    when(roundState.getRoundIdentifier()).thenReturn(roundIdentifier);

    protocolContext =
        new ProtocolContext<>(null, null, new IbftContext(new VoteTally(validators), null));

    when(roundFactory.createNewRound(any(), anyInt()))
        .thenReturn(
            new IbftRound(
                roundState,
                blockCreator,
                protocolContext,
                blockImporter,
                new Subscribers<>(),
                localNodeKeys,
                null,
                messageTransmitter));
  }

  @Test
  public void atCreationIfLocalNodeIsProposerBlockTimeIsStarted() {
    when(finalState.isLocalNodeProposerForRound(any())).thenReturn(true);

    final IbftBlockHeightManager manager =
        new IbftBlockHeightManager(
            headerTestFixture.buildHeader(),
            finalState,
            roundChangeManager,
            roundFactory,
            clock,
            messageValidatorFactory);

    verify(blockTimer, times(1)).startTimer(any(), any());
  }

  @Test
  public void onBlockTimerExpiryProposalMessageIsTransmitted() {
    final IbftBlockHeightManager manager =
        new IbftBlockHeightManager(
            headerTestFixture.buildHeader(),
            finalState,
            roundChangeManager,
            roundFactory,
            clock,
            messageValidatorFactory);

    manager.handleBlockTimerExpiry(roundIdentifier);
    verify(messageTransmitter, times(1)).multicastProposal(eq(roundIdentifier), any());
    verify(messageTransmitter, never()).multicastPrepare(any(), any());
    verify(messageTransmitter, never()).multicastPrepare(any(), any());
  }

  @Test
  public void onRoundChangeReceptionROundChangeManagerisInvoked() {
    final IbftBlockHeightManager manager =
        new IbftBlockHeightManager(
            headerTestFixture.buildHeader(),
            finalState,
            roundChangeManager,
            roundFactory,
            clock,
            messageValidatorFactory);

    //    final SignedData<RoundChangePayload> roundChangePayload =
    //        messageFactory.createSignedRoundChangePayload()
    //
    //    manager.handleRoundChangeMessage();

  }

  @Test
  public void onRoundTimerExpiryANewRoundIsCreatedWithAnIncrementedRoundNumber() {}

  @Test
  public void whenSufficientRoundChangesAreReceivedANewRoundMessageIsTransmitted() {}

  @Test
  public void messagesForFutureRoundsAreBufferedAndUsedToPreloadNewRoundWhenItIsStarted() {}
}
