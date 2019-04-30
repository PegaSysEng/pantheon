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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import com.google.common.collect.Lists;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class QueryBlocksFetcher extends AbstractBlockFetcher implements DataFetcher<List<Block>> {

  public QueryBlocksFetcher(final BlockchainQueries blockchain) {
    super(blockchain);
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.BLOCKS;
  }

  @Override
  public ArrayList<Block> get(final DataFetchingEnvironment environment) {
    final BigInteger from = getNumber(environment, "from");
    if (environment.containsArgument("to")) {
      return getBlocks(from.longValue(), getNumber(environment, "to").longValue());
    } else {
      return getBlocks(from.longValue(), blockchain.headBlockNumber());
    }
  }

  private BigInteger getNumber(final DataFetchingEnvironment environment, final String key) {
    return (BigInteger) Optional.of(environment.getArgument(key)).orElse(null);
  }

  private ArrayList<Block> getBlocks(final long from, final long to) {
    if (to < from) {
      return Lists.newArrayList();
    }
    final ArrayList<Block> blocks = new ArrayList<>(((int) (to - from + 1)));
    LongStream.range(from, to).forEach(i -> blocks.add(getBlockByNumber(i)));
    return blocks;
  }
}
