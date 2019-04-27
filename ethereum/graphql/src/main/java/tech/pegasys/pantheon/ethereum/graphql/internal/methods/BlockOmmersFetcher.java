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
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Block;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Ommer;

import java.util.ArrayList;
import java.util.stream.IntStream;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class BlockOmmersFetcher implements DataFetcher<ArrayList<Ommer>>, GraphQLMethod {

  private BlockchainQueries blockchain;

  //    private static final Logger LOG = LogManager.getLogger();

  public BlockOmmersFetcher(final BlockchainQueries blockchain) {
    this.blockchain = blockchain;
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.OMMERS;
  }

  @Override
  public ArrayList<Ommer> get(final DataFetchingEnvironment environment) {
    final long childNumber = ((Block) environment.getSource()).getNumber().longValue();
    return getOmmersByNumber(childNumber - 1);
  }

  private ArrayList<Ommer> getOmmersByNumber(final Long number) {
    try {
      final ArrayList<Ommer> ommers = new ArrayList<>();
      blockchain
          .getOmmerCount(number)
          .ifPresent(
              ommerCount ->
                  IntStream.range(0, ommerCount)
                      .forEach(
                          i ->
                              blockchain
                                  .getOmmer(number, i)
                                  .ifPresent(header -> ommers.add(new Ommer(header)))));
      return ommers;

    } catch (NullPointerException e) {
      return null;
    }
  }
}
