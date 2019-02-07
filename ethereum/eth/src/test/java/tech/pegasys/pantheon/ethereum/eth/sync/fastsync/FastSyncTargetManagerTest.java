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
import static tech.pegasys.pantheon.ethereum.eth.manager.RespondingEthPeer.blockchainResponder;

import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.chain.MutableBlockchain;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockBody;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.ethereum.eth.manager.EthProtocolManager;
import tech.pegasys.pantheon.ethereum.eth.manager.EthProtocolManagerTestUtil;
import tech.pegasys.pantheon.ethereum.eth.manager.RespondingEthPeer;
import tech.pegasys.pantheon.ethereum.eth.manager.RespondingEthPeer.Responder;
import tech.pegasys.pantheon.ethereum.eth.manager.ethtaskutils.BlockchainSetupUtil;
import tech.pegasys.pantheon.ethereum.eth.sync.SynchronizerConfiguration;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncState;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncTarget;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;

public class FastSyncTargetManagerTest {

  protected ProtocolSchedule<Void> protocolSchedule;
  protected EthProtocolManager ethProtocolManager;
  protected EthContext ethContext;
  protected ProtocolContext<Void> protocolContext;

  protected MutableBlockchain localBlockchain;
  protected Blockchain otherBlockchain;
  private BlockHeader pivotBlockHeader;
  private FastSyncTargetManager<Void> fastSyncTargetManager;
  private Responder otherChainResponder;
  private SyncState syncState;

  @Before
  public void setupTest() {
    final BlockchainSetupUtil<Void> localBlockchainSetup = BlockchainSetupUtil.forTesting();
    localBlockchain = localBlockchainSetup.getBlockchain();
    final BlockchainSetupUtil<Void> otherBlockchainSetup = BlockchainSetupUtil.forTesting();
    otherBlockchain = otherBlockchainSetup.getBlockchain();

    protocolSchedule = localBlockchainSetup.getProtocolSchedule();
    protocolContext = localBlockchainSetup.getProtocolContext();
    ethProtocolManager =
        EthProtocolManagerTestUtil.create(localBlockchain, localBlockchainSetup.getWorldArchive());
    ethContext = ethProtocolManager.ethContext();
    syncState = new SyncState(protocolContext.getBlockchain(), ethContext.getEthPeers());

    otherBlockchainSetup.importFirstBlocks(30);

    pivotBlockHeader = otherBlockchain.getBlockHeader(17).get();

    otherChainResponder = blockchainResponder(otherBlockchain);
    fastSyncTargetManager =
        new FastSyncTargetManager<>(
            SynchronizerConfiguration.builder().build(),
            protocolSchedule,
            protocolContext,
            ethContext,
            syncState,
            NoOpMetricsSystem.NO_OP_LABELLED_TIMER,
            pivotBlockHeader);
  }

  @Test
  public void shouldSelectBestPeerAsSyncTargetWhenItHasCorrectPivotHeader() {
    final RespondingEthPeer peer =
        EthProtocolManagerTestUtil.createPeer(ethProtocolManager, otherBlockchain);
    final CompletableFuture<SyncTarget> result = fastSyncTargetManager.findSyncTarget();

    peer.respondWhile(otherChainResponder, () -> !result.isDone());
    assertThat(result)
        .isCompletedWithValue(
            new SyncTarget(peer.getEthPeer(), localBlockchain.getBlockHeader(0).get()));
  }

  @Test
  public void shouldNotHaveASyncTargetWhenBestPeerHasNotReachedPivotBlock() {
    final RespondingEthPeer peer =
        EthProtocolManagerTestUtil.createPeer(ethProtocolManager, localBlockchain);
    final CompletableFuture<Optional<EthPeer>> result =
        fastSyncTargetManager.selectBestAvailableSyncTarget();

    peer.respondWhile(blockchainResponder(localBlockchain), () -> !result.isDone());
    assertThat(result).isCompletedWithValue(Optional.empty());
  }

  @Test
  public void shouldDisconnectBestPeerIfItHasIncorrectPivotBlock() {
    final BlockchainSetupUtil<Void> forkedChainSetup = BlockchainSetupUtil.forTesting();
    forkedChainSetup.importFirstBlocks((int) pivotBlockHeader.getNumber() - 1);
    final MutableBlockchain forkedChain = forkedChainSetup.getBlockchain();
    forkedChain.appendBlock(
        new Block(
            new BlockHeaderTestFixture()
                .number(pivotBlockHeader.getNumber())
                .parentHash(forkedChain.getChainHeadHash())
                .buildHeader(),
            new BlockBody(Collections.emptyList(), Collections.emptyList())),
        Collections.emptyList());

    final RespondingEthPeer peer =
        EthProtocolManagerTestUtil.createPeer(ethProtocolManager, forkedChain);

    final CompletableFuture<Optional<EthPeer>> result =
        fastSyncTargetManager.selectBestAvailableSyncTarget();

    peer.respondWhile(blockchainResponder(forkedChain), () -> !result.isDone());
    assertThat(result).isCompletedWithValue(Optional.empty());
    assertThat(peer.getPeerConnection().isDisconnected()).isTrue();
  }

  @Test
  public void shouldResumeSyncingFromFastSyncHead() {
    final BlockHeader importedHeader = otherBlockchain.getBlockHeader(1).get();
    localBlockchain.fastAppendBlock(
        new Block(importedHeader, otherBlockchain.getBlockBody(importedHeader.getHash()).get()),
        otherBlockchain.getTxReceipts(importedHeader.getHash()).get());

    final RespondingEthPeer peer =
        EthProtocolManagerTestUtil.createPeer(ethProtocolManager, otherBlockchain);
    final CompletableFuture<SyncTarget> result = fastSyncTargetManager.findSyncTarget();

    peer.respondWhile(blockchainResponder(otherBlockchain), () -> !result.isDone());
    assertThat(result).isCompletedWithValue(new SyncTarget(peer.getEthPeer(), importedHeader));
  }
}
