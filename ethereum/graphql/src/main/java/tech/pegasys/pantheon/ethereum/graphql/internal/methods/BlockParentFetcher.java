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

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class BlockParentFetcher extends AbstractBlockFetcher implements DataFetcher<Block> {

  //    private static final Logger LOG = LogManager.getLogger();

  public BlockParentFetcher(final BlockchainQueries blockchain) {
    super(blockchain);
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.PARENT;
  }

  @Override
  public Block get(final DataFetchingEnvironment environment) {
    final long childNumber = ((Block) environment.getSource()).getNumber().longValue();
    return childNumber == 0 ? null : getParentByNumber(childNumber - 1);
  }

  private Block getParentByNumber(final Long number) {
    try {
      return getBlockByNumber(number);
    } catch (NullPointerException e) {
      return null;
    }
  }
}
