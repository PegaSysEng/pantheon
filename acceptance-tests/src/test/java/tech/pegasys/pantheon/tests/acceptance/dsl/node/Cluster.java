package tech.pegasys.pantheon.tests.acceptance.dsl.node;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.tests.acceptance.dsl.WaitUtils;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cluster implements AutoCloseable {

  private final Map<String, PantheonNode> nodes = new HashMap<>();
  private final PantheonNodeRunner pantheonNodeRunner = PantheonNodeRunner.instance();

  public void start(final PantheonNode... nodes) {
    this.nodes.clear();

    final List<String> bootNodes = new ArrayList<>();

    for (final PantheonNode node : nodes) {
      this.nodes.put(node.getName(), node);
      bootNodes.add(node.enodeUrl());
    }

    for (final PantheonNode node : nodes) {
      node.bootnodes(bootNodes);
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
            config.getWebSocketConfiguration());
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
