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
package org.hyperledger.besu.tests.acceptance.dsl.node;

import static org.hyperledger.besu.cli.config.NetworkName.DEV;

import org.hyperledger.besu.Runner;
import org.hyperledger.besu.RunnerBuilder;
import org.hyperledger.besu.cli.config.EthNetworkConfig;
import org.hyperledger.besu.controller.KeyPairUtil;
import org.hyperledger.besu.controller.PantheonController;
import org.hyperledger.besu.controller.PantheonControllerBuilder;
import org.hyperledger.besu.ethereum.eth.EthProtocolConfiguration;
import org.hyperledger.besu.ethereum.eth.sync.SynchronizerConfiguration;
import org.hyperledger.besu.ethereum.eth.transactions.TransactionPoolConfiguration;
import org.hyperledger.besu.ethereum.graphql.GraphQLConfiguration;
import org.hyperledger.besu.ethereum.p2p.peers.EnodeURL;
import org.hyperledger.besu.ethereum.permissioning.PermissioningConfiguration;
import org.hyperledger.besu.metrics.MetricsSystem;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.hyperledger.besu.plugin.services.PantheonEvents;
import org.hyperledger.besu.plugin.services.PicoCLIOptions;
import org.hyperledger.besu.services.PantheonEventsImpl;
import org.hyperledger.besu.services.PantheonPluginContextImpl;
import org.hyperledger.besu.services.PicoCLIOptionsImpl;
import org.hyperledger.besu.services.kvstore.RocksDbConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.io.Files;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

public class ThreadPantheonNodeRunner implements PantheonNodeRunner {

  private final Logger LOG = LogManager.getLogger();
  private final Map<String, Runner> pantheonRunners = new HashMap<>();
  private ExecutorService nodeExecutor = Executors.newCachedThreadPool();

  private final Map<Node, PantheonPluginContextImpl> pantheonPluginContextMap = new HashMap<>();

  private PantheonPluginContextImpl buildPluginContext(final PantheonNode node) {
    final PantheonPluginContextImpl pantheonPluginContext = new PantheonPluginContextImpl();
    final Path pluginsPath = node.homeDirectory().resolve("plugins");
    final File pluginsDirFile = pluginsPath.toFile();
    if (!pluginsDirFile.isDirectory()) {
      pluginsDirFile.mkdirs();
      pluginsDirFile.deleteOnExit();
    }
    System.setProperty("pantheon.plugins.dir", pluginsPath.toString());
    pantheonPluginContext.registerPlugins(pluginsPath);
    return pantheonPluginContext;
  }

