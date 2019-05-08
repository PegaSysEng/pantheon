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
package tech.pegasys.pantheon.ethereum.worldstate;

import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.chain.BlockAddedEvent;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Pruner {
  private static final Logger LOG = LogManager.getLogger();
  private final MarkSweepPruner pruningStrategy;
  private final ProtocolContext<?> protocolContext;
  private final ExecutorService executor;
  private final long retentionPeriodInBlocks;
  private final AtomicReference<State> state = new AtomicReference<>(State.IDLE);
  private volatile long markedBlockNumber = 0;

  public Pruner(
      final MarkSweepPruner pruningStrategy,
      final ProtocolContext<?> protocolContext,
      final long retentionPeriodInBlocks) {
    this.pruningStrategy = pruningStrategy;
    this.protocolContext = protocolContext;
    this.executor =
        Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                .setDaemon(true)
                .setPriority(Thread.MIN_PRIORITY)
                .setNameFormat("StatePruning-%d")
                .build());
    this.retentionPeriodInBlocks = retentionPeriodInBlocks;
  }

  public void start() {
    // TODO: Don't attempt pruning while a fast sync is in progress.
    protocolContext.getBlockchain().observeBlockAdded((event, blockchain) -> handleNewBlock(event));
  }

  public void stop() {
    executor.shutdownNow();
  }

  private void handleNewBlock(final BlockAddedEvent event) {
    if (!event.isNewCanonicalHead()) {
      return;
    }
    final BlockHeader header = event.getBlock().getHeader();
    // Only kick off pruning every million blocks.
    if (state.compareAndSet(State.IDLE, State.MARKING)) {
      /* TODO: We don't currently handle re-orgs.
      Will need to:
       1. start listening for new nodes at this point
       2. then after a few blocks mark the world state of the block when we started listening (now a few blocks back)
       3. check the block we marked is still on the canonical chain when we begin sweeping and abort that round of pruning if not
       */

      mark(header);
    } else if (header.getNumber() > markedBlockNumber + retentionPeriodInBlocks
        && state.compareAndSet(State.MARKING_COMPLETE, State.SWEEPING)) {
      sweep();
    }
  }

  private void mark(final BlockHeader header) {
    markedBlockNumber = header.getNumber();
    final Hash stateRoot = header.getStateRoot();
    LOG.info(
        "Begin marking used nodes for pruning. Block number: {} State root: {}",
        markedBlockNumber,
        stateRoot);
    pruningStrategy.prepare();
    execute(
        () -> {
          pruningStrategy.mark(stateRoot);
          state.compareAndSet(State.MARKING, State.MARKING_COMPLETE);
        });
  }

  private void sweep() {
    execute(
        () -> {
          pruningStrategy.sweep();
          state.compareAndSet(State.SWEEPING, State.IDLE);
        });
  }

  private void execute(final Runnable action) {
    try {
      executor.execute(action);
    } catch (final Throwable t) {
      LOG.error("Pruning failed", t);
      state.set(State.IDLE);
      System.exit(1);
    }
  }

  private enum State {
    IDLE,
    MARKING,
    MARKING_COMPLETE,
    SWEEPING
  }
}
