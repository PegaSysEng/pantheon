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
package tech.pegasys.pantheon.consensus.ibft;

import tech.pegasys.pantheon.consensus.ibft.network.ValidatorMulticaster;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;

import java.util.Collection;
import java.util.Collections;

public class UniqueMessageMulticaster implements ValidatorMulticaster {
  private final ValidatorMulticaster multicaster;
  private final MessageTracker messageTracker;

  /**
   * Constructor that attaches gossip logic to a set of multicaster
   *
   * @param multicaster Network connections to the remote validators
   * @param messageTracker Tracks seen messages
   */
  public UniqueMessageMulticaster(
      final ValidatorMulticaster multicaster, final MessageTracker messageTracker) {
    this.multicaster = multicaster;
    this.messageTracker = messageTracker;
  }

  @Override
  public void send(final MessageData message) {
    send(message, Collections.emptyList());
  }

  @Override
  public void send(final MessageData message, final Collection<Address> blackList) {
    if (messageTracker.hasSeenMessage(message)) {
      return;
    }
    multicaster.send(message, blackList);
    messageTracker.addSeenMessage(message);
  }
}
