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
package tech.pegasys.pantheon.ethereum.graphql;

import tech.pegasys.pantheon.config.GenesisConfigOptions;
import tech.pegasys.pantheon.ethereum.blockcreation.MiningCoordinator;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;
import tech.pegasys.pantheon.ethereum.core.Synchronizer;
import tech.pegasys.pantheon.ethereum.eth.transactions.TransactionPool;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.BlockOmmerAtFetcher;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.BlockOmmersFetcher;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.BlockParentFetcher;
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
        new BlockTransactionsFetcher(blockchainQueries));
    //    if (rpcApis.contains(RpcApis.ETH)) {
    //      addMethods(
    //          enabledMethods,
    //          new EthAccounts(),
    //          new EthBlockNumber(blockchainQueries),
    //          new EthGetBalance(blockchainQueries, parameter),
    //          new EthGetBlockByHash(blockchainQueries, blockResult, parameter),
    //          new EthGetBlockByNumber(blockchainQueries, blockResult, parameter),
    //          new EthGetBlockTransactionCountByNumber(blockchainQueries, parameter),
    //          new EthGetBlockTransactionCountByHash(blockchainQueries, parameter),
    //          new EthCall(
    //              blockchainQueries,
    //              new TransactionSimulator(
    //                  blockchainQueries.getBlockchain(),
    //                  blockchainQueries.getWorldStateArchive(),
    //                  protocolSchedule),
    //              parameter),
    //          new EthGetCode(blockchainQueries, parameter),
    //          new EthGetLogs(blockchainQueries, parameter),
    //          new EthGetUncleCountByBlockHash(blockchainQueries, parameter),
    //          new EthGetUncleCountByBlockNumber(blockchainQueries, parameter),
    //          new EthGetUncleByBlockNumberAndIndex(blockchainQueries, parameter),
    //          new EthGetUncleByBlockHashAndIndex(blockchainQueries, parameter),
    //          new EthNewBlockFilter(filterManager),
    //          new EthNewPendingTransactionFilter(filterManager),
    //          new EthNewFilter(filterManager, parameter),
    //          new EthGetTransactionByHash(
    //              blockchainQueries, transactionPool.getPendingTransactions(), parameter),
    //          new EthGetTransactionByBlockHashAndIndex(blockchainQueries, parameter),
    //          new EthGetTransactionByBlockNumberAndIndex(blockchainQueries, parameter),
    //          new EthGetTransactionCount(
    //              blockchainQueries, transactionPool.getPendingTransactions(), parameter),
    //          new EthGetTransactionReceipt(blockchainQueries, parameter),
    //          new EthUninstallFilter(filterManager, parameter),
    //          new EthGetFilterChanges(filterManager, parameter),
    //          new EthGetFilterLogs(filterManager, parameter),
    //          new EthSyncing(synchronizer),
    //          new EthGetStorageAt(blockchainQueries, parameter),
    //          new EthSendRawTransaction(transactionPool, parameter),
    //          new EthEstimateGas(
    //              blockchainQueries,
    //              new TransactionSimulator(
    //                  blockchainQueries.getBlockchain(),
    //                  blockchainQueries.getWorldStateArchive(),
    //                  protocolSchedule),
    //              parameter),
    //          new EthMining(miningCoordinator),
    //          new EthCoinbase(miningCoordinator),
    //          new EthProtocolVersion(supportedCapabilities),
    //          new EthGasPrice(miningCoordinator),
    //          new EthGetWork(miningCoordinator),
    //          new EthHashrate(miningCoordinator),
    //          new EthChainId(protocolSchedule.getChainId()));
    //    }
    //    if (rpcApis.contains(RpcApis.DEBUG)) {
    //      final BlockReplay blockReplay =
    //          new BlockReplay(
    //              protocolSchedule,
    //              blockchainQueries.getBlockchain(),
    //              blockchainQueries.getWorldStateArchive());
    //      addMethods(
    //          enabledMethods,
    //          new DebugTraceTransaction(
    //              blockchainQueries, new TransactionTracer(blockReplay), parameter),
    //          new DebugStorageRangeAt(parameter, blockchainQueries, blockReplay),
    //          new DebugMetrics(metricsSystem),
    //          new DebugTraceBlock(
    //              parameter,
    //              new BlockTracer(blockReplay),
    //              ScheduleBasedBlockHashFunction.create(protocolSchedule),
    //              blockchainQueries),
    //          new DebugTraceBlockByNumber(parameter, new BlockTracer(blockReplay),
    // blockchainQueries),
    //          new DebugTraceBlockByHash(parameter, new BlockTracer(blockReplay)));
    //    }
    //    if (rpcApis.contains(RpcApis.NET)) {
    //      addMethods(
    //          enabledMethods,
    //          new NetVersion(protocolSchedule.getChainId()),
    //          new NetListening(p2pNetwork),
    //          new NetPeerCount(p2pNetwork),
    //          new NetEnode(p2pNetwork),
    //          new NetServices(
    //              jsonRpcConfiguration, webSocketConfiguration, p2pNetwork,
    // metricsConfiguration));
    //    }
    //    if (rpcApis.contains(RpcApis.WEB3)) {
    //      addMethods(enabledMethods, new Web3ClientVersion(clientVersion), new Web3Sha3());
    //    }
    //    if (rpcApis.contains(RpcApis.MINER)) {
    //      final MinerSetCoinbase minerSetCoinbase = new MinerSetCoinbase(miningCoordinator,
    // parameter);
    //      addMethods(
    //          enabledMethods,
    //          new MinerStart(miningCoordinator),
    //          new MinerStop(miningCoordinator),
    //          minerSetCoinbase,
    //          new MinerSetEtherbase(minerSetCoinbase));
    //    }
    //    if (rpcApis.contains(RpcApis.TX_POOL)) {
    //      addMethods(
    //          enabledMethods, new
    // TxPoolPantheonTransactions(transactionPool.getPendingTransactions()));
    //    }
    //    if (rpcApis.contains(RpcApis.PERM)) {
    //      addMethods(
    //          enabledMethods,
    //          new PermAddNodesToWhitelist(nodeWhitelistController, parameter),
    //          new PermRemoveNodesFromWhitelist(nodeWhitelistController, parameter),
    //          new PermGetNodesWhitelist(nodeWhitelistController),
    //          new PermGetAccountsWhitelist(accountsWhitelistController),
    //          new PermAddAccountsToWhitelist(accountsWhitelistController, parameter),
    //          new PermRemoveAccountsFromWhitelist(accountsWhitelistController, parameter),
    //          new PermReloadPermissionsFromFile(accountsWhitelistController,
    // nodeWhitelistController));
    //    }
    //    if (rpcApis.contains(RpcApis.ADMIN)) {
    //      addMethods(
    //          enabledMethods,
    //          new AdminAddPeer(p2pNetwork, parameter),
    //          new AdminRemovePeer(p2pNetwork, parameter),
    //          new AdminNodeInfo(
    //              clientVersion, networkId, genesisConfigOptions, p2pNetwork, blockchainQueries),
    //          new AdminPeers(p2pNetwork));
    //    }
    //    if (rpcApis.contains(RpcApis.EEA)) {
    //      addMethods(
    //          enabledMethods,
    //          new EeaSendRawTransaction(
    //              new PrivateTransactionHandler(privacyParameters), transactionPool, parameter));
    //      addMethods(
    //          enabledMethods,
    //          new EeaGetTransactionReceipt(
    //              blockchainQueries,
    //              new Enclave(privacyParameters.getEnclaveUri()),
    //              parameter,
    //              privacyParameters));
    //    }
    return enabledMethods;
  }

  private void addMethods(
      final Map<String, GraphQLMethod> methods, final GraphQLMethod... graphQLMethods) {
    for (final GraphQLMethod graphQLMethod : graphQLMethods) {
      methods.put(graphQLMethod.getType().getField(), graphQLMethod);
    }
  }
}
