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
package tech.pegasys.pantheon.tests.acceptance.dsl.node;

import static tech.pegasys.pantheon.cli.EthNetworkConfig.mainnet;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import tech.pegasys.pantheon.cli.EthNetworkConfig;
import tech.pegasys.pantheon.ethereum.blockcreation.MiningParameters;
import tech.pegasys.pantheon.ethereum.core.MiningParametersTestBuilder;
import tech.pegasys.pantheon.ethereum.jsonrpc.JsonRpcConfiguration;
import tech.pegasys.pantheon.ethereum.jsonrpc.JsonRpcConfiguration.RpcApis;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.WebSocketConfiguration;

public class PantheonNodeConfig {
  private static final int NETWORK_ID = 10;
  private final String name;
  private final MiningParameters miningParameters;
  private final JsonRpcConfiguration jsonRpcConfiguration;
  private final WebSocketConfiguration webSocketConfiguration;
  private final EthNetworkConfig ethNetworkConfig;
  private final boolean devMode;
  private final Optional<Path> keyPath;
  private ServerSocket serverSocket;

  private PantheonNodeConfig(
      final String name,
      final MiningParameters miningParameters,
      final JsonRpcConfiguration jsonRpcConfiguration,
      final WebSocketConfiguration webSocketConfiguration,
      final EthNetworkConfig ethNetworkConfig,
      final boolean devMode,
      final Path keyPath
  ) {
    this.name = name;
    this.miningParameters = miningParameters;
    this.jsonRpcConfiguration = jsonRpcConfiguration;
    this.webSocketConfiguration = webSocketConfiguration;
    this.ethNetworkConfig = ethNetworkConfig;
    this.devMode = devMode;
    this.keyPath = Optional.of(keyPath);
  }

  private PantheonNodeConfig(
      final String name,
      final MiningParameters miningParameters,
      final JsonRpcConfiguration jsonRpcConfiguration,
      final WebSocketConfiguration webSocketConfiguration) {
    this.name = name;
    this.miningParameters = miningParameters;
    this.jsonRpcConfiguration = jsonRpcConfiguration;
    this.webSocketConfiguration = webSocketConfiguration;
    this.ethNetworkConfig = createEthNetworkConfig();
    this.devMode = true;
    this.keyPath = Optional.empty();
  }

  private PantheonNodeConfig(final String name, final MiningParameters miningParameters) {
    this.name = name;
    this.miningParameters = miningParameters;
    this.jsonRpcConfiguration = createJsonRpcConfig();
    this.webSocketConfiguration = createWebSocketConfig();
    this.ethNetworkConfig = createEthNetworkConfig();
    this.devMode = true;
    this.keyPath = Optional.empty();
  }

  private static MiningParameters createMiningParameters(final boolean miner) {
    return new MiningParametersTestBuilder().enabled(miner).build();
  }

  public static PantheonNodeConfig pantheonMinerNode(final String name) {
    return new PantheonNodeConfig(name, createMiningParameters(true));
  }

  public static PantheonNodeConfig pantheonNode(final String name) {
    return new PantheonNodeConfig(name, createMiningParameters(false));
  }

  public static PantheonNodeConfig pantheonRpcDisabledNode(final String name) {
    return new PantheonNodeConfig(
        name,
        createMiningParameters(false),
        JsonRpcConfiguration.createDefault(),
        WebSocketConfiguration.createDefault());
  }

  public static PantheonNodeConfig patheonNodeWithRpcApis(
      final String name, final RpcApis... enabledRpcApis) {
    final JsonRpcConfiguration jsonRpcConfig = createJsonRpcConfig();
    jsonRpcConfig.setRpcApis(Arrays.asList(enabledRpcApis));
    final WebSocketConfiguration webSocketConfig = createWebSocketConfig();
    webSocketConfig.setRpcApis(Arrays.asList(enabledRpcApis));

    return new PantheonNodeConfig(
        name, createMiningParameters(false), jsonRpcConfig, webSocketConfig);
  }

  public static PantheonNodeConfig pantheonCliqueNode(final String name, final String cliqueGenesis, final String keyFile) throws URISyntaxException {
    final URI genesisConfig = Resources.getResource(cliqueGenesis).toURI();
    final Path keyPath = Paths.get(Resources.getResource(keyFile).toURI());
    final EthNetworkConfig ethNetworkConfig = new EthNetworkConfig.Builder(EthNetworkConfig.defaultConfig())
        .setGenesisConfig(genesisConfig).build();
    return new PantheonNodeConfig(
        name,
        createMiningParameters(true),
        createJsonRpcConfig(),
        createWebSocketConfig(),
        ethNetworkConfig,
        false,
        keyPath
    );
  }

  private static JsonRpcConfiguration createJsonRpcConfig() {
    final JsonRpcConfiguration config = JsonRpcConfiguration.createDefault();
    config.setEnabled(true);
    config.setPort(0);
    return config;
  }

  private static WebSocketConfiguration createWebSocketConfig() {
    final WebSocketConfiguration config = WebSocketConfiguration.createDefault();
    config.setEnabled(true);
    config.setPort(0);
    return config;
  }

  private EthNetworkConfig createEthNetworkConfig() {
    return new EthNetworkConfig.Builder(mainnet()).setNetworkId(NETWORK_ID).build();
  }

  public void initSocket() throws IOException {
    serverSocket = new ServerSocket(0);
  }

  public void closeSocket() throws IOException {
    serverSocket.close();
  }

  public int getSocketPort() {
    return serverSocket.getLocalPort();
  }

  public String getName() {
    return name;
  }

  public MiningParameters getMiningParameters() {
    return miningParameters;
  }

  public JsonRpcConfiguration getJsonRpcConfiguration() {
    return jsonRpcConfiguration;
  }

  public WebSocketConfiguration getWebSocketConfiguration() {
    return webSocketConfiguration;
  }

  public EthNetworkConfig getEthNetworkConfig() {
    return ethNetworkConfig;
  }

  public boolean isDevMode() {
    return devMode;
  }

  public Optional<Path> getKeyPath() {
    return keyPath;
  }
}
