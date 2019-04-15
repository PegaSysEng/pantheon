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

import com.google.common.collect.ImmutableMap;

public class NetServices implements JsonRpcMethod {

  private final ImmutableMap<String, ImmutableMap<String, String>> services;

  public NetServices(
      final JsonRpcConfiguration jsonRpcConfiguration,
      final WebSocketConfiguration webSocketConfiguration,
      final P2PNetwork p2pNetwork,
      final MetricsConfiguration metricsConfiguration) {

    Map<String, ImmutableMap<String, String>> activeServices =
        new LinkedHashMap<String, ImmutableMap<String, String>>();

    if (jsonRpcConfiguration.isEnabled()) {
      activeServices.put(
          "jsonrpc",
          createServiceDetailsMap(jsonRpcConfiguration.getHost(), jsonRpcConfiguration.getPort()));
    }
    if (webSocketConfiguration.isEnabled()) {
      activeServices.put(
          "ws",
          createServiceDetailsMap(
              webSocketConfiguration.getHost(), webSocketConfiguration.getPort()));
    }
    if (p2pNetwork.isP2pEnabled()) {
      final PeerInfo peerInfo = p2pNetwork.getLocalPeerInfo();
      p2pNetwork
          .getAdvertisedPeer()
          .ifPresent(
              advertisedPeer -> {
                activeServices.put(
                    "p2p",
                    createServiceDetailsMap(
                        advertisedPeer.getEndpoint().getHost(), peerInfo.getPort()));
              });
    }
    if (metricsConfiguration.isEnabled()) {
      activeServices.put(
          "metrics",
          createServiceDetailsMap(metricsConfiguration.getHost(), metricsConfiguration.getPort()));
    }

    services =
        ImmutableMap.<String, ImmutableMap<String, String>>builder().putAll(activeServices).build();
  }

  @Override
  public String getName() {
    return "net_services";
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest req) {
    return new JsonRpcSuccessResponse(req.getId(), services);
  }

  private ImmutableMap<String, String> createServiceDetailsMap(final String host, final int port) {
    return ImmutableMap.of("host", host, "port", String.valueOf(port));
  }
}
