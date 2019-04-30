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
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Block;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Transaction;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class BlockTransactionAtFetcher implements DataFetcher<Transaction>, GraphQLMethod {

  private BlockchainQueries blockchain;

  public BlockTransactionAtFetcher(final BlockchainQueries blockchain) {
    this.blockchain = blockchain;
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.TRANSACTION_AT;
  }

  @Override
  public Transaction get(final DataFetchingEnvironment environment) {
    final long blockNumber = ((Block) environment.getSource()).getNumber().longValue();
    if (environment.containsArgument("index")) {
      Integer transactionAt = environment.getArgument("index");
      return getTransactionAt(blockNumber, transactionAt);
    }
    return null;
  }

  private Transaction getTransactionAt(final long blockNumber, final Integer transactionAt) {
    Hash transactionHash =
        blockchain
            .transactionByBlockNumberAndIndex(blockNumber, transactionAt)
            .map(transaction -> transaction.getTransaction().hash())
            .orElse(null);
    if (transactionHash == null) {
      return null;
    }
    return blockchain
        .transactionReceiptByTransactionHash(transactionHash)
        .map(Transaction::new)
        .orElse(null);
  }
}
