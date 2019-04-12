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
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods;

import tech.pegasys.pantheon.ethereum.jsonrpc.JsonRpcConfiguration;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.WebSocketConfiguration;
import tech.pegasys.pantheon.ethereum.p2p.api.P2PNetwork;
import tech.pegasys.pantheon.ethereum.p2p.wire.PeerInfo;
import tech.pegasys.pantheon.metrics.prometheus.MetricsConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

public class NetServices implements JsonRpcMethod {

  private final Map<String, Map<String, String>> services;

  public NetServices(
      final JsonRpcConfiguration jsonRpcConfiguration,
      final WebSocketConfiguration webSocketConfiguration,
      final P2PNetwork p2pNetwork,
      final MetricsConfiguration metricsConfiguration) {

    services = new LinkedHashMap<String, Map<String, String>>();
    if (jsonRpcConfiguration.isEnabled()) {
      services.put(
          "jsonrpc",
          getServiceDetailsMap(jsonRpcConfiguration.getHost(), jsonRpcConfiguration.getPort()));
    }
    if (webSocketConfiguration.isEnabled()) {
      services.put(
          "ws",
          getServiceDetailsMap(webSocketConfiguration.getHost(), webSocketConfiguration.getPort()));
    }
    if (p2pNetwork.isP2pEnabled()) {
      final PeerInfo peerInfo = p2pNetwork.getLocalPeerInfo();
      p2pNetwork
          .getAdvertisedPeer()
          .ifPresent(
              advertisedPeer -> {
                services.put(
                    "p2p",
                    getServiceDetailsMap(
                        advertisedPeer.getEndpoint().getHost(), peerInfo.getPort()));
              });
    }
    if (metricsConfiguration.isEnabled()) {
      services.put(
          "metrics",
          getServiceDetailsMap(metricsConfiguration.getHost(), metricsConfiguration.getPort()));
    }
  }

  @Override
  public String getName() {
    return "net_services";
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest req) {
    return new JsonRpcSuccessResponse(req.getId(), services);
  }

  private Map<String, String> getServiceDetailsMap(final String host, final int port) {
    Map<String, String> serviceDetails = new LinkedHashMap<String, String>();
    serviceDetails.put("host", host);
    serviceDetails.put("port", String.valueOf((port)));
    return serviceDetails;
  }
}
