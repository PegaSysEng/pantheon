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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import tech.pegasys.pantheon.consensus.ibft.ibftmessage.CommitMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.NewRoundMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.PrepareMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.ProposalMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.RoundChangeMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.Payload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.consensus.ibft.network.IbftNetworkPeers;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;

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


  private <P extends Payload> void assertRebroadcastToAllExceptSigner(
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

  private <P extends Payload> void assertRebroadcastToAllExceptSignerAndSuppliedExceptions(
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

  private <P extends Payload> void assertRebroadcastOnlyOnce(
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
  public void assertRebroadcastsProposalToAllExceptSigner() {
    assertRebroadcastToAllExceptSigner(
        TestHelpers::createSignedProposalPayload,
        ProposalMessageData::create,
        ibftGossip::gossipProposalPayload);
  }

  @Test
  public void assertRebroadcastsProposalToAllExceptSignerAndSuppliedExceptions() {
    assertRebroadcastToAllExceptSignerAndSuppliedExceptions(
        TestHelpers::createSignedProposalPayload,
        ProposalMessageData::create,
        ibftGossip::gossipProposalPayload);
  }

  @Test
  public void assertRebroadcastsProposalOnlyOnce() {
    assertRebroadcastOnlyOnce(
        TestHelpers::createSignedProposalPayload,
        ProposalMessageData::create,
        ibftGossip::gossipProposalPayload);
  }

  @Test
  public void assertRebroadcastsPrepareToAllExceptSigner() {
    assertRebroadcastToAllExceptSigner(
        TestHelpers::createSignedPreparePayload,
        PrepareMessageData::create,
        ibftGossip::gossipPreparePayload);
  }

  @Test
  public void assertRebroadcastsPrepareToAllExceptSignerAndSuppliedExceptions() {
    assertRebroadcastToAllExceptSignerAndSuppliedExceptions(
        TestHelpers::createSignedPreparePayload,
        PrepareMessageData::create,
        ibftGossip::gossipPreparePayload);
  }

  @Test
  public void assertRebroadcastsPrepareOnlyOnce() {
    assertRebroadcastOnlyOnce(
        TestHelpers::createSignedPreparePayload,
        PrepareMessageData::create,
        ibftGossip::gossipPreparePayload);
  }

  @Test
  public void assertRebroadcastsCommitToAllExceptSigner() {
    assertRebroadcastToAllExceptSigner(
        TestHelpers::createSignedCommitPayload,
        CommitMessageData::create,
        ibftGossip::gossipCommitPayload);
  }

  @Test
  public void assertRebroadcastsCommitToAllExceptSignerAndSuppliedExceptions() {
    assertRebroadcastToAllExceptSignerAndSuppliedExceptions(
        TestHelpers::createSignedCommitPayload,
        CommitMessageData::create,
        ibftGossip::gossipCommitPayload);
  }

  @Test
  public void assertRebroadcastsCommitOnlyOnce() {
    assertRebroadcastOnlyOnce(
        TestHelpers::createSignedCommitPayload,
        CommitMessageData::create,
        ibftGossip::gossipCommitPayload);
  }

  @Test
  public void assertRebroadcastsRoundChangeToAllExceptSigner() {
    assertRebroadcastToAllExceptSigner(
        TestHelpers::createSignedRoundChangePayload,
        RoundChangeMessageData::create,
        ibftGossip::gossipRoundChangePayload);
  }

  @Test
  public void assertRebroadcastsRoundChangeToAllExceptSignerAndSuppliedExceptions() {
    assertRebroadcastToAllExceptSignerAndSuppliedExceptions(
        TestHelpers::createSignedRoundChangePayload,
        RoundChangeMessageData::create,
        ibftGossip::gossipRoundChangePayload);
  }

  @Test
  public void assertRebroadcastsRoundChangeOnlyOnce() {
    assertRebroadcastOnlyOnce(
        TestHelpers::createSignedRoundChangePayload,
        RoundChangeMessageData::create,
        ibftGossip::gossipRoundChangePayload);
  }

  @Test
  public void assertRebroadcastsNewRoundToAllExceptSigner() {
    assertRebroadcastToAllExceptSigner(
        TestHelpers::createSignedNewRoundPayload,
        NewRoundMessageData::create,
        ibftGossip::gossipNewRoundPayload);
  }

  @Test
  public void assertRebroadcastsNewRoundToAllExceptSignerAndSuppliedExceptions() {
    assertRebroadcastToAllExceptSignerAndSuppliedExceptions(
        TestHelpers::createSignedNewRoundPayload,
        NewRoundMessageData::create,
        ibftGossip::gossipNewRoundPayload);
  }

  @Test
  public void assertRebroadcastsNewRoundOnlyOnce() {
    assertRebroadcastOnlyOnce(
        TestHelpers::createSignedNewRoundPayload,
        NewRoundMessageData::create,
        ibftGossip::gossipNewRoundPayload);
  }
}
