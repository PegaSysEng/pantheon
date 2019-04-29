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
package tech.pegasys.pantheon.ethereum.graphql.internal.methods;

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.graphql.internal.filter.LogsQuery;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.TransactionReceiptWithMetadata;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Log;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Transaction;

import java.util.ArrayList;
import java.util.stream.Collectors;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class TransactionLogsFetcher implements DataFetcher<ArrayList<Log>>, GraphQLMethod {
  private BlockchainQueries blockchain;

  public TransactionLogsFetcher(final BlockchainQueries blockchain) {
    this.blockchain = blockchain;
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.TRANSACTION_LOGS;
  }

  @Override
  public ArrayList<Log> get(final DataFetchingEnvironment environment) {
    final Hash hash = Hash.fromHexString(((Transaction) environment.getSource()).getHash());
    final TransactionReceiptWithMetadata receipt =
        blockchain.transactionReceiptByTransactionHash(hash).orElse(null);
    return blockchain.matchingLogs(receipt.getBlockHash(), new LogsQuery.Builder().build()).stream()
        .filter(item -> item.getTransactionHash() == hash)
        .map(log -> new Log(log, receipt))
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
