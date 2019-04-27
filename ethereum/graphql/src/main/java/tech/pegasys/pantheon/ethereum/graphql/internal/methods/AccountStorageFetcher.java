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
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.Bytes32Backed;

import java.util.Optional;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class AccountStorageFetcher implements DataFetcher<Bytes32>, GraphQLMethod {
  private BlockchainQueries blockchain;

  //    private static final Logger LOG = LogManager.getLogger();

  public AccountStorageFetcher(final BlockchainQueries blockchain) {
    this.blockchain = blockchain;
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.STORAGE;
  }

  @Override
  public Bytes32 get(final DataFetchingEnvironment environment) {
    final Address address = ((Account) environment.getSource()).getAddress();
    if (environment.containsArgument("slot")) {
      Bytes32 slot = environment.getArgument("slot");
      return getStorageAt(slot, address);
    }
    return null;
  }

  private Bytes32 getStorageAt(final Bytes32 slot, final Address address) {
    Optional<Hash> stateRootOptional =
        blockchain.latestBlockWithTxHashes().map(item -> item.getHeader().getStateRoot());
    return stateRootOptional
        .map(
            stateRoot ->
                blockchain
                    .getWorldStateArchive()
                    .get(stateRoot)
                    .map(worldState -> worldState.get(address).getStorageValue(slot.asUInt256()))
                    .map(Bytes32Backed::getBytes)
                    .orElse(Bytes32.ZERO))
        .orElse(Bytes32.ZERO);
  }
}
