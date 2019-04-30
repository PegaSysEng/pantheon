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
import tech.pegasys.pantheon.ethereum.graphql.internal.request.FilterCriteria;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Log;

import java.util.ArrayList;
import java.util.stream.Collectors;

import graphql.GraphQLException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class QueryLogsFetcher implements DataFetcher<ArrayList<Log>>, GraphQLMethod {

  private BlockchainQueries blockchain;

  public QueryLogsFetcher(final BlockchainQueries blockchain) {

    this.blockchain = blockchain;
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.LOGS;
  }

  @Override
  public ArrayList<Log> get(final DataFetchingEnvironment environment) {
    FilterCriteria filter = new FilterCriteria(environment.getArgument("filter"));

    if (filter.getFromBlock().compareTo(filter.getToBlock()) > 0) {
      throw new GraphQLException(
          "fromBlock ("
              + filter.getFromBlock()
              + ") cannot be greater that toBlock ("
              + filter.getToBlock()
              + ")");
    }

    return blockchain
        .matchingLogs(filter.getFromBlock().longValue(), filter.getToBlock().longValue(), filter)
        .stream()
        .map(
            log ->
                new Log(
                    log,
                    blockchain
                        .transactionReceiptByTransactionHash(log.getTransactionHash())
                        .orElse(null)))
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
