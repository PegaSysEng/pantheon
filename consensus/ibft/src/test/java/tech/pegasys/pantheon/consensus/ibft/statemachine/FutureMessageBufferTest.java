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
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

public class FutureMessageBufferTest {
  private Message message;
  private NavigableMap<Long, List<Message>> map;
  private FutureMessageBuffer buffer;
  private PeerConnection peerConnection = MockPeerFactory.create(AddressHelpers.ofValue(9));
  private long currentHeight = 0;

  @Before
  public void setup() {
    message = createMessage("0x10");
    map = new TreeMap<>();
    buffer = new FutureMessageBuffer(5, 5, () -> currentHeight, map);
  }

  @Test
  public void addsFutureMessages() {
    buffer.addMessage(1, message);
    buffer.addMessage(2, message);
    assertThat(map).hasSize(2);
    assertThat(map.get(1L)).containsExactly(message);
    assertThat(map.get(2L)).containsExactly(message);
  }

  @Test
  public void pollReturnsMessagesForHeightAndRemovesThem() {
    buffer.addMessage(2, message);
    buffer.addMessage(3, message);

    final List<Message> messagesHeight2 = buffer.poll(2);
    assertThat(messagesHeight2).containsExactly(message);
    assertThat(messagesHeight2).hasSize(1);

    final List<Message> messagesHeight3 = buffer.poll(3);
    assertThat(messagesHeight3).hasSize(1);
    assertThat(map).isEmpty();
  }

  @Test
  public void pollOnHeightWithNoMessagesReturnsEmptySet() {
    assertThat(buffer.poll(3)).isEmpty();
  }

  @Test
  public void doNotAddFutureMessagesWhenBeyondMaxDistanceFromCurrentHeight() {
    buffer.addMessage(6, message);
    buffer.addMessage(7, message);
    assertThat(map).isEmpty();

    buffer.addMessage(1, message);
    buffer.addMessage(4, message);
    buffer.addMessage(5, message);
    assertThat(map).hasSize(3);
    assertThat(map.get(1L)).containsExactly(message);
    assertThat(map.get(4L)).containsExactly(message);
    assertThat(map.get(5L)).containsExactly(message);
  }

  @Test
  public void removeHighestChainHeightWhenOverMessageLimit() {
    DefaultMessage[] height1Msgs = addMessages(4, 1);
    addMessages(1, 2);

    buffer.addMessage(1, message);
    assertThat(map).hasSize(1);
    assertThat(map.get(1L)).contains(height1Msgs);
    assertThat(map.get(1L)).contains(message);
    assertThat(map.get(1L)).hasSize(5);
    assertThat(map.get(2L)).isNull();
  }

  @Test
  public void removeOldestMessageWhenOverLimitAndSingleHeightInBuffer() {
    DefaultMessage[] messages = addMessages(5, 1);

    buffer.addMessage(1, message);
    assertThat(map).hasSize(1);
    assertThat(map.get(1L)).hasSize(5);
    final DefaultMessage[] messagesExcludingFirst = copyOfRange(messages, 1, messages.length);
    assertThat(map.get(1L)).contains(messagesExcludingFirst);
    assertThat(map.get(1L)).contains(message);
  }

  @Test
  public void doNotAddMessageLessThanOrEqualToCurrentChainHeight() {
    currentHeight = 2;
    buffer.addMessage(0, message);
    buffer.addMessage(1, message);
    buffer.addMessage(2, message);
    assertThat(map).isEmpty();

    buffer.addMessage(3, message);
    assertThat(map).hasSize(1);
  }

  @Test
  public void doNotAddMessagesWhenLimitIsZero() {
    buffer = new FutureMessageBuffer(1, 0, () -> 0L, map);
    buffer.addMessage(1, message);
    assertThat(map).isEmpty();
  }

  @Test
  public void totalMessagesSizeUpdatedWhenMessagesAdded() {
    buffer = new FutureMessageBuffer(5, 10, () -> 0L, map);
    addMessages(2, 1);
    assertThat(buffer.totalMessagesSize()).isEqualTo(2);

    addMessages(3, 2);
    assertThat(buffer.totalMessagesSize()).isEqualTo(5);

    addMessages(3, 1);
    assertThat(buffer.totalMessagesSize()).isEqualTo(8);
  }

  @Test
  public void totalMessagesSizeUpdatedWhenMessagesPolled() {
    buffer = new FutureMessageBuffer(5, 10, () -> 0L, map);
    addMessages(2, 1);
    addMessages(3, 2);
    addMessages(3, 1);

    buffer.poll(2);
    assertThat(buffer.totalMessagesSize()).isEqualTo(5);

    buffer.poll(1);
    assertThat(buffer.totalMessagesSize()).isZero();
  }

  @Test
  public void totalSizeUpdatedWhenMessagesAreEvicted() {
    buffer = new FutureMessageBuffer(5, 5, () -> 0L, map);
    addMessages(2, 1);
    addMessages(3, 2);

    // height 2 will be evicted
    buffer.addMessage(1, message);
    assertThat(buffer.totalMessagesSize()).isEqualTo(3);

    for (int i = 0; i <= 5; i++) {
      buffer.addMessage(1, message);
    }
    assertThat(buffer.totalMessagesSize()).isEqualTo(5);
  }

  private DefaultMessage[] addMessages(final int count, final long height) {
    return IntStream.range(0, count)
        .mapToObj(
            i -> {
              final DefaultMessage message = createMessage("0x0" + i);
              buffer.addMessage(height, message);
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
