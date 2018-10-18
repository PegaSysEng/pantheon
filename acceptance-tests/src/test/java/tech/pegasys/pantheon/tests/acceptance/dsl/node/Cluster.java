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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.utils.Convert.Unit;
import tech.pegasys.pantheon.cli.EthNetworkConfig;
import tech.pegasys.pantheon.tests.acceptance.dsl.WaitUtils;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;


public class Cluster implements AutoCloseable {

  private static final Logger LOG = LogManager.getLogger(Cluster.class);
  public static final int NETWORK_ID = 10;

  private final Map<String, PantheonNode> nodes = new HashMap<>();
  private final PantheonNodeRunner pantheonNodeRunner = PantheonNodeRunner.instance();

  public void start(final PantheonNode... nodes) {
    this.nodes.clear();

    final List<String> bootNodes = new ArrayList<>();

    for (final PantheonNode node : nodes) {
      this.nodes.put(node.getName(), node);
      bootNodes.add(node.enodeUrl());
    }

    // TODO create clique genesis file
    // TODO how do we know it's a clique config?

    // TODO provide a config file creator?
    // (nodes[] -> genesis file)
    // need to know it's a clique cluster?
    // only apply to clique nodes?
    // each node could use same factory? cache result if needed to improve performance?
    // need to handle null case?
    // factory to create ethNod

    for (final PantheonNode node : nodes) {
      node.bootnodes(bootNodes);
      Optional<EthNetworkConfig> ethNetworkConfig = node.getGenesisConfig()
          .apply(Arrays.asList(nodes))
          .map(config -> new EthNetworkConfig(() -> config, NETWORK_ID, bootNodes));
      node.ethNetworkConfig(ethNetworkConfig);
      node.start(pantheonNodeRunner);
    }

    for (final PantheonNode node : nodes) {
      awaitPeerDiscovery(node, nodes.length);
    }
  }

  public void stop() {
    for (final PantheonNode node : nodes.values()) {
      node.stop();
    }
    pantheonNodeRunner.shutdown();
  }

  @Override
  public void close() {
    for (final PantheonNode node : nodes.values()) {
      node.close();
    }
    pantheonNodeRunner.shutdown();
  }

  public PantheonNode create(final PantheonNodeConfig config) throws IOException {
    config.initSocket();
    final PantheonNode node =
        new PantheonNode(
            config.getName(),
            config.getSocketPort(),
            config.getMiningParameters(),
            config.getJsonRpcConfiguration(),
            config.getWebSocketConfiguration(),
            config.isDevMode(),
            config.getGenesisConfig());
    config.closeSocket();
    return node;
  }

  private void awaitPeerDiscovery(final PantheonNode node, final int nodeCount) {
    if (node.jsonRpcEnabled()) {
      WaitUtils.waitFor(() -> assertThat(node.getPeerCount()).isEqualTo(nodeCount - 1));
    }
  }

  public void verify(final Condition expected) {
    for (final Node node : nodes.values()) {
      expected.verify(node);
    }
  }
}
