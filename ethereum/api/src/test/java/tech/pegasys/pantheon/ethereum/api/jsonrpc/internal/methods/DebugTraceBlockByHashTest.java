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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.processor.BlockTrace;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.processor.BlockTracer;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.processor.TransactionTrace;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.core.Gas;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.debug.TraceFrame;
import tech.pegasys.pantheon.ethereum.mainnet.TransactionProcessor;
import tech.pegasys.pantheon.ethereum.vm.ExceptionalHaltReason;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.Test;

public class DebugTraceBlockByHashTest {

  private final JsonRpcParameter parameters = new JsonRpcParameter();
  private final BlockTracer blockTracer = mock(BlockTracer.class);
  private final DebugTraceBlockByHash debugTraceBlockByHash =
      new DebugTraceBlockByHash(parameters, blockTracer);

  private final Hash blockHash =
      Hash.fromHexString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

  @Test
  public void nameShouldBeDebugTraceBlockByHash() {
    assertEquals("debug_traceBlockByHash", debugTraceBlockByHash.getName());
  }

  @Test
  public void shouldReturnCorrectResponse() {
    final Object[] params = new Object[] {blockHash};
    final JsonRpcRequest request = new JsonRpcRequest("2.0", "debug_traceBlockByHash", params);

    final TraceFrame traceFrame =
        new TraceFrame(
            12,
            "NONE",
            Gas.of(45),
            Optional.of(Gas.of(56)),
            2,
            EnumSet.noneOf(ExceptionalHaltReason.class),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    final TransactionProcessor.Result transaction1Result = mock(TransactionProcessor.Result.class);
    final TransactionProcessor.Result transaction2Result = mock(TransactionProcessor.Result.class);

    final TransactionTrace transaction1Trace = mock(TransactionTrace.class);
    final TransactionTrace transaction2Trace = mock(TransactionTrace.class);

    BlockTrace blockTrace = new BlockTrace(Arrays.asList(transaction1Trace, transaction2Trace));

    when(transaction1Trace.getTraceFrames()).thenReturn(Arrays.asList(traceFrame));
    when(transaction2Trace.getTraceFrames()).thenReturn(Arrays.asList(traceFrame));
    when(transaction1Trace.getResult()).thenReturn(transaction1Result);
    when(transaction2Trace.getResult()).thenReturn(transaction2Result);
    when(transaction1Result.getOutput()).thenReturn(BytesValue.fromHexString("1234"));
    when(transaction2Result.getOutput()).thenReturn(BytesValue.fromHexString("1234"));
    when(blockTracer.trace(eq(blockHash), any())).thenReturn(Optional.of(blockTrace));

    final JsonRpcSuccessResponse response =
        (JsonRpcSuccessResponse) debugTraceBlockByHash.response(request);
    final Collection<?> result = (Collection<?>) response.getResult();
    assertEquals(2, result.size());
  }
}
