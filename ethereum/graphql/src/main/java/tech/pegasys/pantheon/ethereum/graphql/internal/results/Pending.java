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
package tech.pegasys.pantheon.ethereum.graphql.internal.results;

import tech.pegasys.pantheon.ethereum.eth.transactions.PendingTransactions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Pending implements GraphQLResult {

  private final Integer transactionCount;
  private List<Transaction> transactions;

  @JsonCreator
  public Pending(
      @JsonProperty("transactionCount") final Integer transactionCount,
      @JsonProperty("transactions") final List<Transaction> transactions) {

    this.transactionCount = transactionCount;
    this.transactions = transactions;
  }

  public Pending(final Set<PendingTransactions.TransactionInfo> transactionInfo) {
    this(
        transactionInfo.size(),
        transactionInfo.stream().map(item -> new Transaction(item)).collect(Collectors.toList()));
  }

  @JsonGetter(value = "transactionCount")
  public Integer getTransactionCount() {
    return transactionCount;
  }

  @JsonGetter(value = "transactions")
  public List<Transaction> getTransactions() {
    return transactions;
  }
}
