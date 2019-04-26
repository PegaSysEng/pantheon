/*
 * Copyright 2018 ConsenSys AG.
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
import tech.pegasys.pantheon.util.bytes.Bytes32;

import java.math.BigInteger;
import java.util.Optional;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class QueryBlockFetcher extends AbstractBlockFetcher implements DataFetcher<Block> {

  //    private static final Logger LOG = LogManager.getLogger();

  public QueryBlockFetcher(final BlockchainQueries blockchain) {
    super(blockchain);
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.BLOCK;
  }

  @Override
  public Block get(final DataFetchingEnvironment environment) {

    if (environment.containsArgument("hash")) {
      Bytes32 hash = getHash(environment);
      return getBlockByHash(hash);
    } else if (environment.containsArgument("number")) {
      BigInteger number = getNumber(environment);
      return getBlockByNumber(number.longValue());
    } else {
      return getLatestBlock();
    }
  }

  private Bytes32 getHash(final DataFetchingEnvironment environment) {
    return (Bytes32) Optional.of(environment.getArgument("hash")).orElse(null);
  }

  private BigInteger getNumber(final DataFetchingEnvironment environment) {
    return (BigInteger) Optional.of(environment.getArgument("number")).orElse(null);
  }
}
