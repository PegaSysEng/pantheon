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
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Log;

import graphql.schema.DataFetchingEnvironment;

public class LogAccountFetcher extends AbstractAccountFetcher {

  public LogAccountFetcher(final BlockchainQueries blockchain) {
    super(blockchain);
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.LOG_ACCOUNT;
  }

  @Override
  public Account get(final DataFetchingEnvironment environment) {
    Hash transactionHash =
        Hash.fromHexString(((Log) environment.getSource()).getTransaction().getHash());
    Hash stateRoot = resolveStateRootFromBlockInEnvironment(environment);
    Address contractAddress =
        blockchain
            .transactionReceiptByTransactionHash(transactionHash)
            .map(
                t ->
                    t.getTransaction()
                        .getTo()
                        .orElse(t.getTransaction().contractAddress().orElse(null)))
            .orElse(null);

    return getAccountOrNull(stateRoot, contractAddress);
  }
}
