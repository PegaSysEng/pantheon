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
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.permissioning;

import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethod;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.p2p.api.P2PNetwork;
import tech.pegasys.pantheon.ethereum.p2p.peers.Peer;

import java.util.List;
import java.util.stream.Collectors;

public class PermGetNodesWhitelist implements JsonRpcMethod {

  private final P2PNetwork p2pNetwork;

  public PermGetNodesWhitelist(final P2PNetwork p2pNetwork) {
    this.p2pNetwork = p2pNetwork;
  }

  @Override
  public String getName() {
    return "perm_getNodesWhitelist";
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest req) {
    if (p2pNetwork.getNodeWhitelistController().nodeWhitelistSet()) {
      List<Peer> nodesWhitelist = p2pNetwork.getNodeWhitelistController().getNodesWhitelist();

      // TODO: The endpoint stuff is a placeholder and isn't returning the value we need.  Need to
      // convert to enode URI.
      List<String> collect =
          nodesWhitelist
              .parallelStream()
              .map(s -> s.getEndpoint().toString())
              .collect(Collectors.toList());
      return new JsonRpcSuccessResponse(req.getId(), collect);
    } else {
      return new JsonRpcErrorResponse(req.getId(), JsonRpcError.NODE_WHITELIST_NOT_SET);
    }
  }
}
