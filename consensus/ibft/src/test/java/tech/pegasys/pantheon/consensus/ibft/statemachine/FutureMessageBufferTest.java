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
package tech.pegasys.pantheon.consensus.ibft.statemachine;

import static java.util.Arrays.copyOfRange;
import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.consensus.ibft.network.MockPeerFactory;
import tech.pegasys.pantheon.ethereum.core.AddressHelpers;
import tech.pegasys.pantheon.ethereum.p2p.api.Message;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.ethereum.p2p.api.PeerConnection;
import tech.pegasys.pantheon.ethereum.p2p.wire.DefaultMessage;
import tech.pegasys.pantheon.ethereum.p2p.wire.RawMessage;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

public class FutureMessageBufferTest {
  private Message message;
  private FutureMessageBuffer futureMsgBuffer;
  private PeerConnection peerConnection = MockPeerFactory.create(AddressHelpers.ofValue(9));

  @Before
  public void setup() {
    message = createMessage("0x10");
    futureMsgBuffer = new FutureMessageBuffer(5, 5, 0);
  }

  @Test
  public void addsFutureMessages() {
    futureMsgBuffer.addMessage(1, message);
    futureMsgBuffer.addMessage(2, message);
    assertThat(futureMsgBuffer.poll(1)).containsExactly(message);
    assertThat(futureMsgBuffer.poll(2)).containsExactly(message);
    assertThat(futureMsgBuffer.poll(2)).isEmpty();
  }

  @Test
  public void pollOnHeightWithNoMessagesReturnsEmptySet() {
    assertThat(futureMsgBuffer.poll(3)).isEmpty();
  }

  @Test
  public void doNotAddFutureMessagesWhenBeyondMaxDistanceFromCurrentHeight() {
    futureMsgBuffer.addMessage(6, message);
    futureMsgBuffer.addMessage(7, message);
    assertThat(futureMsgBuffer.poll(6)).isEmpty();
    assertThat(futureMsgBuffer.poll(7)).isEmpty();

    futureMsgBuffer.addMessage(1, message);
    futureMsgBuffer.addMessage(4, message);
    futureMsgBuffer.addMessage(5, message);
    assertThat(futureMsgBuffer.poll(1)).containsExactly(message);
    assertThat(futureMsgBuffer.poll(4)).containsExactly(message);
    assertThat(futureMsgBuffer.poll(5)).containsExactly(message);
  }

  @Test
  public void removeHighestChainHeightWhenOverMessageLimit() {
    DefaultMessage[] height1Msgs = addMessages(1, 4);
    addMessages(2, 1);
    futureMsgBuffer.addMessage(1, message);

    List<Message> actualHeight1Msgs = futureMsgBuffer.poll(1);
    assertThat(actualHeight1Msgs).contains(height1Msgs);
    assertThat(actualHeight1Msgs).contains(message);
    assertThat(futureMsgBuffer.poll(2)).isEmpty();
  }

  @Test
  public void removeOldestMessageWhenOverLimitAndSingleHeightInBuffer() {
    DefaultMessage[] messages = addMessages(1, 5);
    futureMsgBuffer.addMessage(1, message);

    final DefaultMessage[] messagesExcludingFirst = copyOfRange(messages, 1, messages.length);
    final List<Message> actualHeight1Msgs = futureMsgBuffer.poll(1L);
    assertThat(actualHeight1Msgs).contains(messagesExcludingFirst);
    assertThat(actualHeight1Msgs).contains(message);
  }

  @Test
  public void doNotAddMessageLessThanOrEqualToCurrentChainHeight() {
    futureMsgBuffer.updateChainHeight(2);
    futureMsgBuffer.addMessage(0, message);
    futureMsgBuffer.addMessage(1, message);
    futureMsgBuffer.addMessage(2, message);
    assertThat(futureMsgBuffer.poll(2)).isEmpty();

    futureMsgBuffer.addMessage(3, message);
    assertThat(futureMsgBuffer.poll(3)).containsExactly(message);
  }

  @Test
  public void doNotAddMessagesWhenLimitIsZero() {
    futureMsgBuffer = new FutureMessageBuffer(1, 0, 0);
    futureMsgBuffer.addMessage(1, message);
    assertThat(futureMsgBuffer.poll(0)).isEmpty();
  }

