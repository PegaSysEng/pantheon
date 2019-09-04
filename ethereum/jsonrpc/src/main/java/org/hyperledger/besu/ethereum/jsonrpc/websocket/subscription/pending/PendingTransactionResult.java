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
package org.hyperledger.besu.ethereum.jsonrpc.websocket.subscription.pending;

import com.fasterxml.jackson.annotation.JsonValue;
import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.jsonrpc.internal.results.JsonRpcResult;

public class PendingTransactionResult implements JsonRpcResult {

  private final String hash;

  public PendingTransactionResult(final Hash hash) {
    this.hash = hash.toString();
  }

  @JsonValue
  public String getHash() {
    return hash;
  }
}
