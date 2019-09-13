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
package tech.pegasys.pantheon.ethereum.api.jsonrpc.websocket.subscription.syncing;

import tech.pegasys.pantheon.ethereum.api.jsonrpc.websocket.subscription.Subscription;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.websocket.subscription.request.SubscriptionType;

public class SyncingSubscription extends Subscription {
  private boolean firstMessageHasBeenSent = false;

  public SyncingSubscription(
      final Long subscriptionId,
      final String connectionId,
      final SubscriptionType subscriptionType) {
    super(subscriptionId, connectionId, subscriptionType, Boolean.FALSE);
  }

  public void setFirstMessageHasBeenSent(final boolean firstMessageHasBeenSent) {
    this.firstMessageHasBeenSent = firstMessageHasBeenSent;
  }

  public boolean isFirstMessageHasBeenSent() {
    return firstMessageHasBeenSent;
  }
}
