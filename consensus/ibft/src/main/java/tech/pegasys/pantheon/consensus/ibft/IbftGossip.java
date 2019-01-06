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

/** Class responsible for rebroadcasting IBFT messages to known validators */
public class IbftGossip {
  private final IbftNetworkPeers peers;

  // Size of the seenMessages cache, should end up utilising 65bytes * this number + some meta data
  private final int maxSeenMessages;

  // Set that starts evicting members when it hits capacity
  private final Set<Signature> seenMessages =
      Collections.newSetFromMap(
          new LinkedHashMap<Signature, Boolean>() {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<Signature, Boolean> eldest) {
              return size() > maxSeenMessages;
            }
          });

  IbftGossip(final IbftNetworkPeers peers, final int maxSeenMessages) {
    this.maxSeenMessages = maxSeenMessages;
    this.peers = peers;
  }

  /**
   * Constructor that attaches gossip logic to a set of peers
   *
   * @param peers The always up to date set of connected peers that understand IBFT
   */
  public IbftGossip(final IbftNetworkPeers peers) {
    this(peers, 10_000);
  }

  /**
   * Retransmit a given proposal payload to all IBFT nodes
   *
   * @param payload The signed payload to retransmit
   * @param excludeAddresses A list of addresses to not communicate this to. Does not need to
   *     include the payload signer, that is handled implicitly
   * @return Boolean of whether the message was rebroadcast or ignored as it's already been
   *     broadcast in recent memory
   */
  public boolean gossipProposalPayload(
      final SignedData<ProposalPayload> payload, final Address... excludeAddresses) {
    final ProposalMessageData messageData = ProposalMessageData.create(payload);
    return gossipMessage(
        payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  /**
   * Retransmit a given prepare payload to all IBFT nodes
   *
   * @param payload The signed payload to retransmit
   * @param excludeAddresses A list of addresses to not communicate this to. Does not need to
   *     include the payload signer, that is handled implicitly
   * @return Boolean of whether the message was rebroadcast or ignored as it's already been
   *     broadcast in recent memory
   */
  public boolean gossipPreparePayload(
      final SignedData<PreparePayload> payload, final Address... excludeAddresses) {
    final PrepareMessageData messageData = PrepareMessageData.create(payload);
    return gossipMessage(
        payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  /**
   * Retransmit a given commit payload to all IBFT nodes
   *
   * @param payload The signed payload to retransmit
   * @param excludeAddresses A list of addresses to not communicate this to. Does not need to
   *     include the payload signer, that is handled implicitly
   * @return Boolean of whether the message was rebroadcast or ignored as it's already been
   *     broadcast in recent memory
   */
  public boolean gossipCommitPayload(
      final SignedData<CommitPayload> payload, final Address... excludeAddresses) {
    final CommitMessageData messageData = CommitMessageData.create(payload);
    return gossipMessage(
        payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  /**
   * Retransmit a given round change payload to all IBFT nodes
   *
   * @param payload The signed payload to retransmit
   * @param excludeAddresses A list of addresses to not communicate this to. Does not need to
   *     include the payload signer, that is handled implicitly
   * @return Boolean of whether the message was rebroadcast or ignored as it's already been
   *     broadcast in recent memory
   */
  public boolean gossipRoundChangePayload(
      final SignedData<RoundChangePayload> payload, final Address... excludeAddresses) {
    final RoundChangeMessageData messageData = RoundChangeMessageData.create(payload);
    return gossipMessage(
        payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  /**
   * Retransmit a given new round payload to all IBFT nodes
   *
   * @param payload The signed payload to retransmit
   * @param excludeAddresses A list of addresses to not communicate this to. Does not need to
   *     include the payload signer, that is handled implicitly
   * @return Boolean of whether the message was rebroadcast or ignored as it's already been
   *     broadcast in recent memory
   */
  public boolean gossipNewRoundPayload(
      final SignedData<NewRoundPayload> payload, final Address... excludeAddresses) {
    final NewRoundMessageData messageData = NewRoundMessageData.create(payload);
    return gossipMessage(
        payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  // Checks whether a payloads signature has been seen recently, if not it performs the retransmit
  private boolean gossipMessage(
      final Signature signature,
      final MessageData messageData,
      final Address payloadSigner,
      final Address... excludeAddresses) {
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
