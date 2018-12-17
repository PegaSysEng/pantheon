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

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.pegasys.pantheon.consensus.ibft.TestHelpers.createFrom;

import tech.pegasys.pantheon.consensus.common.VoteTally;
import tech.pegasys.pantheon.consensus.ibft.BlockTimer;
import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibft.RoundTimer;
import tech.pegasys.pantheon.consensus.ibft.blockcreation.IbftBlockCreator;
import tech.pegasys.pantheon.consensus.ibft.ibftevent.RoundExpiry;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangeCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
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
import java.util.Optional;

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
    when(finalState.getMessageFactory()).thenReturn(messageFactory);
    when(blockCreator.createBlock(anyLong())).thenReturn(block);
    when(roundState.getRoundIdentifier()).thenReturn(roundIdentifier);

    protocolContext =
        new ProtocolContext<>(null, null, new IbftContext(new VoteTally(validators), null));

    // Ensure the created IbftRound has the valid ConsensusRoundIdentifier;
    when(roundFactory.createNewRound(any(), anyInt()))
        .thenAnswer(
            invocation -> {
              final int round = (int) invocation.getArgument(1);
              final ConsensusRoundIdentifier roundId = new ConsensusRoundIdentifier(1, round);
              when(roundState.getRoundIdentifier()).thenReturn(roundId);
              return new IbftRound(
                  roundState,
                  blockCreator,
                  protocolContext,
                  blockImporter,
                  new Subscribers<>(),
                  localNodeKeys,
                  messageFactory,
                  messageTransmitter);
            });
  }

  @Test
  public void startsABlockTimerOnStartIfLocalNodeIsTheProoserForRound() {
    when(finalState.isLocalNodeProposerForRound(any())).thenReturn(true);

    final IbftBlockHeightManager manager =
        new IbftBlockHeightManager(
            headerTestFixture.buildHeader(),
            finalState,
            roundChangeManager,
            roundFactory,
            clock,
            messageValidatorFactory);
    manager.start();

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
    manager.start();

    manager.handleBlockTimerExpiry(roundIdentifier);
    verify(messageTransmitter, times(1)).multicastProposal(eq(roundIdentifier), any());
    verify(messageTransmitter, never()).multicastPrepare(any(), any());
    verify(messageTransmitter, never()).multicastPrepare(any(), any());
  }

  @Test
  public void onRoundChangeReceptionRoundChangeManagerIsInvokedAndNewRoundStarted() {
    final ConsensusRoundIdentifier futureRoundIdentifier = createFrom(roundIdentifier, 0, +2);
    final SignedData<RoundChangePayload> roundChangePayload =
        messageFactory.createSignedRoundChangePayload(futureRoundIdentifier, Optional.empty());
    when(roundChangeManager.appendRoundChangeMessage(any()))
        .thenReturn(Optional.of(new RoundChangeCertificate(singletonList(roundChangePayload))));
    when(finalState.isLocalNodeProposerForRound(any())).thenReturn(false);

    final IbftBlockHeightManager manager =
        new IbftBlockHeightManager(
            headerTestFixture.buildHeader(),
            finalState,
            roundChangeManager,
            roundFactory,
            clock,
            messageValidatorFactory);
    manager.start();
    verify(roundFactory).createNewRound(any(), eq(0));

    manager.handleRoundChangeMessage(roundChangePayload);

    verify(roundChangeManager, times(1)).appendRoundChangeMessage(roundChangePayload);
    verify(roundFactory, times(1))
        .createNewRound(any(), eq(futureRoundIdentifier.getRoundNumber()));
  }

  @Test
  public void onRoundTimerExpiryANewRoundIsCreatedWithAnIncrementedRoundNumber() {
    final IbftBlockHeightManager manager =
        new IbftBlockHeightManager(
            headerTestFixture.buildHeader(),
            finalState,
            roundChangeManager,
            roundFactory,
            clock,
            messageValidatorFactory);
    manager.start();
    verify(roundFactory).createNewRound(any(), eq(0));

    manager.roundExpired(new RoundExpiry(roundIdentifier));
    verify(roundFactory).createNewRound(any(), eq(1));
  }

  @Test
  public void whenSufficientRoundChangesAreReceivedANewRoundMessageIsTransmitted() {
    final ConsensusRoundIdentifier futureRoundIdentifier = createFrom(roundIdentifier, 0, +2);
    final SignedData<RoundChangePayload> roundChangePayload =
        messageFactory.createSignedRoundChangePayload(futureRoundIdentifier, Optional.empty());
    final RoundChangeCertificate roundChangCert =
        new RoundChangeCertificate(singletonList(roundChangePayload));

    when(roundChangeManager.appendRoundChangeMessage(any()))
        .thenReturn(Optional.of(roundChangCert));
    when(finalState.isLocalNodeProposerForRound(any())).thenReturn(true);

    final IbftBlockHeightManager manager =
        new IbftBlockHeightManager(
            headerTestFixture.buildHeader(),
            finalState,
            roundChangeManager,
            roundFactory,
            clock,
            messageValidatorFactory);
    manager.start();

    manager.handleRoundChangeMessage(roundChangePayload);

    verify(messageTransmitter, times(1))
        .multicastNewRound(eq(futureRoundIdentifier), eq(roundChangCert), any());
  }

  @Test
  public void messagesForFutureRoundsAreBufferedAndUsedToPreloadNewRoundWhenItIsStarted() {}
}