  @Override
  @SuppressWarnings("UnstableApiUsage")
  public void startNode(final PantheonNode node) {
    if (nodeExecutor == null || nodeExecutor.isShutdown()) {
      nodeExecutor = Executors.newCachedThreadPool();
    }

    final CommandLine commandLine = new CommandLine(CommandSpec.create());
    final PantheonPluginContextImpl pantheonPluginContext =
        pantheonPluginContextMap.computeIfAbsent(node, n -> buildPluginContext(node));
    pantheonPluginContext.addService(PicoCLIOptions.class, new PicoCLIOptionsImpl(commandLine));

    commandLine.parseArgs(node.getConfiguration().getExtraCLIOptions().toArray(new String[0]));

    final MetricsSystem noOpMetricsSystem = new NoOpMetricsSystem();
    final List<EnodeURL> bootnodes =
        node.getConfiguration().getBootnodes().stream()
            .map(EnodeURL::fromURI)
            .collect(Collectors.toList());
    final EthNetworkConfig.Builder networkConfigBuilder =
        new EthNetworkConfig.Builder(EthNetworkConfig.getNetworkConfig(DEV))
            .setBootNodes(bootnodes);
    node.getConfiguration().getGenesisConfig().ifPresent(networkConfigBuilder::setGenesisConfig);
    final EthNetworkConfig ethNetworkConfig = networkConfigBuilder.build();
    final PantheonControllerBuilder<?> builder =
        new PantheonController.Builder().fromEthNetworkConfig(ethNetworkConfig);
    final Path tempDir = Files.createTempDir().toPath();

    final PantheonController<?> pantheonController;
    try {
      pantheonController =
          builder
              .synchronizerConfiguration(new SynchronizerConfiguration.Builder().build())
              .dataDirectory(node.homeDirectory())
              .miningParameters(node.getMiningParameters())
              .privacyParameters(node.getPrivacyParameters())
              .nodePrivateKeyFile(KeyPairUtil.getDefaultKeyFile(node.homeDirectory()))
              .metricsSystem(noOpMetricsSystem)
              .transactionPoolConfiguration(TransactionPoolConfiguration.builder().build())
              .rocksDbConfiguration(RocksDbConfiguration.builder().databaseDir(tempDir).build())
              .ethProtocolConfiguration(EthProtocolConfiguration.defaultConfig())
              .clock(Clock.systemUTC())
              .isRevertReasonEnabled(node.isRevertReasonEnabled())
              .build();
    } catch (final IOException e) {
      throw new RuntimeException("Error building PantheonController", e);
    }

    final RunnerBuilder runnerBuilder = new RunnerBuilder();
    if (node.getPermissioningConfiguration().isPresent()) {
      final PermissioningConfiguration permissioningConfiguration =
          node.getPermissioningConfiguration().get();

      runnerBuilder.permissioningConfiguration(permissioningConfiguration);
    }

    pantheonPluginContext.addService(
        PantheonEvents.class,
        new PantheonEventsImpl(
            pantheonController.getProtocolManager().getBlockBroadcaster(),
            pantheonController.getTransactionPool()));
    pantheonPluginContext.startPlugins();

    final Runner runner =
        runnerBuilder
            .vertx(Vertx.vertx())
            .pantheonController(pantheonController)
            .ethNetworkConfig(ethNetworkConfig)
            .discovery(node.isDiscoveryEnabled())
            .p2pAdvertisedHost(node.getHostName())
            .p2pListenPort(0)
            .maxPeers(25)
            .networkingConfiguration(node.getNetworkingConfiguration())
            .jsonRpcConfiguration(node.jsonRpcConfiguration())
            .webSocketConfiguration(node.webSocketConfiguration())
            .dataDir(node.homeDirectory())
            .metricsSystem(noOpMetricsSystem)
            .metricsConfiguration(node.metricsConfiguration())
            .p2pEnabled(node.isP2pEnabled())
            .graphQLConfiguration(GraphQLConfiguration.createDefault())
            .staticNodes(
                node.getStaticNodes().stream()
                    .map(EnodeURL::fromString)
                    .collect(Collectors.toList()))
            .build();

    runner.start();

    pantheonRunners.put(node.getName(), runner);
  }

  @Override
  public void stopNode(final PantheonNode node) {
    final PantheonPluginContextImpl pluginContext = pantheonPluginContextMap.remove(node);
    if (pluginContext != null) {
      pluginContext.stopPlugins();
    }
    node.stop();
    killRunner(node.getName());
  }

  @Override
  public void shutdown() {
    // stop all plugins from pluginContext
    pantheonPluginContextMap.values().forEach(PantheonPluginContextImpl::stopPlugins);
    pantheonPluginContextMap.clear();

    // iterate over a copy of the set so that pantheonRunner can be updated when a runner is killed
    new HashSet<>(pantheonRunners.keySet()).forEach(this::killRunner);
    try {
      nodeExecutor.shutdownNow();
      if (!nodeExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        throw new IllegalStateException("Failed to shut down node executor");
      }
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isActive(final String nodeName) {
    return pantheonRunners.containsKey(nodeName);
  }

  private void killRunner(final String name) {
    LOG.info("Killing " + name + " runner");

    if (pantheonRunners.containsKey(name)) {
      try {
        pantheonRunners.get(name).close();
        pantheonRunners.remove(name);
      } catch (final Exception e) {
        throw new RuntimeException("Error shutting down node " + name, e);
      }
    }
  }
}
