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

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class BlockOmmerAtFetcher implements DataFetcher<Ommer>, GraphQLMethod {

  private BlockchainQueries blockchain;

  public BlockOmmerAtFetcher(final BlockchainQueries blockchain) {
    this.blockchain = blockchain;
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.OMMERAT;
  }

  @Override
  public Ommer get(final DataFetchingEnvironment environment) {
    final long blockNumber = ((Block) environment.getSource()).getNumber().longValue();
    if (environment.containsArgument("ommerAt")) {
      Integer ommerAt = environment.getArgument("ommerAt");
      return getOmmerAt(blockNumber, ommerAt);
    }
    return null;
  }

  private Ommer getOmmerAt(final long blockNumber, final Integer ommerAt) {
    try {
      return blockchain.getOmmer(blockNumber, ommerAt).map(Ommer::new).orElse(null);
    } catch (NullPointerException e) {
      return null;
    }
  }
}
