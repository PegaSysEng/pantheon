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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import tech.pegasys.pantheon.consensus.ibft.ibftmessage.PrepareMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.ProposalMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.consensus.ibft.network.IbftNetworkPeers;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.core.AddressHelpers;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;

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

  @Test
  public void rebroadcastsProposalToAllExceptSigner() {
    final KeyPair keypair = KeyPair.generate();
    final SignedData<ProposalPayload> payload = createSignedProposalPayload(keypair);
    ibftGossip.gossipProposalPayload(payload);
    final MessageData messageData = ProposalMessageData.create(payload);
    verify(ibftNetworkPeers)
        .multicastToValidatorsExcept(messageData, newArrayList(payload.getSender()));
  }

  @Test
  public void rebroadcastsProposalOnlyOnce() {
    final KeyPair keypair = KeyPair.generate();
    final SignedData<ProposalPayload> payload = createSignedProposalPayload(keypair);
    ibftGossip.gossipProposalPayload(payload);
    ibftGossip.gossipProposalPayload(payload);
    final MessageData messageData = ProposalMessageData.create(payload);
    verify(ibftNetworkPeers, times(1))
        .multicastToValidatorsExcept(messageData, newArrayList(payload.getSender()));
  }

  @Test
  public void rebroadcastsPrepareToAllExceptSigner() {
    final KeyPair keypair = KeyPair.generate();
    final SignedData<PreparePayload> payload = createSignedPreparePayload(keypair);
    ibftGossip.gossipPreparePayload(payload);
    final MessageData messageData = PrepareMessageData.create(payload);
    verify(ibftNetworkPeers)
        .multicastToValidatorsExcept(messageData, newArrayList(payload.getSender()));
  }

  @Test
  public void rebroadcastsPrepareOnlyOnce() {
    final KeyPair keypair = KeyPair.generate();
    final SignedData<PreparePayload> payload = createSignedPreparePayload(keypair);
    ibftGossip.gossipPreparePayload(payload);
    ibftGossip.gossipPreparePayload(payload);
    final MessageData messageData = PrepareMessageData.create(payload);
    verify(ibftNetworkPeers, times(1))
        .multicastToValidatorsExcept(messageData, newArrayList(payload.getSender()));
  }
}
