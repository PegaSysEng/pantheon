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

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.consensus.ibft.network.MockPeerFactory;
import tech.pegasys.pantheon.ethereum.core.Address;
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

import org.junit.Before;
import org.junit.Test;

public class FutureMessageBufferTest {
  private Message message;
  private NavigableMap<Long, List<Message>> map;
  private FutureMessageBuffer buffer;

  @Before
  public void setup() {
    final Address address = AddressHelpers.ofValue(9);
    final PeerConnection peerConnection = MockPeerFactory.create(address);
    final MessageData messageData = new RawMessage(0, BytesValue.fromHexString("0x00"));
    message = new DefaultMessage(peerConnection, messageData);
    map = new TreeMap<>();
    buffer = new FutureMessageBuffer(5, 5, () -> 0L, map);
  }

  @Test
  public void addsFutureMessages() {
    buffer.addMessage(2, message);
    assertThat(map).hasSize(1);
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
    buffer.addMessage(7, message);
    buffer.addMessage(8, message);
    buffer.addMessage(10, message);
    assertThat(map).isEmpty();
  }

  @Test
  public void removeHighestChainHeightWhenOverMessageLimit() {
    addMessages(4, 1);
    addMessages(1, 2);
    buffer.addMessage(1, message);
    // TODO replace size asserts with contains asserts
    assertThat(map).hasSize(1);
    assertThat(map.get(1L)).hasSize(5);
    assertThat(map.get(2L)).isNull();
  }

  @Test
  public void removeOldestMessageWhenOverLimitAndSingleHeightInBuffer() {
    addMessages(5, 1);
    assertThat(map).hasSize(1);
    assertThat(map.get(1L)).hasSize(5);

    buffer.addMessage(2, message);
    assertThat(map).hasSize(2);
    assertThat(map.get(1L)).hasSize(4);
    assertThat(map.get(2L)).containsExactly(message);
  }

  @Test
  public void doNotAddMessageWhenLessThanCurrentChainHeight() {
    buffer.addMessage(0, message);
    assertThat(map).isEmpty();
  }

  private void addMessages(final int count, final long height) {
    for (int i = 0; i < count; i++) {
      buffer.addMessage(height, message);
    }
  }
}
