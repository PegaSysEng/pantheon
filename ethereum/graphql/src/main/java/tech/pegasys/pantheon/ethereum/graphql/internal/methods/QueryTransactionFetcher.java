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
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Transaction;
import tech.pegasys.pantheon.util.bytes.Bytes32;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class QueryTransactionFetcher implements DataFetcher<Transaction>, GraphQLMethod {

  private BlockchainQueries blockchain;

  public QueryTransactionFetcher(final BlockchainQueries blockchain) {
    this.blockchain = blockchain;
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.TRANSACTION;
  }

  @Override
  public Transaction get(final DataFetchingEnvironment environment) {
    Bytes32 hash = environment.getArgument("hash");
    return getTransactionByHash(hash);
  }

  private Transaction getTransactionByHash(final Bytes32 hash) {
    return blockchain
        .transactionReceiptByTransactionHash(Hash.wrap(hash))
        .map(Transaction::new)
        .orElse(null);
  }
}
