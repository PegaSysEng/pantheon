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
package tech.pegasys.pantheon.ethereum.eth.sync;

import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.eth.manager.AbstractPeerTask.PeerTaskResult;
import tech.pegasys.pantheon.ethereum.eth.manager.ChainState;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeers;
import tech.pegasys.pantheon.ethereum.eth.sync.ChainDownloader.SyncTargetManager;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncState;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncTarget;
import tech.pegasys.pantheon.ethereum.eth.sync.tasks.DetermineCommonAncestorTask;
import tech.pegasys.pantheon.ethereum.eth.sync.tasks.ImportBlocksTask;
import tech.pegasys.pantheon.ethereum.eth.sync.tasks.PipelinedImportChainSegmentTask;
import tech.pegasys.pantheon.ethereum.eth.sync.tasks.WaitForPeerTask;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.metrics.LabelledMetric;
import tech.pegasys.pantheon.metrics.OperationTimer;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.time.Duration;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FullSyncDownloader<C> {
  private static final Logger LOG = LogManager.getLogger();
  private final ChainDownloader<C> chainDownloader;
  private final SynchronizerConfiguration config;
  private final ProtocolSchedule<C> protocolSchedule;
  private final ProtocolContext<C> protocolContext;
  private final EthContext ethContext;
  private final SyncState syncState;
  private final LabelledMetric<OperationTimer> ethTasksTimer;

  FullSyncDownloader(
      final SynchronizerConfiguration config,
      final ProtocolSchedule<C> protocolSchedule,
      final ProtocolContext<C> protocolContext,
      final EthContext ethContext,
      final SyncState syncState,
      final LabelledMetric<OperationTimer> ethTasksTimer) {
    this.config = config;
    this.protocolSchedule = protocolSchedule;
    this.protocolContext = protocolContext;
    this.ethContext = ethContext;
    this.syncState = syncState;
    this.ethTasksTimer = ethTasksTimer;
    chainDownloader =
        new ChainDownloader<>(
            config,
            protocolSchedule,
            protocolContext,
            ethContext,
            syncState,
            ethTasksTimer,
            new FullSyncTargetManager(),
            this::importBlocksForCheckpoints);
  }

  public void start() {
    chainDownloader.start();
  }

  @VisibleForTesting
  CompletableFuture<?> getCurrentTask() {
    return chainDownloader.getCurrentTask();
  }

  private CompletableFuture<List<Block>> importBlocksForCheckpoints(
      final Deque<BlockHeader> checkpointHeaders) {
    final CompletableFuture<List<Block>> importedBlocks;
    if (checkpointHeaders.size() < 2) {
      // Download blocks without constraining the end block
      final ImportBlocksTask<C> importTask =
          ImportBlocksTask.fromHeader(
              protocolSchedule,
              protocolContext,
              ethContext,
              checkpointHeaders.getFirst(),
              config.downloaderChainSegmentSize(),
              ethTasksTimer);
      importedBlocks = importTask.run().thenApply(PeerTaskResult::getResult);
    } else {
      final PipelinedImportChainSegmentTask<C> importTask =
          PipelinedImportChainSegmentTask.forCheckpoints(
              protocolSchedule,
              protocolContext,
              ethContext,
              config.downloaderParallelism(),
              ethTasksTimer,
              Lists.newArrayList(checkpointHeaders));
      importedBlocks = importTask.run();
    }
    return importedBlocks;
  }

  private class FullSyncTargetManager implements SyncTargetManager {

    private long syncTargetDisconnectListenerId;
    private volatile boolean syncTargetDisconnected = false;

    @Override
    public CompletableFuture<SyncTarget> findSyncTarget() {
      final Optional<SyncTarget> maybeSyncTarget = syncState.syncTarget();
      if (maybeSyncTarget.isPresent()) {
        // Nothing to do
        return CompletableFuture.completedFuture(maybeSyncTarget.get());
      }

      final Optional<EthPeer> maybeBestPeer = ethContext.getEthPeers().bestPeer();
      if (!maybeBestPeer.isPresent()) {
        LOG.info("No sync target, wait for peers.");
        return waitForPeerAndThenSetSyncTarget();
      } else {
        final EthPeer bestPeer = maybeBestPeer.get();
        final long peerHeight = bestPeer.chainState().getEstimatedHeight();
        final UInt256 peerTd = bestPeer.chainState().getBestBlock().getTotalDifficulty();
        if (peerTd.compareTo(syncState.chainHeadTotalDifficulty()) <= 0
            && peerHeight <= syncState.chainHeadNumber()) {
          // We're caught up to our best peer, try again when a new peer connects
          LOG.debug("Caught up to best peer: " + bestPeer.chainState().getEstimatedHeight());
          return waitForPeerAndThenSetSyncTarget();
        }
        return DetermineCommonAncestorTask.create(
                protocolSchedule,
                protocolContext,
                ethContext,
                bestPeer,
                config.downloaderHeaderRequestSize(),
                ethTasksTimer)
            .run()
            .handle((r, t) -> r)
            .thenCompose(
                (target) -> {
                  if (target == null) {
                    return waitForPeerAndThenSetSyncTarget();
                  }
                  final SyncTarget syncTarget = syncState.setSyncTarget(bestPeer, target);
                  LOG.info(
                      "Found common ancestor with peer {} at block {}",
                      bestPeer,
                      target.getNumber());
                  syncTargetDisconnectListenerId =
                      bestPeer.subscribeDisconnect(this::onSyncTargetPeerDisconnect);
                  return CompletableFuture.completedFuture(syncTarget);
                });
      }
    }

    @Override
    public boolean shouldSwitchSyncTarget(final SyncTarget currentTarget) {
      final EthPeer currentPeer = currentTarget.peer();
      final ChainState currentPeerChainState = currentPeer.chainState();
      final Optional<EthPeer> maybeBestPeer = ethContext.getEthPeers().bestPeer();

      return maybeBestPeer
          .map(
              bestPeer -> {
                if (EthPeers.BEST_CHAIN.compare(bestPeer, currentPeer) <= 0) {
                  // Our current target is better or equal to the best peer
                  return false;
                }
                // Require some threshold to be exceeded before switching targets to keep some
                // stability
                // when multiple peers are in range of each other
                final ChainState bestPeerChainState = bestPeer.chainState();
                final long heightDifference =
                    bestPeerChainState.getEstimatedHeight()
                        - currentPeerChainState.getEstimatedHeight();
                if (heightDifference == 0 && bestPeerChainState.getEstimatedHeight() == 0) {
                  // Only check td if we don't have a height metric
                  final UInt256 tdDifference =
                      bestPeerChainState
                          .getBestBlock()
                          .getTotalDifficulty()
                          .minus(currentPeerChainState.getBestBlock().getTotalDifficulty());
                  return tdDifference.compareTo(config.downloaderChangeTargetThresholdByTd()) > 0;
                }
                return heightDifference > config.downloaderChangeTargetThresholdByHeight();
              })
          .orElse(false);
    }

    @Override
    public boolean isSyncTargetDisconnected() {
      return syncTargetDisconnected;
    }

    @Override
    public void clearSyncTarget(final SyncTarget syncTarget) {
      syncTarget.peer().unsubscribeDisconnect(syncTargetDisconnectListenerId);
      syncTargetDisconnected = false;
    }

    private CompletableFuture<SyncTarget> waitForPeerAndThenSetSyncTarget() {
      return waitForNewPeer().handle((r, t) -> r).thenCompose((r) -> findSyncTarget());
    }

    private CompletableFuture<?> waitForNewPeer() {
      return ethContext
          .getScheduler()
          .timeout(WaitForPeerTask.create(ethContext, ethTasksTimer), Duration.ofSeconds(5));
    }

    private void onSyncTargetPeerDisconnect(final EthPeer ethPeer) {
      LOG.info("Sync target disconnected: {}", ethPeer);
      syncTargetDisconnected = true;
    }
  }
}
