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
package tech.pegasys.pantheon.controller;

import tech.pegasys.pantheon.config.GenesisConfigFile;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.blockcreation.MiningCoordinator;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.chain.GenesisState;
import tech.pegasys.pantheon.ethereum.chain.MutableBlockchain;
import tech.pegasys.pantheon.ethereum.core.MiningParameters;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;
import tech.pegasys.pantheon.ethereum.core.Synchronizer;
import tech.pegasys.pantheon.ethereum.eth.EthProtocol;
import tech.pegasys.pantheon.ethereum.eth.EthereumWireProtocolConfiguration;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthProtocolManager;
import tech.pegasys.pantheon.ethereum.eth.peervalidation.DaoForkPeerValidator;
import tech.pegasys.pantheon.ethereum.eth.peervalidation.PeerValidatorRunner;
import tech.pegasys.pantheon.ethereum.eth.sync.DefaultSynchronizer;
import tech.pegasys.pantheon.ethereum.eth.sync.SyncMode;
import tech.pegasys.pantheon.ethereum.eth.sync.SynchronizerConfiguration;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncState;
import tech.pegasys.pantheon.ethereum.eth.transactions.TransactionPool;
import tech.pegasys.pantheon.ethereum.eth.transactions.TransactionPoolFactory;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethodFactory;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.p2p.config.SubProtocolConfiguration;
import tech.pegasys.pantheon.ethereum.storage.StorageProvider;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateArchive;
import tech.pegasys.pantheon.metrics.MetricsSystem;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ConsensusAwarePantheonControllerBuilder<C> {
  private static final Logger LOG = LogManager.getLogger();

  protected final GenesisConfigFile genesisConfig;
  protected final SynchronizerConfiguration syncConfig;
  protected final EthereumWireProtocolConfiguration ethereumWireProtocolConfiguration;
  protected final StorageProvider storageProvider;
  protected final int networkId;
  protected final MiningParameters miningParameters;
  protected final KeyPair nodeKeys;
  protected final MetricsSystem metricsSystem;
  protected final PrivacyParameters privacyParameters;
  protected final Path dataDirectory;
  protected final Clock clock;
  protected final int maxPendingTransactions;
  private final List<Runnable> shutdownActions = new ArrayList<>();

  protected ConsensusAwarePantheonControllerBuilder(
      final GenesisConfigFile genesisConfig,
      final SynchronizerConfiguration syncConfig,
      final EthereumWireProtocolConfiguration ethereumWireProtocolConfiguration,
      final StorageProvider storageProvider,
      final int networkId,
      final MiningParameters miningParameters,
      final KeyPair nodeKeys,
      final MetricsSystem metricsSystem,
      final PrivacyParameters privacyParameters,
      final Path dataDirectory,
      final Clock clock,
      final int maxPendingTransactions) {
    this.genesisConfig = genesisConfig;
    this.syncConfig = syncConfig;
    this.ethereumWireProtocolConfiguration = ethereumWireProtocolConfiguration;
    this.storageProvider = storageProvider;
    this.networkId = networkId;
    this.miningParameters = miningParameters;
    this.nodeKeys = nodeKeys;
    this.metricsSystem = metricsSystem;
    this.privacyParameters = privacyParameters;
    this.dataDirectory = dataDirectory;
    this.clock = clock;
    this.maxPendingTransactions = maxPendingTransactions;
  }

  public PantheonController<C> createPantheonController() {
    final ProtocolSchedule<C> protocolSchedule = createProtocolSchedule();
    final GenesisState genesisState = GenesisState.fromConfig(genesisConfig, protocolSchedule);
    final ProtocolContext<C> protocolContext =
        ProtocolContext.init(
            storageProvider,
            genesisState,
            protocolSchedule,
            metricsSystem,
            this::createConsensusContext);
    final MutableBlockchain blockchain = protocolContext.getBlockchain();

    final boolean fastSyncEnabled = syncConfig.syncMode().equals(SyncMode.FAST);
    final EthProtocolManager ethProtocolManager =
        createEthProtocolManager(protocolContext, fastSyncEnabled);
    final SyncState syncState =
        new SyncState(blockchain, ethProtocolManager.ethContext().getEthPeers());
    final Synchronizer synchronizer =
        new DefaultSynchronizer<>(
            syncConfig,
            protocolSchedule,
            protocolContext,
            protocolContext.getWorldStateArchive().getStorage(),
            ethProtocolManager.ethContext(),
            syncState,
            dataDirectory,
            clock,
            metricsSystem);

    final OptionalLong daoBlock = genesisConfig.getConfigOptions().getDaoForkBlock();
    if (daoBlock.isPresent()) {
      // Setup dao validator
      final EthContext ethContext = ethProtocolManager.ethContext();
      final DaoForkPeerValidator daoForkPeerValidator =
          new DaoForkPeerValidator(
              ethContext, protocolSchedule, metricsSystem, daoBlock.getAsLong());
      PeerValidatorRunner.runValidator(ethContext, daoForkPeerValidator);
    }

    final TransactionPool transactionPool =
        TransactionPoolFactory.createTransactionPool(
            protocolSchedule,
            protocolContext,
            ethProtocolManager.ethContext(),
            clock,
            maxPendingTransactions,
            metricsSystem,
            syncState);

    final MiningCoordinator miningCoordinator =
        createMiningCoordinator(
            protocolSchedule,
            protocolContext,
            transactionPool,
            miningParameters,
            syncState,
            ethProtocolManager);

    final SubProtocolConfiguration subProtocolConfiguration =
        createSubProtocolConfiguration(ethProtocolManager);

    final JsonRpcMethodFactory additionalJsonRpcMethodFactory =
        createAdditionalJsonRpcMethodFactory(protocolContext);
    return new PantheonController<>(
        protocolSchedule,
        protocolContext,
        genesisConfig.getConfigOptions(),
        subProtocolConfiguration,
        synchronizer,
        additionalJsonRpcMethodFactory,
        nodeKeys,
        transactionPool,
        miningCoordinator,
        privacyParameters,
        () -> {
          shutdownActions.forEach(Runnable::run);
          try {
            storageProvider.close();
            if (privacyParameters.getPrivateStorageProvider() != null) {
              privacyParameters.getPrivateStorageProvider().close();
            }
          } catch (final IOException e) {
            LOG.error("Failed to close storage provider", e);
          }
        });
  }

  protected JsonRpcMethodFactory createAdditionalJsonRpcMethodFactory(
      final ProtocolContext<C> protocolContext) {
    return apis -> Collections.emptyMap();
  }

  protected SubProtocolConfiguration createSubProtocolConfiguration(
      final EthProtocolManager ethProtocolManager) {
    return new SubProtocolConfiguration().withSubProtocol(EthProtocol.get(), ethProtocolManager);
  }

  protected final void addShutdownAction(final Runnable action) {
    shutdownActions.add(action);
  }

  protected abstract MiningCoordinator createMiningCoordinator(
      ProtocolSchedule<C> protocolSchedule,
      ProtocolContext<C> protocolContext,
      TransactionPool transactionPool,
      MiningParameters miningParameters,
      SyncState syncState,
      EthProtocolManager ethProtocolManager);

  protected abstract ProtocolSchedule<C> createProtocolSchedule();

  protected abstract C createConsensusContext(
      Blockchain blockchain, WorldStateArchive worldStateArchive);

  protected EthProtocolManager createEthProtocolManager(
      final ProtocolContext<C> protocolContext, final boolean fastSyncEnabled) {
    return new EthProtocolManager(
        protocolContext.getBlockchain(),
        protocolContext.getWorldStateArchive(),
        networkId,
        fastSyncEnabled,
        syncConfig.downloaderParallelism(),
        syncConfig.transactionsParallelism(),
        syncConfig.computationParallelism(),
        metricsSystem,
        ethereumWireProtocolConfiguration);
  }
}
