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
import tech.pegasys.pantheon.ethereum.core.LogTopic;
import tech.pegasys.pantheon.ethereum.graphql.internal.filter.LogsQuery;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.TransactionReceiptWithMetadata;
import tech.pegasys.pantheon.ethereum.graphql.internal.request.BlockFilterCriteria;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Block;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Log;

import java.util.ArrayList;
import java.util.stream.Collectors;

import graphql.GraphQLException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class BlockLogsFetcher implements DataFetcher<ArrayList<Log>>, GraphQLMethod {
  private BlockchainQueries blockchain;

  //    private static final Logger LOG = LogManager.getLogger();

  public BlockLogsFetcher(final BlockchainQueries blockchain) {
    this.blockchain = blockchain;
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.BLOCK_LOG;
  }

  @Override
  public ArrayList<Log> get(final DataFetchingEnvironment environment) {
    final String blockHash = ((Block) environment.getSource()).getHash();
    final BlockFilterCriteria filter = new BlockFilterCriteria(environment.getArgument("filter"));
    final LogsQuery query =
        new LogsQuery.Builder()
            .addresses(filter.getAddresses())
            .topics(
                filter.getTopics().stream()
                    .map(item -> item.stream().map(LogTopic::wrap).collect(Collectors.toList()))
                    .collect(Collectors.toList()))
            .build();
    return blockchain.matchingLogs(Hash.fromHexString(blockHash), query).stream()
        .map(
            log -> {
              final TransactionReceiptWithMetadata receipt =
                  blockchain
                      .transactionReceiptByTransactionHash(log.getTransactionHash())
                      .orElseThrow(
                          () ->
                              new GraphQLException(
                                  "Log found but no transaction associated to it"));
              return new Log(log, receipt);
            })
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