  @Test
  public void doNotMessageWhenMessageHeightIsHighestAndOverLimit() {
    DefaultMessage[] height1Msgs = addMessages(1, 4);
    DefaultMessage[] height2Msgs = addMessages(2, 1);
    futureMsgBuffer.addMessage(3, message);

    assertThat(futureMsgBuffer.poll(1)).containsExactly(height1Msgs);
    assertThat(futureMsgBuffer.poll(2)).containsExactly(height2Msgs);
    assertThat(futureMsgBuffer.poll(3)).isEmpty();
  }

  @Test
  public void totalMessagesSizeUpdatedWhenMessagesAdded() {
    futureMsgBuffer = new FutureMessageBuffer(5, 10, 0);

    addMessages(1, 2);
    assertThat(futureMsgBuffer.totalMessagesSize()).isEqualTo(2);

    addMessages(2, 3);
    assertThat(futureMsgBuffer.totalMessagesSize()).isEqualTo(5);

    addMessages(1, 3);
    assertThat(futureMsgBuffer.totalMessagesSize()).isEqualTo(8);
  }

  @Test
  public void totalMessagesSizeUpdatedWhenMessagesPolled() {
    futureMsgBuffer = new FutureMessageBuffer(5, 10, 0);
    addMessages(1, 2);
    addMessages(2, 3);
    addMessages(1, 3);

    futureMsgBuffer.poll(2);
    assertThat(futureMsgBuffer.totalMessagesSize()).isEqualTo(5);

    futureMsgBuffer.poll(1);
    assertThat(futureMsgBuffer.totalMessagesSize()).isZero();
  }

  @Test
  public void totalSizeUpdatedWhenMessagesAreEvicted() {
    futureMsgBuffer = new FutureMessageBuffer(5, 5, 0);
    addMessages(1, 2);
    addMessages(2, 3);

    // height 2 will be evicted
    futureMsgBuffer.addMessage(1, message);
    assertThat(futureMsgBuffer.totalMessagesSize()).isEqualTo(3);

    for (int i = 0; i <= 5; i++) {
      futureMsgBuffer.addMessage(1, message);
    }
    assertThat(futureMsgBuffer.totalMessagesSize()).isEqualTo(5);
  }

  @Test
  public void updatesChainHeight() {
    futureMsgBuffer.addMessage(1, message);
    assertThat(futureMsgBuffer.poll(1)).containsExactly(message);

    futureMsgBuffer.updateChainHeight(2);
    futureMsgBuffer.addMessage(1, message);
    futureMsgBuffer.addMessage(2, message);
    futureMsgBuffer.addMessage(3, message);
    assertThat(futureMsgBuffer.poll(1)).isEmpty();
    assertThat(futureMsgBuffer.poll(2)).isEmpty();
    assertThat(futureMsgBuffer.poll(3)).containsExactly(message);

    futureMsgBuffer.addMessage(4, message);
    futureMsgBuffer.addMessage(5, message);
    futureMsgBuffer.updateChainHeight(3);
    assertThat(futureMsgBuffer.poll(4)).containsExactly(message);
    assertThat(futureMsgBuffer.poll(5)).containsExactly(message);
  }

  @Test
  public void updateChainHeightDiscardsOldHeights() {
    futureMsgBuffer.addMessage(0, message);
    futureMsgBuffer.addMessage(1, message);
    futureMsgBuffer.addMessage(3, message);
    futureMsgBuffer.addMessage(4, message);

    futureMsgBuffer.updateChainHeight(3);
    assertThat(futureMsgBuffer.poll(1)).isEmpty();
    assertThat(futureMsgBuffer.poll(2)).isEmpty();
    assertThat(futureMsgBuffer.poll(3)).isEmpty();
  }

  private DefaultMessage[] addMessages(final long height, final int count) {
    return IntStream.range(0, count)
        .mapToObj(
            i -> {
              final DefaultMessage message = createMessage("0x0" + i);
              futureMsgBuffer.addMessage(height, message);
              return message;
            })
        .collect(Collectors.toList())
        .toArray(new DefaultMessage[] {});
  }

  private DefaultMessage createMessage(final String s) {
    final MessageData messageData = new RawMessage(0, BytesValue.fromHexString(s));
    return new DefaultMessage(peerConnection, messageData);
  }
}
