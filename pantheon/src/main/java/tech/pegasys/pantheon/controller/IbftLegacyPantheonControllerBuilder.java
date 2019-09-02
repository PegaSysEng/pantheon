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

import org.hyperledger.besu.config.IbftConfigOptions;
import org.hyperledger.besu.consensus.common.BlockInterface;
import org.hyperledger.besu.consensus.common.EpochManager;
import org.hyperledger.besu.consensus.common.VoteProposer;
import org.hyperledger.besu.consensus.common.VoteTallyCache;
import org.hyperledger.besu.consensus.common.VoteTallyUpdater;
import org.hyperledger.besu.consensus.ibft.IbftContext;
import org.hyperledger.besu.consensus.ibftlegacy.IbftLegacyBlockInterface;
import org.hyperledger.besu.consensus.ibftlegacy.IbftProtocolSchedule;
import org.hyperledger.besu.consensus.ibftlegacy.protocol.Istanbul64Protocol;
import org.hyperledger.besu.consensus.ibftlegacy.protocol.Istanbul64ProtocolManager;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.blockcreation.MiningCoordinator;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.MiningParameters;
import tech.pegasys.pantheon.ethereum.eth.manager.EthProtocolManager;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncState;
import tech.pegasys.pantheon.ethereum.eth.transactions.TransactionPool;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.ethereum.p2p.config.SubProtocolConfiguration;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateArchive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IbftLegacyPantheonControllerBuilder extends PantheonControllerBuilder<IbftContext> {

  private static final Logger LOG = LogManager.getLogger();
  private final BlockInterface blockInterface = new IbftLegacyBlockInterface();

  @Override
  protected SubProtocolConfiguration createSubProtocolConfiguration(
      final EthProtocolManager ethProtocolManager) {
    return new SubProtocolConfiguration()
        .withSubProtocol(Istanbul64Protocol.get(), ethProtocolManager);
  }

  @Override
  protected MiningCoordinator createMiningCoordinator(
      final ProtocolSchedule<IbftContext> protocolSchedule,
      final ProtocolContext<IbftContext> protocolContext,
      final TransactionPool transactionPool,
      final MiningParameters miningParameters,
      final SyncState syncState,
      final EthProtocolManager ethProtocolManager) {
    return null;
  }

  @Override
  protected ProtocolSchedule<IbftContext> createProtocolSchedule() {
    return IbftProtocolSchedule.create(
        genesisConfig.getConfigOptions(), privacyParameters, isRevertReasonEnabled);
  }

  @Override
  protected IbftContext createConsensusContext(
      final Blockchain blockchain, final WorldStateArchive worldStateArchive) {
    final IbftConfigOptions ibftConfig =
        genesisConfig.getConfigOptions().getIbftLegacyConfigOptions();
    final EpochManager epochManager = new EpochManager(ibftConfig.getEpochLength());
    final VoteTallyCache voteTallyCache =
        new VoteTallyCache(
            blockchain,
            new VoteTallyUpdater(epochManager, blockInterface),
            epochManager,
            blockInterface);

    final VoteProposer voteProposer = new VoteProposer();
    return new IbftContext(voteTallyCache, voteProposer);
  }

  @Override
  protected void validateContext(final ProtocolContext<IbftContext> context) {
    final BlockHeader genesisBlockHeader = context.getBlockchain().getGenesisBlock().getHeader();

    if (blockInterface.validatorsInBlock(genesisBlockHeader).isEmpty()) {
      LOG.warn("Genesis block contains no signers - chain will not progress.");
    }
  }

  @Override
  protected EthProtocolManager createEthProtocolManager(
      final ProtocolContext<IbftContext> protocolContext, final boolean fastSyncEnabled) {
    LOG.info("Operating on IBFT-1.0 network.");
    return new Istanbul64ProtocolManager(
        protocolContext.getBlockchain(),
        protocolContext.getWorldStateArchive(),
        networkId,
        fastSyncEnabled,
        syncConfig.getDownloaderParallelism(),
        syncConfig.getTransactionsParallelism(),
        syncConfig.getComputationParallelism(),
        clock,
        metricsSystem,
        ethereumWireProtocolConfiguration);
  }
}
