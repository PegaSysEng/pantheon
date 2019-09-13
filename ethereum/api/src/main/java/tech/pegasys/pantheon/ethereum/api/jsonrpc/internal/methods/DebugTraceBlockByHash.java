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
package tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.methods;

import tech.pegasys.pantheon.ethereum.api.jsonrpc.RpcMethod;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.parameters.TransactionTraceParams;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.processor.BlockTrace;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.processor.BlockTracer;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.results.DebugTraceTransactionResult;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.debug.TraceOptions;
import tech.pegasys.pantheon.ethereum.vm.DebugOperationTracer;

import java.util.Collection;

public class DebugTraceBlockByHash implements JsonRpcMethod {

  private final JsonRpcParameter parameters;
  private final BlockTracer blockTracer;

  public DebugTraceBlockByHash(final JsonRpcParameter parameters, final BlockTracer blockTracer) {
    this.parameters = parameters;
    this.blockTracer = blockTracer;
  }

  @Override
  public String getName() {
    return RpcMethod.DEBUG_TRACE_BLOCK_BY_HASH.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    final Hash blockHash = parameters.required(request.getParams(), 0, Hash.class);
    final TraceOptions traceOptions =
        parameters
            .optional(request.getParams(), 1, TransactionTraceParams.class)
            .map(TransactionTraceParams::traceOptions)
            .orElse(TraceOptions.DEFAULT);

    final Collection<DebugTraceTransactionResult> results =
        blockTracer
            .trace(blockHash, new DebugOperationTracer(traceOptions))
            .map(BlockTrace::getTransactionTraces)
            .map(DebugTraceTransactionResult::of)
            .orElse(null);
    return new JsonRpcSuccessResponse(request.getId(), results);
  }
}
