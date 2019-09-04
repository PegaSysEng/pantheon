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
import org.hyperledger.besu.Runner;
import org.hyperledger.besu.RunnerBuilder;
import org.hyperledger.besu.cli.config.EthNetworkConfig;
import org.hyperledger.besu.controller.KeyPairUtil;
import org.hyperledger.besu.controller.BesuController;
import org.hyperledger.besu.controller.BesuControllerBuilder;
import org.hyperledger.besu.ethereum.eth.EthProtocolConfiguration;
import org.hyperledger.besu.ethereum.eth.sync.SynchronizerConfiguration;
import org.hyperledger.besu.ethereum.eth.transactions.TransactionPoolConfiguration;
import org.hyperledger.besu.ethereum.graphql.GraphQLConfiguration;
import org.hyperledger.besu.ethereum.p2p.peers.EnodeURL;
import org.hyperledger.besu.ethereum.permissioning.PermissioningConfiguration;
import org.hyperledger.besu.metrics.ObservableMetricsSystem;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.hyperledger.besu.plugin.services.BesuEvents;
import org.hyperledger.besu.plugin.services.PicoCLIOptions;
import org.hyperledger.besu.services.BesuEventsImpl;
import org.hyperledger.besu.services.BesuPluginContextImpl;
import org.hyperledger.besu.services.PicoCLIOptionsImpl;
import org.hyperledger.besu.services.kvstore.RocksDbConfiguration;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

public class ThreadBesuNodeRunner implements BesuNodeRunner {

  private final Logger LOG = LogManager.getLogger();
  private final Map<String, Runner> besuRunners = new HashMap<>();
  private ExecutorService nodeExecutor = Executors.newCachedThreadPool();

  private final Map<Node, BesuPluginContextImpl> besuPluginContextMap = new HashMap<>();

  private BesuPluginContextImpl buildPluginContext(final BesuNode node) {
    final BesuPluginContextImpl besuPluginContext = new BesuPluginContextImpl();
    final Path pluginsPath = node.homeDirectory().resolve("plugins");
    final File pluginsDirFile = pluginsPath.toFile();
    if (!pluginsDirFile.isDirectory()) {
      pluginsDirFile.mkdirs();
      pluginsDirFile.deleteOnExit();
    }
    System.setProperty("besu.plugins.dir", pluginsPath.toString());
    besuPluginContext.registerPlugins(pluginsPath);
    return besuPluginContext;
  }

  @Override
  @SuppressWarnings("UnstableApiUsage")
  public void startNode(final BesuNode node) {
    if (nodeExecutor == null || nodeExecutor.isShutdown()) {
      nodeExecutor = Executors.newCachedThreadPool();
    }

    final CommandLine commandLine = new CommandLine(CommandSpec.create());
    final BesuPluginContextImpl besuPluginContext =
        besuPluginContextMap.computeIfAbsent(node, n -> buildPluginContext(node));
    besuPluginContext.addService(PicoCLIOptions.class, new PicoCLIOptionsImpl(commandLine));

    commandLine.parseArgs(node.getConfiguration().getExtraCLIOptions().toArray(new String[0]));

    final ObservableMetricsSystem noOpMetricsSystem = new NoOpMetricsSystem();
    final List<EnodeURL> bootnodes =
        node.getConfiguration().getBootnodes().stream()
            .map(EnodeURL::fromURI)
            .collect(Collectors.toList());
    final EthNetworkConfig.Builder networkConfigBuilder =
        new EthNetworkConfig.Builder(EthNetworkConfig.getNetworkConfig(DEV))
            .setBootNodes(bootnodes);
    node.getConfiguration().getGenesisConfig().ifPresent(networkConfigBuilder::setGenesisConfig);
    final EthNetworkConfig ethNetworkConfig = networkConfigBuilder.build();
    final BesuControllerBuilder<?> builder =
        new BesuController.Builder().fromEthNetworkConfig(ethNetworkConfig);
    final Path tempDir = Files.createTempDir().toPath();

    final BesuController<?> besuController;
    try {
      besuController =
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
      throw new RuntimeException("Error building BesuController", e);
    }

    final RunnerBuilder runnerBuilder = new RunnerBuilder();
    if (node.getPermissioningConfiguration().isPresent()) {
      final PermissioningConfiguration permissioningConfiguration =
          node.getPermissioningConfiguration().get();

      runnerBuilder.permissioningConfiguration(permissioningConfiguration);
    }

    besuPluginContext.addService(
        BesuEvents.class,
        new BesuEventsImpl(
            besuController.getProtocolManager().getBlockBroadcaster(),
            besuController.getTransactionPool()));
    besuPluginContext.startPlugins();

    final Runner runner =
        runnerBuilder
            .vertx(Vertx.vertx())
            .besuController(besuController)
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

    besuRunners.put(node.getName(), runner);
  }

  @Override
  public void stopNode(final BesuNode node) {
    final BesuPluginContextImpl pluginContext = besuPluginContextMap.remove(node);
    if (pluginContext != null) {
      pluginContext.stopPlugins();
    }
    node.stop();
    killRunner(node.getName());
  }

  @Override
  public void shutdown() {
    // stop all plugins from pluginContext
    besuPluginContextMap.values().forEach(BesuPluginContextImpl::stopPlugins);
    besuPluginContextMap.clear();

    // iterate over a copy of the set so that besuRunner can be updated when a runner is killed
    new HashSet<>(besuRunners.keySet()).forEach(this::killRunner);
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
    return besuRunners.containsKey(nodeName);
  }

  private void killRunner(final String name) {
    LOG.info("Killing " + name + " runner");

    if (besuRunners.containsKey(name)) {
      try {
        besuRunners.get(name).close();
        besuRunners.remove(name);
      } catch (final Exception e) {
        throw new RuntimeException("Error shutting down node " + name, e);
      }
    }
  }
}
