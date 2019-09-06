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
package tech.pegasys.pantheon.services;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import tech.pegasys.pantheon.plugin.services.RpcEndpointService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RPCEndpointServiceImpl implements RpcEndpointService {

  private final Map<String, Function<List<String>, ?>> rpcMethods = new HashMap<>();

  @Override
  public <T> void registerRPCEndpoint(
      final String namespace, final String functionName, final Function<List<String>, T> function) {
    checkArgument(namespace.matches("\\p{Alnum}+"), "Namespace must be only alpha numeric");
    checkArgument(functionName.matches("\\p{Alnum}+"), "Function Name must be only alpha numeric");
    checkNotNull(function);

    rpcMethods.put(namespace + "_" + functionName, function);
  }

  public Map<String, Function<List<String>, ?>> getRpcMethods() {
    return rpcMethods;
  }
}
