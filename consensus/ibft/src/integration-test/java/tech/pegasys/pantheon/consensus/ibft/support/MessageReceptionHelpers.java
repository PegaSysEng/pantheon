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

import tech.pegasys.pantheon.consensus.ibft.ibftmessage.CommitMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.IbftV2;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.NewRoundMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.PrepareMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.ProposalMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.RoundChangeMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.Payload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;

import java.util.Arrays;
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

    List<SignedData<? extends Payload>> msgList = Arrays.asList(msgs);

    for (int i = 0; i < msgList.size(); i++) {
      final int index = i;
      final SignedData<? extends Payload> msg = msgList.get(index);
      allPeers.forEach(
          n -> {
            final List<MessageData> rxMsgs = n.getReceivedMessages();
            final MessageData rxMsgData = rxMsgs.get(index);
            msgMatchesExpected(rxMsgData, msg);
          });
    }
    allPeers.forEach(p -> p.clearReceivedMessages());
  }

  public static void msgMatchesExpected(
      final MessageData actual, final SignedData<? extends Payload> expected) {
    final Payload expectedPayload = expected.getPayload();
    SignedData<?> actualMessage = null;

    switch (expectedPayload.getMessageType()) {
      case IbftV2.PROPOSAL:
        actualMessage = ProposalMessageData.fromMessageData(actual).decode();
        break;
      case IbftV2.PREPARE:
        actualMessage = PrepareMessageData.fromMessageData(actual).decode();
        break;
      case IbftV2.COMMIT:
        actualMessage = CommitMessageData.fromMessageData(actual).decode();
        break;
        // final SignedData<CommitPayload> actualCommit =
        // CommitMessageData.fromMessageData(actual).decode();
        // assertThat(expectedPayload).isEqualToComparingFieldByField(actualCommit.getPayload());
      case IbftV2.NEW_ROUND:
        actualMessage = NewRoundMessageData.fromMessageData(actual).decode();
        break;
        // final SignedData<NewRoundPayload> actualNewRound =
        // NewRoundMessageData.fromMessageData(actual).decode();
        // assertThat(expectedPayload).isEqualToComparingFieldByField(actualNewRound.getPayload());
      case IbftV2.ROUND_CHANGE:
        actualMessage = RoundChangeMessageData.fromMessageData(actual).decode();
        break;
        // final SignedData<RoundChangePayload> actualRoundChange =
        // RoundChangeMessageData.fromMessageData(actual).decode();
        // assertThat(expectedPayload).isEqualToComparingFieldByField(actualRoundChange.getPayload());
      default:
        assertThat(true).isFalse(); // fail
        break;
    }
    assertThat(expectedPayload.equals(actualMessage.getPayload())).isTrue();
  }
}
