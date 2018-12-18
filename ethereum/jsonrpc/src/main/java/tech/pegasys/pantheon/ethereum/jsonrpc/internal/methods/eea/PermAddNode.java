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
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.eea;

import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethod;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.StringParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.p2p.api.P2PNetwork;
import tech.pegasys.pantheon.ethereum.p2p.peers.DefaultPeer;

public class PermAddNode implements JsonRpcMethod {

  private final P2PNetwork p2pNetwork;
  private final JsonRpcParameter parameters;

  public PermAddNode(final P2PNetwork p2pNetwork, final JsonRpcParameter parameters) {
    this.p2pNetwork = p2pNetwork;
    this.parameters = parameters;
  }

  @Override
  public String getName() {
    return "perm_addNode";
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest req) {
    final StringParameter filter = parameters.required(req.getParams(), 0, StringParameter.class);
    DefaultPeer node = DefaultPeer.fromURI(filter.getValue());
    boolean result = p2pNetwork.getNodeWhitelistController().addNode(node);
    if (result) {
      return new JsonRpcSuccessResponse(req.getId(), new Object[] {});
    } else {
      return new JsonRpcErrorResponse(req.getId(), JsonRpcError.INVALID_PARAMS);
    }
  }
}
