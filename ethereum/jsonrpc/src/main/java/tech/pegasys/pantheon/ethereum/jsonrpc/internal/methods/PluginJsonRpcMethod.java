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
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods;

import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PluginJsonRpcMethod implements JsonRpcMethod {

  private final String name;
  private final Function<List<String>, ?> function;

  public PluginJsonRpcMethod(final String name, final Function<List<String>, ?> function) {
    this.name = name;
    this.function = function;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    try {
      return new JsonRpcSuccessResponse(
          request.getId(),
          function.apply(
              Arrays.stream(request.getParams())
                  .map(o -> o == null ? null : o.toString())
                  .collect(Collectors.toList())));
    } catch (final RuntimeException re) {
      return new JsonRpcErrorResponse(request.getId(), JsonRpcError.INTERNAL_ERROR);
    }
  }
}
