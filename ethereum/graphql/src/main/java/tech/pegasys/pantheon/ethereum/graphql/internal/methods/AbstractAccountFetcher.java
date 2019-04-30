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
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockWithMetadata;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.TransactionWithMetadata;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Account;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Transaction;

import java.util.Optional;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

abstract class AbstractAccountFetcher implements DataFetcher<Account>, GraphQLMethod {

  final BlockchainQueries blockchain;

  AbstractAccountFetcher(final BlockchainQueries blockchain) {
    this.blockchain = blockchain;
  }

  Address resolveFrom(final DataFetchingEnvironment environment) {
    final Hash hash = Hash.fromHexString(((Transaction) environment.getSource()).getHash());
    final Optional<TransactionWithMetadata> transaction = blockchain.transactionByHash(hash);
    return transaction.map(t -> t.getTransaction().getSender()).orElse(null);
  }

  Optional<Address> resolveTo(final DataFetchingEnvironment environment) {
    final Hash hash = Hash.fromHexString(((Transaction) environment.getSource()).getHash());
    final Optional<TransactionWithMetadata> transaction = blockchain.transactionByHash(hash);
    return transaction.map(t -> t.getTransaction().getTo()).orElse(null);
  }

  Optional<Address> resolveContractAddress(final DataFetchingEnvironment environment) {
    final Hash hash = Hash.fromHexString(((Transaction) environment.getSource()).getHash());
    final Optional<TransactionWithMetadata> transaction = blockchain.transactionByHash(hash);
    return transaction.map(t -> t.getTransaction().contractAddress()).orElse(null);
  }

  Hash resolveStateRootFromBlockInEnvironment(final DataFetchingEnvironment environment) {
    final Optional<BlockWithMetadata<Hash, Hash>> block;
    if (environment.containsArgument("block")) {
      block = blockchain.blockByNumberWithTxHashes(environment.getArgument("block"));
    } else {
      block = blockchain.blockByNumberWithTxHashes(blockchain.headBlockNumber());
    }
    return block.map(b -> b.getHeader().getStateRoot()).orElse(null);
  }

  Account getAccount(final Hash stateRoot, final Address address) {
    return maybeAccount(stateRoot, address).orElse(new Account(address));
  }

  Account getAccountOrNull(final Hash stateRoot, final Address address) {
    return maybeAccount(stateRoot, address).orElse(new Account(address));
  }

  Optional<Account> maybeAccount(final Hash stateRoot, final Address address) {
    return blockchain
        .getWorldStateArchive()
        .get(stateRoot)
        .map(worldState -> new Account(worldState.get(address)));
  }
}
