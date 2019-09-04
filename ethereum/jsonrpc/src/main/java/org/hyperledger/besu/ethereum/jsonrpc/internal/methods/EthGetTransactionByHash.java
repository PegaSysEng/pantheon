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
package org.hyperledger.besu.ethereum.jsonrpc.internal.methods;

import java.util.Optional;

import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.eth.transactions.PendingTransactions;
import org.hyperledger.besu.ethereum.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.jsonrpc.internal.JsonRpcRequest;
import org.hyperledger.besu.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import org.hyperledger.besu.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import org.hyperledger.besu.ethereum.jsonrpc.internal.response.JsonRpcError;
import org.hyperledger.besu.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import org.hyperledger.besu.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import org.hyperledger.besu.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import org.hyperledger.besu.ethereum.jsonrpc.internal.results.TransactionCompleteResult;
import org.hyperledger.besu.ethereum.jsonrpc.internal.results.TransactionPendingResult;

public class EthGetTransactionByHash implements JsonRpcMethod {

  private final BlockchainQueries blockchain;
  private final PendingTransactions pendingTransactions;
  private final JsonRpcParameter parameters;

  public EthGetTransactionByHash(
      final BlockchainQueries blockchain,
      final PendingTransactions pendingTransactions,
      final JsonRpcParameter parameters) {
    this.blockchain = blockchain;
    this.pendingTransactions = pendingTransactions;
    this.parameters = parameters;
  }

  @Override
  public String getName() {
    return RpcMethod.ETH_GET_TRANSACTION_BY_HASH.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    if (request.getParamLength() != 1) {
      return new JsonRpcErrorResponse(request.getId(), JsonRpcError.INVALID_PARAMS);
    }
    final Hash hash = parameters.required(request.getParams(), 0, Hash.class);
    final JsonRpcSuccessResponse jsonRpcSuccessResponse =
        new JsonRpcSuccessResponse(request.getId(), getResult(hash));
    return jsonRpcSuccessResponse;
  }

  private Object getResult(final Hash hash) {
    final Optional<Object> transactionPendingResult =
        pendingTransactions.getTransactionByHash(hash).map(TransactionPendingResult::new);
    return transactionPendingResult.orElseGet(
        () -> blockchain.transactionByHash(hash).map(TransactionCompleteResult::new).orElse(null));
  }
}
