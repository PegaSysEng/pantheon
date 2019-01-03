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
package tech.pegasys.pantheon.consensus.ibft;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import tech.pegasys.pantheon.consensus.ibft.ibftmessage.CommitMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.NewRoundMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.PrepareMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.ProposalMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.RoundChangeMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.CommitPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.NewRoundPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.Payload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangeCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.consensus.ibft.network.IbftNetworkPeers;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.AddressHelpers;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IbftGossipTest {
  private IbftGossip ibftGossip;
  @Mock private IbftNetworkPeers ibftNetworkPeers;

  @Before
  public void setup() {
    ibftGossip = new IbftGossip(ibftNetworkPeers);
  }

  private SignedData<ProposalPayload> createSignedProposalPayload(final KeyPair signerKeys) {
    final MessageFactory messageFactory = new MessageFactory(signerKeys);
    final ConsensusRoundIdentifier roundIdentifier =
        new ConsensusRoundIdentifier(0x1234567890ABCDEFL, 0xFEDCBA98);
    final Block block =
        TestHelpers.createProposalBlock(singletonList(AddressHelpers.ofValue(1)), 0);
    return messageFactory.createSignedProposalPayload(roundIdentifier, block);
  }

  private SignedData<PreparePayload> createSignedPreparePayload(final KeyPair signerKeys) {
    final MessageFactory messageFactory = new MessageFactory(signerKeys);
    final ConsensusRoundIdentifier roundIdentifier =
        new ConsensusRoundIdentifier(0x1234567890ABCDEFL, 0xFEDCBA98);
    return messageFactory.createSignedPreparePayload(
        roundIdentifier, Hash.fromHexStringLenient("0"));
  }

  private SignedData<CommitPayload> createSignedCommitPayload(final KeyPair signerKeys) {
    final MessageFactory messageFactory = new MessageFactory(signerKeys);
    final ConsensusRoundIdentifier roundIdentifier =
        new ConsensusRoundIdentifier(0x1234567890ABCDEFL, 0xFEDCBA98);
    return messageFactory.createSignedCommitPayload(
        roundIdentifier,
        Hash.fromHexStringLenient("0"),
        Signature.create(BigInteger.ONE, BigInteger.TEN, (byte) 0));
  }

  private SignedData<RoundChangePayload> createSignedRoundChangePayload(final KeyPair signerKeys) {
    final MessageFactory messageFactory = new MessageFactory(signerKeys);
    final ConsensusRoundIdentifier roundIdentifier =
        new ConsensusRoundIdentifier(0x1234567890ABCDEFL, 0xFEDCBA98);
    return messageFactory.createSignedRoundChangePayload(roundIdentifier, Optional.empty());
  }

  private SignedData<NewRoundPayload> createSignedNewRoundPayload(final KeyPair signerKeys) {
    final MessageFactory messageFactory = new MessageFactory(signerKeys);
    final ConsensusRoundIdentifier roundIdentifier =
        new ConsensusRoundIdentifier(0x1234567890ABCDEFL, 0xFEDCBA98);
    final SignedData<ProposalPayload> proposalPayload = createSignedProposalPayload(signerKeys);
    return messageFactory.createSignedNewRoundPayload(
        roundIdentifier, new RoundChangeCertificate(newArrayList()), proposalPayload);
  }

  private <P extends Payload> void rebroadcastToAllExceptSigner(
      final Function<KeyPair, SignedData<P>> createPayload,
      final Function<SignedData<P>, MessageData> createMessageData,
      final Function<SignedData<P>, Boolean> gossiper) {
    final KeyPair keypair = KeyPair.generate();
    final SignedData<P> payload = createPayload.apply(keypair);
    final boolean gossipResult = gossiper.apply(payload);
    assertThat(gossipResult).isTrue();
    final MessageData messageData = createMessageData.apply(payload);
    verify(ibftNetworkPeers)
        .multicastToValidatorsExcept(messageData, newArrayList(payload.getSender()));
  }

  private <P extends Payload> void rebroadcastToAllExceptSignerAndSuppliedExceptions(
      final Function<KeyPair, SignedData<P>> createPayload,
      final Function<SignedData<P>, MessageData> createMessageData,
      final BiFunction<SignedData<P>, Address, Boolean> gossiper) {
    final KeyPair keypair = KeyPair.generate();
    final KeyPair keypair2 = KeyPair.generate();
    final Address key2Address = Util.publicKeyToAddress(keypair2.getPublicKey());
    final SignedData<P> payload = createPayload.apply(keypair);
    final boolean gossipResult = gossiper.apply(payload, key2Address);
    assertThat(gossipResult).isTrue();
    final MessageData messageData = createMessageData.apply(payload);
    verify(ibftNetworkPeers)
        .multicastToValidatorsExcept(messageData, newArrayList(key2Address, payload.getSender()));
  }

  private <P extends Payload> void rebroadcastOnlyOnce(
      final Function<KeyPair, SignedData<P>> createPayload,
      final Function<SignedData<P>, MessageData> createMessageData,
      final Function<SignedData<P>, Boolean> gossiper) {
    final KeyPair keypair = KeyPair.generate();
    final SignedData<P> payload = createPayload.apply(keypair);
    final boolean gossip1Result = gossiper.apply(payload);
    final boolean gossip2Result = gossiper.apply(payload);
    assertThat(gossip1Result).isTrue();
    assertThat(gossip2Result).isFalse();
    final MessageData messageData = createMessageData.apply(payload);
    verify(ibftNetworkPeers, times(1))
        .multicastToValidatorsExcept(messageData, newArrayList(payload.getSender()));
  }

  @Test
  public void rebroadcastsProposalToAllExceptSigner() {
    rebroadcastToAllExceptSigner(
        this::createSignedProposalPayload,
        ProposalMessageData::create,
        ibftGossip::gossipProposalPayload);
  }

  @Test
  public void rebroadcastsProposalToAllExceptSignerAndSuppliedExceptions() {
    rebroadcastToAllExceptSignerAndSuppliedExceptions(
        this::createSignedProposalPayload,
        ProposalMessageData::create,
        ibftGossip::gossipProposalPayload);
  }

  @Test
  public void rebroadcastsProposalOnlyOnce() {
    rebroadcastOnlyOnce(
        this::createSignedProposalPayload,
        ProposalMessageData::create,
        ibftGossip::gossipProposalPayload);
  }

  @Test
  public void rebroadcastsPrepareToAllExceptSigner() {
    rebroadcastToAllExceptSigner(
        this::createSignedPreparePayload,
        PrepareMessageData::create,
        ibftGossip::gossipPreparePayload);
  }

  @Test
  public void rebroadcastsPrepareToAllExceptSignerAndSuppliedExceptions() {
    rebroadcastToAllExceptSignerAndSuppliedExceptions(
        this::createSignedPreparePayload,
        PrepareMessageData::create,
        ibftGossip::gossipPreparePayload);
  }

  @Test
  public void rebroadcastsPrepareOnlyOnce() {
    rebroadcastOnlyOnce(
        this::createSignedPreparePayload,
        PrepareMessageData::create,
        ibftGossip::gossipPreparePayload);
  }

  @Test
  public void rebroadcastsCommitToAllExceptSigner() {
    rebroadcastToAllExceptSigner(
        this::createSignedCommitPayload,
        CommitMessageData::create,
        ibftGossip::gossipCommitPayload);
  }

  @Test
  public void rebroadcastsCommitToAllExceptSignerAndSuppliedExceptions() {
    rebroadcastToAllExceptSignerAndSuppliedExceptions(
        this::createSignedCommitPayload,
        CommitMessageData::create,
        ibftGossip::gossipCommitPayload);
  }

  @Test
  public void rebroadcastsCommitOnlyOnce() {
    rebroadcastOnlyOnce(
        this::createSignedCommitPayload,
        CommitMessageData::create,
        ibftGossip::gossipCommitPayload);
  }

  @Test
  public void rebroadcastsRoundChangeToAllExceptSigner() {
    rebroadcastToAllExceptSigner(
        this::createSignedRoundChangePayload,
        RoundChangeMessageData::create,
        ibftGossip::gossipRoundChangePayload);
  }

  @Test
  public void rebroadcastsRoundChangeToAllExceptSignerAndSuppliedExceptions() {
    rebroadcastToAllExceptSignerAndSuppliedExceptions(
        this::createSignedRoundChangePayload,
        RoundChangeMessageData::create,
        ibftGossip::gossipRoundChangePayload);
  }

  @Test
  public void rebroadcastsRoundChangeOnlyOnce() {
    rebroadcastOnlyOnce(
        this::createSignedRoundChangePayload,
        RoundChangeMessageData::create,
        ibftGossip::gossipRoundChangePayload);
  }

  @Test
  public void rebroadcastsNewRoundToAllExceptSigner() {
    rebroadcastToAllExceptSigner(
        this::createSignedNewRoundPayload,
        NewRoundMessageData::create,
        ibftGossip::gossipNewRoundPayload);
  }

  @Test
  public void rebroadcastsNewRoundToAllExceptSignerAndSuppliedExceptions() {
    rebroadcastToAllExceptSignerAndSuppliedExceptions(
        this::createSignedNewRoundPayload,
        NewRoundMessageData::create,
        ibftGossip::gossipNewRoundPayload);
  }

  @Test
  public void rebroadcastsNewRoundOnlyOnce() {
    rebroadcastOnlyOnce(
        this::createSignedNewRoundPayload,
        NewRoundMessageData::create,
        ibftGossip::gossipNewRoundPayload);
  }
}
