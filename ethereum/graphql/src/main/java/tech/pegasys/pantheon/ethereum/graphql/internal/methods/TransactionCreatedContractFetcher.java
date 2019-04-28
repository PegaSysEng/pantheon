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

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockWithMetadata;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.TransactionWithMetadata;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Account;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Transaction;

import java.util.Optional;

public class TransactionCreatedContractFetcher  extends AbstractAccountFetcher {
  private BlockchainQueries blockchain;

  public TransactionCreatedContractFetcher(final BlockchainQueries blockchain) {
    super(blockchain);
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.TRANSACTION_CREATED_CONTRACT;
  }

  @Override
  public Account get(final DataFetchingEnvironment environment) {
    return resolveOrNull(environment);
  }
}
