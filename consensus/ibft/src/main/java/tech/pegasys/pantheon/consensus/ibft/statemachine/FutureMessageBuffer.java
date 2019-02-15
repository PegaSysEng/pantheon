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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Supplier;

import com.google.common.annotations.VisibleForTesting;

public class FutureMessageBuffer {
  private final NavigableMap<Long, List<Message>> buffer;
  private final long chainHeightDistance;
  private final long messagesLimit;
  private final Supplier<Long> currentChainHeight;
  private long totalMessages;

  public FutureMessageBuffer(
      final long chainHeightDistance,
      final long messagesLimit,
      final Supplier<Long> currentChainHeight) {
    this(chainHeightDistance, messagesLimit, currentChainHeight, new TreeMap<>());
  }

  @VisibleForTesting
  FutureMessageBuffer(
      final long chainHeightDistance,
      final long totalMessagesLimit,
      final Supplier<Long> currentChainHeight,
      final NavigableMap<Long, List<Message>> buffer) {
    this.chainHeightDistance = chainHeightDistance;
    this.messagesLimit = totalMessagesLimit;
    this.currentChainHeight = currentChainHeight;
    this.buffer = buffer;
  }

  public void addMessage(final long msgChainHeight, final Message rawMsg) {
    long chainHeight = currentChainHeight.get();
    if (!validMessageHeight(msgChainHeight, chainHeight)) {
      return;
    }

    if (totalMessages >= messagesLimit) {
      evictMessages();
    }

    if (!buffer.containsKey(msgChainHeight)) {
      buffer.put(msgChainHeight, new LinkedList<>());
    }
    buffer.get(msgChainHeight).add(rawMsg);
    totalMessages += 1;
  }

  private boolean validMessageHeight(final long msgChainHeight, final long chainHeight) {
    boolean isFutureMsg = msgChainHeight > chainHeight;
    boolean isLessThanMaxHeight = msgChainHeight <= chainHeight + chainHeightDistance;
    return isFutureMsg && isLessThanMaxHeight;
  }

  private void evictMessages() {
    if (buffer.size() > 1) {
      buffer.remove(buffer.lastKey());
    } else {
      List<Message> messages = buffer.firstEntry().getValue();
      messages.remove(0);
    }
  }

  public List<Message> poll(final long chainHeight) {
    final List<Message> messages = buffer.getOrDefault(chainHeight, Collections.emptyList());
    buffer.remove(chainHeight);
    return messages;
  }
}
