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
package tech.pegasys.pantheon.ethereum.graphql.internal.response;

import tech.pegasys.pantheon.ethereum.mainnet.TransactionValidator.TransactionInvalidReason;

public class GraphQLErrorConverter {

  public static GraphQLError convertTransactionInvalidReason(
      final TransactionInvalidReason reason) {
    switch (reason) {
      case NONCE_TOO_LOW:
        return GraphQLError.NONCE_TOO_LOW;
      case INCORRECT_NONCE:
        return GraphQLError.INCORRECT_NONCE;
      case INVALID_SIGNATURE:
        return GraphQLError.INVALID_TRANSACTION_SIGNATURE;
      case INTRINSIC_GAS_EXCEEDS_GAS_LIMIT:
        return GraphQLError.INTRINSIC_GAS_EXCEEDS_LIMIT;
      case UPFRONT_COST_EXCEEDS_BALANCE:
        return GraphQLError.TRANSACTION_UPFRONT_COST_EXCEEDS_BALANCE;
      case EXCEEDS_BLOCK_GAS_LIMIT:
        return GraphQLError.EXCEEDS_BLOCK_GAS_LIMIT;
      case TX_SENDER_NOT_AUTHORIZED:
        return GraphQLError.TX_SENDER_NOT_AUTHORIZED;
      case CHAIN_HEAD_WORLD_STATE_NOT_AVAILABLE:
        return GraphQLError.CHAIN_HEAD_WORLD_STATE_NOT_AVAILABLE;

      default:
        return GraphQLError.INVALID_PARAMS;
    }
  }
}
