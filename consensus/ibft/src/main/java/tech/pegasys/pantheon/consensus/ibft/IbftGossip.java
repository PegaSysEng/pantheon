package tech.pegasys.pantheon.consensus.ibft;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import tech.pegasys.pantheon.ethereum.p2p.api.Message;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;

public class IbftGossip {
  private final IbftNetworkPeers peers;

  private static final int SEEN_MESSAGES_SIZE = 10_000;
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

  public boolean gossipProposalPayload(SignedData<ProposalPayload> payload, Address... excludeAddresses) {
    final ProposalMessageData messageData = ProposalMessageData.create(payload);
    return gossipMessage(payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  public boolean gossipPreparePayload(SignedData<PreparePayload> payload, Address... excludeAddresses) {
    final PrepareMessageData messageData = PrepareMessageData.create(payload);
    return gossipMessage(payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  public boolean gossipCommitPayload(SignedData<CommitPayload> payload, Address... excludeAddresses) {
    final CommitMessageData messageData = CommitMessageData.create(payload);
    return gossipMessage(payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  public boolean gossipRoundChangePayload(SignedData<RoundChangePayload> payload, Address... excludeAddresses) {
    final RoundChangeMessageData messageData = RoundChangeMessageData.create(payload);
    return gossipMessage(payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  public boolean gossipNewRoundPayload(SignedData<NewRoundPayload> payload, Address... excludeAddresses) {
    final NewRoundMessageData messageData = NewRoundMessageData.create(payload);
    return gossipMessage(payload.getSignature(), messageData, payload.getSender(), excludeAddresses);
  }

  private boolean gossipMessage(Signature signature, MessageData messageData, Address payloadSigner, Address... excludeAddresses) {
    if (seenMessages.contains(signature)) {
      return false;
    } else {
      final List<Address> excludeAddressesList = Arrays.asList(excludeAddresses);
      excludeAddressesList.add(payloadSigner);
      peers.multicastToValidatorsExcept(messageData, excludeAddressesList);
      seenMessages.add(signature);
      return true;
    }
  }


}
