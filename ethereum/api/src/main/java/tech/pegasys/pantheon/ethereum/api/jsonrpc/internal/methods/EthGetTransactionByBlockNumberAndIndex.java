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
package tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.methods;

import tech.pegasys.pantheon.ethereum.api.TransactionWithMetadata;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.RpcMethod;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.parameters.BlockParameter;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.parameters.UnsignedIntParameter;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.results.TransactionCompleteResult;

import java.util.Optional;

public class EthGetTransactionByBlockNumberAndIndex extends AbstractBlockParameterMethod {

  public EthGetTransactionByBlockNumberAndIndex(
      final BlockchainQueries blockchain, final JsonRpcParameter parameters) {
    super(blockchain, parameters);
  }

  @Override
  public String getName() {
    return RpcMethod.ETH_GET_TRANSACTION_BY_BLOCK_NUMBER_AND_INDEX.getMethodName();
  }

  @Override
  protected BlockParameter blockParameter(final JsonRpcRequest request) {
    return getParameters().required(request.getParams(), 0, BlockParameter.class);
  }

  @Override
  protected Object resultByBlockNumber(final JsonRpcRequest request, final long blockNumber) {
    final int index =
        getParameters().required(request.getParams(), 1, UnsignedIntParameter.class).getValue();
    final Optional<TransactionWithMetadata> transactionWithMetadata =
        getBlockchainQueries().transactionByBlockNumberAndIndex(blockNumber, index);
    return transactionWithMetadata.map(TransactionCompleteResult::new).orElse(null);
  }
}
