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

import static tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLErrorConverter.convertTransactionInvalidReason;

import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.eth.transactions.TransactionPool;
import tech.pegasys.pantheon.ethereum.graphql.internal.exception.InvalidGraphQLParameters;
import tech.pegasys.pantheon.ethereum.mainnet.TransactionValidator;
import tech.pegasys.pantheon.ethereum.mainnet.ValidationResult;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.ethereum.rlp.RLPException;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MutationTransactionFetcher implements DataFetcher<Bytes32>, GraphQLMethod {

  private TransactionPool transactionPool;

  private static final Logger LOG = LogManager.getLogger();

  public MutationTransactionFetcher(final TransactionPool transactionPool) {
    this.transactionPool = transactionPool;
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.SEND_RAW_TRANSACTION;
  }

  @Override
  public Bytes32 get(final DataFetchingEnvironment environment) {
    final BytesValue rawTransaction = environment.getArgument("data");

    final Transaction transaction;
    try {
      transaction = decodeRawTransaction(rawTransaction);
    } catch (final InvalidGraphQLParameters e) {
      throw e;
    }

    final ValidationResult<TransactionValidator.TransactionInvalidReason> validationResult =
        transactionPool.addLocalTransaction(transaction);
    return validationResult.either(
        transaction::hash,
        errorReason -> {
          throw new InvalidGraphQLParameters(
              convertTransactionInvalidReason(errorReason).getMessage());
        });
  }

  private Transaction decodeRawTransaction(final BytesValue hash) throws InvalidGraphQLParameters {
    try {
      return Transaction.readFrom(RLP.input(hash));
    } catch (final IllegalArgumentException | RLPException e) {
      LOG.debug(e);
      throw new InvalidGraphQLParameters(e.getMessage(), e);
    }
  }
}
