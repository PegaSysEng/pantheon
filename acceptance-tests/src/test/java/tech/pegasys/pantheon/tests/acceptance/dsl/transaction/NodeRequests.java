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
package tech.pegasys.pantheon.tests.acceptance.dsl.transaction;

import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.admin.AdminJsonRpcRequestFactory;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.clique.CliqueJsonRpcRequestFactory;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eea.EeaJsonRpcRequestFactory;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.http.HttpRequestFactory;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.ibft2.Ibft2JsonRpcRequestFactory;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.net.NetPeerCountTransaction;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.perm.PermissioningJsonRpcRequestFactory;

import java.util.Optional;

import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.websocket.WebSocketService;

public class NodeRequests {

  private final JsonRpc2_0Web3j netEth;
  private final CliqueJsonRpcRequestFactory clique;
  private final Ibft2JsonRpcRequestFactory ibft;
  private final PermissioningJsonRpcRequestFactory perm;
  private final AdminJsonRpcRequestFactory admin;
  private final EeaJsonRpcRequestFactory eea;
  private final NetPeerCountTransaction.CustomNetJsonRpcRequestFactory customNet;
  private final Optional<WebSocketService> websocketService;
  private final HttpRequestFactory http;

  public NodeRequests(
      final JsonRpc2_0Web3j netEth,
      final CliqueJsonRpcRequestFactory clique,
      final Ibft2JsonRpcRequestFactory ibft,
      final PermissioningJsonRpcRequestFactory perm,
      final AdminJsonRpcRequestFactory admin,
      final EeaJsonRpcRequestFactory eea,
      final NetPeerCountTransaction.CustomNetJsonRpcRequestFactory customNet,
      final Optional<WebSocketService> websocketService,
      final HttpRequestFactory http) {
    this.netEth = netEth;
    this.clique = clique;
    this.ibft = ibft;
    this.perm = perm;
    this.admin = admin;
    this.eea = eea;
    this.customNet = customNet;
    this.websocketService = websocketService;
    this.http = http;
  }

  public JsonRpc2_0Web3j eth() {
    return netEth;
  }

  public JsonRpc2_0Web3j net() {
    return netEth;
  }

  public CliqueJsonRpcRequestFactory clique() {
    return clique;
  }

  public Ibft2JsonRpcRequestFactory ibft() {
    return ibft;
  }

  public PermissioningJsonRpcRequestFactory perm() {
    return perm;
  }

  public AdminJsonRpcRequestFactory admin() {
    return admin;
  }

  public NetPeerCountTransaction.CustomNetJsonRpcRequestFactory customNet() {
    return customNet;
  }

  public EeaJsonRpcRequestFactory eea() {
    return eea;
  }

  public HttpRequestFactory http() {
    return http;
  }

  public void shutdown() {
    netEth.shutdown();
    websocketService.ifPresent(WebSocketService::close);
  }
}
