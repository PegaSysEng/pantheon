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

import tech.pegasys.pantheon.ethereum.p2p.api.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Supplier;

import com.google.common.annotations.VisibleForTesting;

/**
 * Buffer which holds future IBFT messages.
 *
 * <p>This buffer only allows messages to be added which have a chain height greater than current
 * height and up to chain futureMessagesMaxDistance from the current chain height.
 *
 * <p>When the total number of messages is greater futureMessagesLimit then messages are evicted.
 *
 * <p>If there is more than one height in the buffer then all messages for the highest chain height
 * are removed. Otherwise if there is only one height the oldest inserted message is removed.
 */
public class FutureMessageBuffer {
  private final NavigableMap<Long, List<Message>> buffer;
  private final long futureMessagesMaxDistance;
  private final long futureMessagesLimit;
  private final Supplier<Long> currentChainHeight;

  public FutureMessageBuffer(
      final long futureMessagesMaxDistance,
      final long futureMessagesLimit,
      final Supplier<Long> currentChainHeight) {
    this(futureMessagesMaxDistance, futureMessagesLimit, currentChainHeight, new TreeMap<>());
  }

  @VisibleForTesting
  FutureMessageBuffer(
      final long futureMessagesMaxDistance,
      final long futureMessagesLimit,
      final Supplier<Long> currentChainHeight,
      final NavigableMap<Long, List<Message>> buffer) {
    this.futureMessagesMaxDistance = futureMessagesMaxDistance;
    this.futureMessagesLimit = futureMessagesLimit;
    this.currentChainHeight = currentChainHeight;
    this.buffer = buffer;
  }

  public void addMessage(final long msgChainHeight, final Message rawMsg) {
    final long currentHeight = currentChainHeight.get();
    if (futureMessagesLimit == 0 || !validMessageHeight(msgChainHeight, currentHeight)) {
      return;
    }

    if (totalMessagesSize() >= futureMessagesLimit) {
      evictMessages();
    }

    addMessageToBuffer(msgChainHeight, rawMsg);
  }

  private void addMessageToBuffer(final long msgChainHeight, final Message rawMsg) {
    if (!buffer.containsKey(msgChainHeight)) {
      buffer.put(msgChainHeight, new ArrayList<>());
    }
    buffer.get(msgChainHeight).add(rawMsg);
  }

  private boolean validMessageHeight(final long msgChainHeight, final long currentHeight) {
    final boolean isFutureMsg = msgChainHeight > currentHeight;
    final boolean withinMaxChainHeight =
        msgChainHeight <= currentHeight + futureMessagesMaxDistance;
    return isFutureMsg && withinMaxChainHeight;
  }

  private void evictMessages() {
    if (buffer.size() > 1) {
      buffer.remove(buffer.lastKey());
    } else if (buffer.size() == 1) {
      List<Message> messages = buffer.firstEntry().getValue();
      messages.remove(0);
    }
  }

  public List<Message> poll(final long chainHeight) {
    final List<Message> messages = buffer.getOrDefault(chainHeight, Collections.emptyList());
    buffer.remove(chainHeight);
    return messages;
  }

  long totalMessagesSize() {
    return buffer.values().stream().map(List::size).reduce(0, Integer::sum).longValue();
  }
}
