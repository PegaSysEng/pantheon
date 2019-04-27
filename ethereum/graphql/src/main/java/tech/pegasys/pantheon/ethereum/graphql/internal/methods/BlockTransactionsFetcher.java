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
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockWithMetadata;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.TransactionWithMetadata;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Block;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Transaction;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class BlockTransactionsFetcher
    implements DataFetcher<ArrayList<Transaction>>, GraphQLMethod {
  private BlockchainQueries blockchain;

  //    private static final Logger LOG = LogManager.getLogger();

  public BlockTransactionsFetcher(final BlockchainQueries blockchain) {
    this.blockchain = blockchain;
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.TRANSACTIONS;
  }

  @Override
  public ArrayList<Transaction> get(final DataFetchingEnvironment environment) {
    final long blockNumber = ((Block) environment.getSource()).getNumber().longValue();
    return getTransactions(blockNumber);
  }

  private ArrayList<Transaction> getTransactions(final Long blockNumber) {
    try {
      return blockchain.blockByNumber(blockNumber).map(this::extractTransactions).orElse(null);
    } catch (NullPointerException e) {
      return null;
    }
  }

  private ArrayList<Transaction> extractTransactions(
      final BlockWithMetadata<TransactionWithMetadata, Hash>
          transactionWithMetadataHashBlockWithMetadata) {
    return Lists.newArrayList(
        transactionWithMetadataHashBlockWithMetadata.getTransactions().stream()
            .map(Transaction::new)
            .collect(Collectors.toList()));
  }
}
