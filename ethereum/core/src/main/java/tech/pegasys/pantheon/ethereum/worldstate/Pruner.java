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

import tech.pegasys.pantheon.ethereum.chain.BlockAddedEvent;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Pruner {
  private static final Logger LOG = LogManager.getLogger();

  private final MarkSweepPruner pruningStrategy;
  private final Blockchain blockchain;
  private final ExecutorService executor;
  private final long retentionPeriodInBlocks;
  private final AtomicReference<State> state = new AtomicReference<>(State.IDLE);
  private volatile long markBlockNumber = 0;
  private volatile BlockHeader markedBlockHeader;
  private long transientForkThreshold;

  public Pruner(
      final MarkSweepPruner pruningStrategy,
      final Blockchain blockchain,
      final ExecutorService executor,
      final long transientForkThreshold,
      final long retentionPeriodInBlocks) {
    this.pruningStrategy = pruningStrategy;
    this.blockchain = blockchain;
    this.executor = executor;
    if (!(transientForkThreshold >= 0 && retentionPeriodInBlocks >= transientForkThreshold)) {
      throw new IllegalArgumentException(
          String.format(
              "Expected transientForkThreshold >= 0 and retentionPeriodInBlocks >= transientForkThreshold. transientForkThreshold=%d, retentionPeriodInBlocks=%d",
              transientForkThreshold,
              retentionPeriodInBlocks));
    }
    this.retentionPeriodInBlocks = retentionPeriodInBlocks;
    this.transientForkThreshold = transientForkThreshold;
  }

  public void start() {
    blockchain.observeBlockAdded((event, blockchain) -> handleNewBlock(event));
  }

  public void stop() throws InterruptedException {
    executor.awaitTermination(30, TimeUnit.SECONDS);
  }

  private void handleNewBlock(final BlockAddedEvent event) {
    if (!event.isNewCanonicalHead()) {
      return;
    }

    final long blockNumber = event.getBlock().getHeader().getNumber();
    if (state.compareAndSet(State.IDLE, State.TRANSIENT_FORK_OUTLIVING)) {
      pruningStrategy.prepare();
      markBlockNumber = blockNumber;
    }

    if (blockNumber >= markBlockNumber + transientForkThreshold
        && state.compareAndSet(State.TRANSIENT_FORK_OUTLIVING, State.MARKING)) {
      markedBlockHeader = blockchain.getBlockHeader(markBlockNumber).get();
      mark(markedBlockHeader);
    }

    if (blockNumber >= markBlockNumber + retentionPeriodInBlocks
        && blockchain.blockIsOnCanonicalChain(markedBlockHeader.getHash())
        && state.compareAndSet(State.MARKING_COMPLETE, State.SWEEPING)) {
      sweep();
    }
  }

  private void mark(final BlockHeader header) {
    markBlockNumber = header.getNumber();
    final Hash stateRoot = header.getStateRoot();
    LOG.info(
        "Begin marking used nodes for pruning. Block number: {} State root: {}",
        markBlockNumber,
        stateRoot);
    execute(
        () -> {
          pruningStrategy.mark(stateRoot);
          state.compareAndSet(State.MARKING, State.MARKING_COMPLETE);
        });
  }

  private void sweep() {
    LOG.info(
        "Begin sweeping unused nodes for pruning. Retention period: {}", retentionPeriodInBlocks);
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
    }
  }

  private enum State {
    IDLE,
    TRANSIENT_FORK_OUTLIVING,
    MARKING,
    MARKING_COMPLETE,
    SWEEPING;
  }
}
