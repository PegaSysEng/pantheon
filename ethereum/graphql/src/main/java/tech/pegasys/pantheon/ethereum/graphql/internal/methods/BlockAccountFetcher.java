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
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Account;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Block;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class BlockAccountFetcher implements DataFetcher<Account>, GraphQLMethod {
  private BlockchainQueries blockchain;

  //    private static final Logger LOG = LogManager.getLogger();

  public BlockAccountFetcher(final BlockchainQueries blockchain) {
    this.blockchain = blockchain;
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.ACCOUNT;
  }

  @Override
  public Account get(final DataFetchingEnvironment environment) {
    final String stateRoot = ((Block) environment.getSource()).getStateRoot();
    if (environment.containsArgument("address")) {
      Address address = environment.getArgument("address");
      return getAccount(stateRoot, address);
    }
    return null;
  }

  private Account getAccount(final String stateRoot, final Address address) {
    return blockchain
        .getWorldStateArchive()
        .get(Hash.fromHexString(stateRoot))
        .map(worldState -> new Account(worldState.get(address)))
        .orElse(new Account(address));
  }
}
