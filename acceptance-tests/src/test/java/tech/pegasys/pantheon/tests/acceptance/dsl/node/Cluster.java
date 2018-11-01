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

import tech.pegasys.pantheon.tests.acceptance.dsl.WaitUtils;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Cluster implements AutoCloseable {

  private final List<Node> nodes = new ArrayList<>();
  private final PantheonNodeRunner pantheonNodeRunner = PantheonNodeRunner.instance();

  public void start(final Node... nodes) {
    this.nodes.clear();

    final List<String> bootNodes = bootNodes();

    for (final Node node : nodes) {
      node.bootnodes(bootNodes);
      node.start(pantheonNodeRunner);
    }

    for (final Node node : nodes) {
      awaitPeerDiscovery(node, nodes.length);
    }
  }

  private List<String> bootNodes() {
    final List<String> bootNodes = new ArrayList<>();
    for (final Node node : nodes) {
      bootNodes.add(node.enodeUrl());
    }

    return bootNodes;
  }

  public void stop() {
    for (final Node node : nodes) {
      node.stop();
    }
    pantheonNodeRunner.shutdown();
  }

  @Override
  public void close() {
    for (final Node node : nodes) {
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
            config.getWebSocketConfiguration());
    config.closeSocket();
    return node;
  }

  private void awaitPeerDiscovery(final Node node, final int nodeCount) {
    // Todo use conditional?
    if (node.jsonRpcEnabled()) {
      WaitUtils.waitFor(() -> assertThat(node.getPeerCount()).isEqualTo(nodeCount - 1));
    }
  }

  public void verify(final Condition expected) {
    for (final Node node : nodes) {
      expected.verify(node);
    }
  }
}
