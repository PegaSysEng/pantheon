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
package tech.pegasys.pantheon.ethereum.eth.sync.fastsync;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.chain.MutableBlockchain;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockDataGenerator;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.eth.manager.DeterministicEthScheduler;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthProtocolManager;
import tech.pegasys.pantheon.ethereum.eth.manager.EthProtocolManagerTestUtil;
import tech.pegasys.pantheon.ethereum.eth.manager.EthScheduler;
import tech.pegasys.pantheon.ethereum.eth.manager.RespondingEthPeer;
import tech.pegasys.pantheon.ethereum.eth.manager.RespondingEthPeer.Responder;
import tech.pegasys.pantheon.ethereum.eth.manager.ethtaskutils.BlockchainSetupUtil;
import tech.pegasys.pantheon.ethereum.eth.sync.SynchronizerConfiguration;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncState;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncTarget;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;

import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;

public class FastSyncTargetManagerTest {

  protected ProtocolSchedule<Void> protocolSchedule;
  protected EthProtocolManager ethProtocolManager;
  protected EthContext ethContext;
  protected ProtocolContext<Void> protocolContext;
  private SyncState syncState;

  protected MutableBlockchain localBlockchain;
  private BlockchainSetupUtil<Void> otherBlockchainSetup;
  protected Blockchain otherBlockchain;
  private Responder responder;
  private BlockDataGenerator generator;

  @Before
  public void setup() {
    generator = new BlockDataGenerator();
    final BlockchainSetupUtil<Void> localBlockchainSetup = BlockchainSetupUtil.forTesting();
    localBlockchain = localBlockchainSetup.getBlockchain();
    otherBlockchainSetup = BlockchainSetupUtil.forTesting();
    otherBlockchain = otherBlockchainSetup.getBlockchain();
    responder = RespondingEthPeer.blockchainResponder(otherBlockchain);

    protocolSchedule = localBlockchainSetup.getProtocolSchedule();
    protocolContext = localBlockchainSetup.getProtocolContext();
    ethProtocolManager =
        EthProtocolManagerTestUtil.create(
            localBlockchain,
            localBlockchainSetup.getWorldArchive(),
            DeterministicEthScheduler.TimeoutPolicy.NEVER,
            new EthScheduler(1, 1, 1));
    ethContext = ethProtocolManager.ethContext();
    syncState = new SyncState(protocolContext.getBlockchain(), ethContext.getEthPeers());

    otherBlockchainSetup.importFirstBlocks(20);
  }

  private FastSyncTargetManager<Void> createFastSyncTargetManagerForPivot(
      final BlockHeader pivotBlockHeader) {
    return new FastSyncTargetManager<>(
        SynchronizerConfiguration.builder().build(),
        protocolSchedule,
        protocolContext,
        ethContext,
        syncState,
        NoOpMetricsSystem.NO_OP_LABELLED_TIMER,
        pivotBlockHeader);
  }

  @Test
  public void shouldDisconnectPeerIfPivotBlockHeaderDoesNotMatch() {
    final BlockHeader pivotBlockHeader = new BlockHeaderTestFixture().number(15).buildHeader();
    final FastSyncTargetManager<Void> targetManager =
        createFastSyncTargetManagerForPivot(pivotBlockHeader);

    final RespondingEthPeer bestPeer =
        EthProtocolManagerTestUtil.createPeer(ethProtocolManager, 20);

    final CompletableFuture<SyncTarget> result = targetManager.findSyncTarget();

    bestPeer.respond(responder);

    assertThat(result).isNotCompleted();
    assertThat(bestPeer.getPeerConnection().isDisconnected()).isTrue();
  }

  @Test
  public void shouldAcceptPeerIfPivotBlockHeaderMatches() {
    final BlockHeader pivotBlockHeader = otherBlockchain.getBlockHeader(15).get();
    final FastSyncTargetManager<Void> targetManager =
        createFastSyncTargetManagerForPivot(pivotBlockHeader);

    final RespondingEthPeer bestPeer =
        EthProtocolManagerTestUtil.createPeer(ethProtocolManager, 15);

    final CompletableFuture<SyncTarget> result = targetManager.findSyncTarget();

    bestPeer.respond(responder);

    assertThat(result)
        .isCompletedWithValue(
            new SyncTarget(bestPeer.getEthPeer(), localBlockchain.getChainHeadHeader()));
    assertThat(bestPeer.getPeerConnection().isDisconnected()).isFalse();
  }

  @Test
  public void shouldRejectPeerIfChainHeightBelowPivotBlock() {
    final BlockHeader pivotBlockHeader = otherBlockchain.getBlockHeader(15).get();
    final FastSyncTargetManager<Void> targetManager =
        createFastSyncTargetManagerForPivot(pivotBlockHeader);

    final RespondingEthPeer bestPeer =
        EthProtocolManagerTestUtil.createPeer(ethProtocolManager, 14);

    final CompletableFuture<SyncTarget> result = targetManager.findSyncTarget();

    bestPeer.respond(responder);

    assertThat(result).isNotCompleted();
    assertThat(bestPeer.getPeerConnection().isDisconnected()).isFalse();
  }

  @Test
  public void shouldForceChainReorgToCommonAncestor() {
    // Both local and remote chains agree on block 1.
    final BlockHeader commonAncestor = otherBlockchain.getBlockHeader(1).get();
    localBlockchain.appendBlock(
        new Block(commonAncestor, otherBlockchain.getBlockBody(commonAncestor.getHash()).get()),
        otherBlockchain.getTxReceipts(commonAncestor.getHash()).get());

    // But then our local chain has forked off
    appendForkBlockToLocalBlockchain();
    appendForkBlockToLocalBlockchain();

    // When we select a new peer to sync to...
    final BlockHeader pivotBlockHeader = otherBlockchain.getBlockHeader(15).get();
    final FastSyncTargetManager<Void> targetManager =
        createFastSyncTargetManagerForPivot(pivotBlockHeader);

    final RespondingEthPeer bestPeer =
        EthProtocolManagerTestUtil.createPeer(ethProtocolManager, 15);

    final CompletableFuture<SyncTarget> result = targetManager.findSyncTarget();

    bestPeer.respondWhile(responder, () -> !result.isDone());

    assertThat(result).isCompletedWithValue(new SyncTarget(bestPeer.getEthPeer(), commonAncestor));
    assertThat(bestPeer.getPeerConnection().isDisconnected()).isFalse();

    // We should re-org back to the common ancestor even though it isn't yet the best chain
    assertThat(localBlockchain.getChainHeadHeader()).isEqualTo(commonAncestor);
  }

  private void appendForkBlockToLocalBlockchain() {
    final Block block = generator.nextBlock(localBlockchain.getChainHeadBlock());
    localBlockchain.appendBlock(block, generator.receipts(block));
  }
}
