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
package tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.results;

import tech.pegasys.pantheon.ethereum.eth.transactions.PendingTransactions.TransactionInfo;

import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonValue;

public class PendingTransactionsResult implements TransactionResult {

  private final Set<TransactionInfoResult> transactionInfoResults;

  public PendingTransactionsResult(final Set<TransactionInfo> transactionInfoSet) {
    transactionInfoResults =
        transactionInfoSet.stream()
            .map(t -> new TransactionInfoResult(t))
            .collect(Collectors.toSet());
  }

  @JsonValue
  public Set<TransactionInfoResult> getResults() {
    return transactionInfoResults;
  }
}
