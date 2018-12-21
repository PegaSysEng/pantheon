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
package tech.pegasys.pantheon.consensus.ibft;

import tech.pegasys.pantheon.consensus.ibft.ibftmessage.CommitMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.NewRoundMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.PrepareMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.ProposalMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.RoundChangeMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.CommitPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.NewRoundPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.consensus.ibft.network.IbftNetworkPeers;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

public class IbftGossip {
  private final IbftNetworkPeers peers;

  private static final int SEEN_MESSAGES_SIZE = 10_000;
  // Set that starts evicting members when it hits capacity
  private final Set<Signature> seenMessages =
      Collections.newSetFromMap(
          new LinkedHashMap<Signature, Boolean>() {
            protected boolean removeEldestEntry(Map.Entry<Signature, Boolean> eldest) {
              return size() > SEEN_MESSAGES_SIZE;
            }
          });

  public IbftGossip(IbftNetworkPeers peers) {
    this.peers = peers;
  }

  public boolean gossipProposalPayload(
      SignedData<ProposalPayload> payload, Address... excludeAddresses) {
    final ProposalMessageData messageData = ProposalMessageData.create(payload);
    return gossipMessage(
        payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  public boolean gossipPreparePayload(
      SignedData<PreparePayload> payload, Address... excludeAddresses) {
    final PrepareMessageData messageData = PrepareMessageData.create(payload);
    return gossipMessage(
        payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  public boolean gossipCommitPayload(
      SignedData<CommitPayload> payload, Address... excludeAddresses) {
    final CommitMessageData messageData = CommitMessageData.create(payload);
    return gossipMessage(
        payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  public boolean gossipRoundChangePayload(
      SignedData<RoundChangePayload> payload, Address... excludeAddresses) {
    final RoundChangeMessageData messageData = RoundChangeMessageData.create(payload);
    return gossipMessage(
        payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  public boolean gossipNewRoundPayload(
      SignedData<NewRoundPayload> payload, Address... excludeAddresses) {
    final NewRoundMessageData messageData = NewRoundMessageData.create(payload);
    return gossipMessage(
        payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  private boolean gossipMessage(
      Signature signature,
      MessageData messageData,
      Address payloadSigner,
      Address... excludeAddresses) {
    if (seenMessages.contains(signature)) {
      return false;
    } else {
      final List<Address> excludeAddressesList = Lists.newArrayList(excludeAddresses);
      excludeAddressesList.add(payloadSigner);
      peers.multicastToValidatorsExcept(messageData, excludeAddressesList);
      seenMessages.add(signature);
      return true;
    }
  }
}
