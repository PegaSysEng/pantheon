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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static tech.pegasys.pantheon.consensus.clique.jsonrpc.CliqueRpcApis.CLIQUE;

import tech.pegasys.pantheon.consensus.clique.CliqueExtraData;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.MiningParameters;
import tech.pegasys.pantheon.ethereum.core.MiningParametersTestBuilder;
import tech.pegasys.pantheon.ethereum.jsonrpc.JsonRpcConfiguration;
import tech.pegasys.pantheon.ethereum.jsonrpc.RpcApi;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.WebSocketConfiguration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.io.Resources;

public class PantheonNodeConfig {
  private final String name;
  private final MiningParameters miningParameters;
  private final JsonRpcConfiguration jsonRpcConfiguration;
  private final WebSocketConfiguration webSocketConfiguration;
  private final Function<Collection<PantheonNode>, Optional<String>> genesisConfig;
  private final boolean devMode;
  private ServerSocket serverSocket;

  private PantheonNodeConfig(
      final String name,
      final MiningParameters miningParameters,
      final JsonRpcConfiguration jsonRpcConfiguration,
      final WebSocketConfiguration webSocketConfiguration,
      final Function<Collection<PantheonNode>, Optional<String>> genesisConfig,
      final boolean devMode) {
    this.name = name;
    this.miningParameters = miningParameters;
    this.jsonRpcConfiguration = jsonRpcConfiguration;
    this.webSocketConfiguration = webSocketConfiguration;
    this.genesisConfig = genesisConfig;
    this.devMode = devMode;
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
    this.genesisConfig = (ignore) -> Optional.empty();
    this.devMode = true;
  }

  private PantheonNodeConfig(final String name, final MiningParameters miningParameters) {
    this.name = name;
    this.miningParameters = miningParameters;
    this.jsonRpcConfiguration = createJsonRpcConfig();
    this.webSocketConfiguration = createWebSocketConfig();
    this.genesisConfig = (ignore) -> Optional.empty();
    this.devMode = true;
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
      final String name, final RpcApi... enabledRpcApis) {
    final JsonRpcConfiguration jsonRpcConfig = createJsonRpcConfig();
    jsonRpcConfig.setRpcApis(asList(enabledRpcApis));
    final WebSocketConfiguration webSocketConfig = createWebSocketConfig();
    webSocketConfig.setRpcApis(asList(enabledRpcApis));

    return new PantheonNodeConfig(
        name, createMiningParameters(false), jsonRpcConfig, webSocketConfig);
  }

  public static PantheonNodeConfig pantheonCliqueMinerNode(final String name) {
    return new PantheonNodeConfig(
        name,
        createMiningParameters(true),
        jsonRpcConfigWithClique(),
        createWebSocketConfig(),
        PantheonNodeConfig::createCliqueGenesisConfig,
        false);
  }

  public static PantheonNodeConfig pantheonCliqueMinerNodeWithValidators(
      final String name, final String... validators) {
    return new PantheonNodeConfig(
        name,
        createMiningParameters(true),
        jsonRpcConfigWithClique(),
        createWebSocketConfig(),
        clusterNodes ->
            createCliqueGenesisConfig(
                clusterNodes
                    .stream()
                    .filter(n -> asList(validators).contains(n.getName()))
                    .collect(toList())),
        false);
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

  private static Optional<String> createCliqueGenesisConfig(
      final Collection<PantheonNode> validators) {
    String genesisTemplate = cliqueGenesisTemplateConfig();
    String cliqueExtraData = encodeCliqueExtraData(validators);
    String genesis = genesisTemplate.replaceAll("%cliqueExtraData%", cliqueExtraData);
    return Optional.of(genesis);
  }

  private static String cliqueGenesisTemplateConfig() {
    try {
      URI uri = Resources.getResource("clique/clique.json").toURI();
      return Resources.toString(uri.toURL(), Charset.defaultCharset());
    } catch (URISyntaxException | IOException e) {
      throw new IllegalStateException("Unable to get test clique genesis config");
    }
  }

  private static String encodeCliqueExtraData(final Collection<PantheonNode> nodes) {
    final List<Address> addresses = nodes.stream().map(PantheonNode::getAddress).collect(toList());
    return CliqueExtraData.createGenesisExtraDataString(addresses);
  }

  private static JsonRpcConfiguration jsonRpcConfigWithClique() {
    final JsonRpcConfiguration jsonRpcConfig = createJsonRpcConfig();
    final List<RpcApi> rpcApis = new ArrayList<>(jsonRpcConfig.getRpcApis());
    rpcApis.add(CLIQUE);
    jsonRpcConfig.setRpcApis(rpcApis);
    return jsonRpcConfig;
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

  public Function<Collection<PantheonNode>, Optional<String>> getGenesisConfig() {
    return genesisConfig;
  }

  public boolean isDevMode() {
    return devMode;
  }
}
