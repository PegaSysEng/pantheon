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
package org.hyperledger.besu.ethereum.jsonrpc.internal.methods;

import org.hyperledger.besu.ethereum.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.jsonrpc.internal.JsonRpcRequest;
import org.hyperledger.besu.ethereum.core.Block;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.debug.TraceOptions;
import org.hyperledger.besu.ethereum.jsonrpc.internal.parameters.BlockParameter;
import org.hyperledger.besu.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import org.hyperledger.besu.ethereum.jsonrpc.internal.parameters.TraceTypeParameter;
import org.hyperledger.besu.ethereum.jsonrpc.internal.processor.BlockTrace;
import org.hyperledger.besu.ethereum.jsonrpc.internal.processor.BlockTracer;
import org.hyperledger.besu.ethereum.jsonrpc.internal.processor.TransactionTrace;
import org.hyperledger.besu.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import org.hyperledger.besu.ethereum.vm.DebugOperationTracer;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TraceReplayBlockTransactions extends AbstractBlockParameterMethod {

  private final BlockTracer blockTracer;

  public TraceReplayBlockTransactions(
      final JsonRpcParameter parameters,
      final BlockTracer blockTracer,
      final BlockchainQueries queries) {
    super(queries, parameters);
    this.blockTracer = blockTracer;
  }

  @Override
  public String getName() {
    return RpcMethod.TRACE_REPLAY_BLOCK_TRANSACTIONS.getMethodName();
  }

  @Override
  protected BlockParameter blockParameter(final JsonRpcRequest request) {
    return getParameters().required(request.getParams(), 0, BlockParameter.class);
  }

  @Override
  protected Object resultByBlockNumber(final JsonRpcRequest request, final long blockNumber) {
    final TraceTypeParameter traceTypeParameter =
        getParameters().required(request.getParams(), 1, TraceTypeParameter.class);

    if (blockNumber == BlockHeader.GENESIS_BLOCK_NUMBER) {
      // Nothing to trace for the genesis block
      return emptyResult();
    }

    return getBlockchainQueries()
        .getBlockchain()
        .getBlockByNumber(blockNumber)
        .map((block) -> traceBlock(block, traceTypeParameter))
        .orElse(null);
  }

  private Object traceBlock(final Block block, final TraceTypeParameter traceTypeParameter) {
    // TODO: generate options based on traceTypeParameter
    final TraceOptions traceOptions = TraceOptions.DEFAULT;

    return blockTracer
        .trace(block, new DebugOperationTracer(traceOptions))
        .map(BlockTrace::getTransactionTraces)
        .map((traces) -> formatTraces(traces, traceTypeParameter))
        .orElse(null);
  }

  private List<JsonNode> formatTraces(
      final List<TransactionTrace> traces, final TraceTypeParameter traceTypeParameter) {
    return traces.stream()
        .map((trace) -> formatTrace(trace, traceTypeParameter))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unused")
  private JsonNode formatTrace(
      final TransactionTrace trace, final TraceTypeParameter traceTypeParameter) {
    // TODO: generate result
    ObjectMapper mapper = new ObjectMapper();
    return mapper.createObjectNode();
  }

  private Object emptyResult() {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.createArrayNode();
  }
}
