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
package tech.pegasys.pantheon.consensus.ibft.support;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.consensus.ibft.ibftmessage.CommitMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.NewRoundMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.PrepareMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.ProposalMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.RoundChangeMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.CommitPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.NewRoundPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.Payload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;

import java.util.Collection;
import java.util.List;

public class MessageReceptionHelpers {

  public static void assertPeersReceivedNoMessages(final Collection<ValidatorPeer> nodes) {
    nodes.forEach(n -> assertThat(n.getReceivedMessages()).isEmpty());
  }

  @SafeVarargs
  public static void assertPeersReceivedExactly(
      final Collection<ValidatorPeer> allPeers, final SignedData<? extends Payload>... msgs) {
    allPeers.forEach(n -> assertThat(n.getReceivedMessages().size()).isEqualTo(msgs.length));

    for (SignedData<? extends Payload> msg : msgs) {
      allPeers.forEach(
          n -> {
            final List<MessageData> rxMsgs = n.getReceivedMessages();
            final MessageData rxMsgData = rxMsgs.get(0);
            rxMsgs.remove(0);
            assertThat(msgMatchesExpected(rxMsgData, msg)).isTrue();
          });
    }
  }

  public static boolean msgMatchesExpected(
      final MessageData actual, SignedData<? extends Payload> expected) {
    final Payload expectedPayload = expected.getPayload();
    if (expectedPayload instanceof ProposalPayload) {
      final SignedData<ProposalPayload> actualPayload =
          ProposalMessage.fromMessage(actual).decode();
      return actualPayload.equals(expected);
    } else if (expectedPayload instanceof PreparePayload) {
      final SignedData<PreparePayload> actualPayload = PrepareMessage.fromMessage(actual).decode();
      return actualPayload.equals(expected);
    } else if (expectedPayload instanceof CommitPayload) {
      final SignedData<CommitPayload> actualPayload = CommitMessage.fromMessage(actual).decode();
      return actualPayload.equals(expected);
    } else if (expectedPayload instanceof RoundChangePayload) {
      final SignedData<RoundChangePayload> actualPayload =
          RoundChangeMessage.fromMessage(actual).decode();
      return actualPayload.equals(expected);
    } else if (expectedPayload instanceof NewRoundPayload) {
      final SignedData<NewRoundPayload> actualPayload =
          NewRoundMessage.fromMessage(actual).decode();
      return actualPayload.equals(expected);
    } else {
      return false;
    }
  }
}
