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

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.ProcessableBlockHeader;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Account;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Block;

import graphql.schema.DataFetchingEnvironment;

public class BlockMinerFetcher extends AbstractAccountFetcher {

  public BlockMinerFetcher(final BlockchainQueries blockchain) {
    super(blockchain);
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.MINER;
  }

  @Override
  public Account get(final DataFetchingEnvironment environment) {
    final long blockNumber = ((Block) environment.getSource()).getNumber().longValue();

    final Address address =
        blockchain
            .getBlockHeaderByNumber(blockNumber)
            .map(ProcessableBlockHeader::getCoinbase)
            .orElse(null);

    final Hash stateRoot = resolveStateRootFromBlockInEnvironment(environment);

    return getAccount(stateRoot, address);
  }
}
