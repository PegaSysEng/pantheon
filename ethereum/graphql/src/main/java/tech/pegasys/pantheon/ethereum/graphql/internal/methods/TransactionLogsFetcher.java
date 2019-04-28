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

import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Log;

import java.util.ArrayList;

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
    //      final Hash hash = Hash.fromHexString(((Transaction) environment.getSource()).getHash());
    //      final Hash blockHash =
    //   blockchain.transactionByHash(hash).map(TransactionWithMetadata::getBlockHash).orElse(null);
    //      blockchain.matchingLogs(blockHash, new LogsQuery.Builder().build()).stream().filter(item
    // ->
    //   item.getTransactionHash() == hash).collect(Collectors.toList())

    return null;
  }
}
