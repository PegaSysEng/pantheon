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
package tech.pegasys.pantheon.ethereum.graphql;

import tech.pegasys.pantheon.config.GenesisConfigOptions;
import tech.pegasys.pantheon.ethereum.blockcreation.MiningCoordinator;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;
import tech.pegasys.pantheon.ethereum.core.Synchronizer;
import tech.pegasys.pantheon.ethereum.eth.transactions.TransactionPool;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.BlockAccountFetcher;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.BlockOmmerAtFetcher;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.BlockOmmersFetcher;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.BlockParentFetcher;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.BlockTransactionAtFetcher;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.BlockTransactionsFetcher;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.GraphQLMethod;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.QueryBlockFetcher;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.QueryBlocksFetcher;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.p2p.api.P2PNetwork;
import tech.pegasys.pantheon.ethereum.p2p.wire.Capability;
import tech.pegasys.pantheon.ethereum.permissioning.AccountWhitelistController;
import tech.pegasys.pantheon.ethereum.permissioning.NodeLocalConfigPermissioningController;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateArchive;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.metrics.prometheus.MetricsConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GraphQLMethodsFactory {

  public Map<String, GraphQLMethod> methods(
      final String clientVersion,
      final int networkId,
      final GenesisConfigOptions genesisConfigOptions,
      final P2PNetwork peerNetworkingService,
      final Blockchain blockchain,
      final WorldStateArchive worldStateArchive,
      final Synchronizer synchronizer,
      final TransactionPool transactionPool,
      final ProtocolSchedule<?> protocolSchedule,
      final MiningCoordinator miningCoordinator,
      final MetricsSystem metricsSystem,
      final Set<Capability> supportedCapabilities,
      final Optional<AccountWhitelistController> accountsWhitelistController,
      final Optional<NodeLocalConfigPermissioningController> nodeWhitelistController,
      final PrivacyParameters privacyParameters,
      //      final JsonRpcConfiguration jsonRpcConfiguration,
      //      final WebSocketConfiguration webSocketConfiguration,
      final MetricsConfiguration metricsConfiguration) {
    final BlockchainQueries blockchainQueries =
        new BlockchainQueries(blockchain, worldStateArchive);
    return methods(
        clientVersion,
        networkId,
        genesisConfigOptions,
        peerNetworkingService,
        blockchainQueries,
        synchronizer,
        protocolSchedule,
        transactionPool,
        miningCoordinator,
        metricsSystem,
        supportedCapabilities,
        accountsWhitelistController,
        nodeWhitelistController,
        privacyParameters,
        metricsConfiguration);
  }

  public Map<String, GraphQLMethod> methods(
      final String clientVersion,
      final int networkId,
      final GenesisConfigOptions genesisConfigOptions,
      final P2PNetwork p2pNetwork,
      final BlockchainQueries blockchainQueries,
      final Synchronizer synchronizer,
      final ProtocolSchedule<?> protocolSchedule,
      final TransactionPool transactionPool,
      final MiningCoordinator miningCoordinator,
      final MetricsSystem metricsSystem,
      final Set<Capability> supportedCapabilities,
      final Optional<AccountWhitelistController> accountsWhitelistController,
      final Optional<NodeLocalConfigPermissioningController> nodeWhitelistController,
      final PrivacyParameters privacyParameters,
      final MetricsConfiguration metricsConfiguration) {
    final Map<String, GraphQLMethod> enabledMethods = new HashMap<>();
    addMethods(
        enabledMethods,
        new QueryBlockFetcher(blockchainQueries),
        new QueryBlocksFetcher(blockchainQueries),
        new BlockParentFetcher(blockchainQueries),
        new BlockOmmersFetcher(blockchainQueries),
        new BlockOmmerAtFetcher(blockchainQueries),
        new BlockTransactionsFetcher(blockchainQueries),
            new BlockTransactionAtFetcher(blockchainQueries)
//            new BlockAccountFetcher(worldStateArchive),
    );
    return enabledMethods;
  }

  private void addMethods(
      final Map<String, GraphQLMethod> methods, final GraphQLMethod... graphQLMethods) {
    for (final GraphQLMethod graphQLMethod : graphQLMethods) {
      methods.put(graphQLMethod.getType().getField(), graphQLMethod);
    }
  }
}
