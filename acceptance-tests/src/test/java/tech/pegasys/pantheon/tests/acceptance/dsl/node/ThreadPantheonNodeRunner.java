package tech.pegasys.pantheon.tests.acceptance.dsl.node;

import io.vertx.core.Vertx;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.pantheon.Runner;
import tech.pegasys.pantheon.RunnerBuilder;
import tech.pegasys.pantheon.cli.PantheonControllerBuilder;
import tech.pegasys.pantheon.controller.PantheonController;
import tech.pegasys.pantheon.ethereum.eth.sync.SynchronizerConfiguration.Builder;

public class ThreadPantheonNodeRunner implements PantheonNodeRunner {

  private final Logger LOG = LogManager.getLogger();
  private final Map<String, Runner> pantheonRunners = new HashMap<>();
  private ExecutorService nodeExecutor = Executors.newCachedThreadPool();

  @SuppressWarnings("rawtypes")
  @Override
  public void startNode(final PantheonNode node) {
    if (nodeExecutor == null || nodeExecutor.isShutdown()) {
      nodeExecutor = Executors.newCachedThreadPool();
    }

    final PantheonControllerBuilder builder = new PantheonControllerBuilder();
    PantheonController<?, ?> pantheonController;
    try {
      pantheonController =
          builder.build(
              new Builder().build(),
              node.homeDirectory(),
              node.getEthNetworkConfig(),
              false,
              node.getMiningParameters(),
              node.isDevMode());
    } catch (final IOException e) {
      throw new RuntimeException("Error building PantheonController", e);
    }

    final Runner runner =
        new RunnerBuilder()
            .build(
                Vertx.vertx(),
                pantheonController,
                true,
                node.bootnodes(),
                node.getHost(),
                node.p2pPort(),
                25,
                node.jsonRpcConfiguration(),
                node.webSocketConfiguration(),
                node.homeDirectory());

    nodeExecutor.submit(runner::execute);

    waitForPortsFile(node.homeDirectory().toAbsolutePath());

    pantheonRunners.put(node.getName(), runner);
  }

  @Override
  public void stopNode(final PantheonNode node) {
    node.stop();
    killRunner(node.getName());
  }

  @Override
  public void shutdown() {
    pantheonRunners.keySet().forEach(this::killRunner);
    try {
      nodeExecutor.shutdownNow();
      if (!nodeExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        throw new IllegalStateException("Failed to shut down node executor");
      }
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void killRunner(final String name) {
    LOG.info("Killing " + name + " runner");

    if (pantheonRunners.containsKey(name)) {
      try {
        pantheonRunners.get(name).close();
      } catch (final Exception e) {
        throw new RuntimeException("Error shutting down node " + name, e);
      }
    }
  }
}
