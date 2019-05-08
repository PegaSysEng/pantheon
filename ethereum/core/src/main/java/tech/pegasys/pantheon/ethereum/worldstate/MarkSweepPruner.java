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

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.ethereum.trie.MerklePatriciaTrie;
import tech.pegasys.pantheon.ethereum.trie.StoredMerklePatriciaTrie;
import tech.pegasys.pantheon.metrics.Counter;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.metrics.PantheonMetricCategory;
import tech.pegasys.pantheon.services.kvstore.KeyValueStorage;
import tech.pegasys.pantheon.services.kvstore.KeyValueStorage.Transaction;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MarkSweepPruner {
  private static final Logger LOG = LogManager.getLogger();
  private static final BytesValue IN_USE = BytesValue.of(1);
  private static final int MARKS_PER_TRANSACTION = 1000;
  private final WorldStateStorage worldStateStorage;
  private final KeyValueStorage markStorage;
  private final Counter markedNodesCounter;
  private final Counter markOperationCounter;
  private final Counter sweepOperationCounter;
  private final Counter sweptNodesCounter;
  private long nodeAddedListenerId;
  private final ReentrantLock markLock = new ReentrantLock(true);
  private final Set<BytesValue> pendingMarks = Collections.newSetFromMap(new ConcurrentHashMap<>());

  public MarkSweepPruner(
      final WorldStateStorage worldStateStorage,
      final KeyValueStorage markStorage,
      final MetricsSystem metricsSystem) {
    this.worldStateStorage = worldStateStorage;
    this.markStorage = markStorage;

    markedNodesCounter =
        metricsSystem.createCounter(
            PantheonMetricCategory.PRUNER,
            "marked_nodes_total",
            "Total number of nodes marked as in use");
    markOperationCounter =
        metricsSystem.createCounter(
            PantheonMetricCategory.PRUNER,
            "mark_operations_total",
            "Total number of mark operations performed");

    sweptNodesCounter =
        metricsSystem.createCounter(
            PantheonMetricCategory.PRUNER,
            "swept_nodes_total",
            "Total number of unused nodes removed");
    sweepOperationCounter =
        metricsSystem.createCounter(
            PantheonMetricCategory.PRUNER,
            "sweep_operations_total",
            "Total number of sweep operations performed");
  }

  public void prepare() {
    worldStateStorage.removeNodeAddedListener(nodeAddedListenerId); // Just in case.
    nodeAddedListenerId = worldStateStorage.addNodeAddedListener(this::markNewNodes);
  }

  // Note chainHeadStateRoot must be the state root of the current chain head.
  // We can delay the actual sweep until a certain number of blocks in the future if we want to
  // have a certain number of block history available.
  // TODO: Need to ensure we only start marking when new world states aren't being persisted
  // Once we add our node added listener persisting world states can continue but we can't start
  // half way through persisting a world state.
  public void mark(final Hash rootHash) {
    markOperationCounter.inc();
    markStorage.clear();
    createStateTrie(rootHash)
        .visitAll(
            node -> {
              if (Thread.interrupted()) {
                // TODO: Probably need a better abort process than this....
                throw new RuntimeException("Interrupted while marking");
              }
              markNode(node.getHash());
              node.getValue().ifPresent(this::processAccountState);
            });
    LOG.info("Completed marking used nodes for pruning");
  }

  public void sweep() {
    flushPendingMarks();
    sweepOperationCounter.inc();
    LOG.info("Sweeping unused nodes");
    final long prunedNodeCount =
        worldStateStorage.prune(key -> pendingMarks.contains(key) || markStorage.containsKey(key));
    sweptNodesCounter.inc(prunedNodeCount);
    worldStateStorage.removeNodeAddedListener(nodeAddedListenerId);
    markStorage.clear();
    LOG.info("Completed sweeping unused nodes");
  }

  private MerklePatriciaTrie<Bytes32, BytesValue> createStateTrie(final Bytes32 rootHash) {
    return new StoredMerklePatriciaTrie<>(
        worldStateStorage::getAccountStateTrieNode,
        rootHash,
        Function.identity(),
        Function.identity(),
        true);
  }

  private MerklePatriciaTrie<Bytes32, BytesValue> createStorageTrie(final Bytes32 rootHash) {
    return new StoredMerklePatriciaTrie<>(
        worldStateStorage::getAccountStorageTrieNode,
        rootHash,
        Function.identity(),
        Function.identity(),
        true);
  }

  private void processAccountState(final BytesValue value) {
    final StateTrieAccountValue accountValue = StateTrieAccountValue.readFrom(RLP.input(value));
    markNode(accountValue.getCodeHash());

    createStorageTrie(accountValue.getStorageRoot())
        .visitAll(storageNode -> markNode(storageNode.getHash()));
  }

  private void markNode(final Bytes32 hash) {
    markedNodesCounter.inc();
    markLock.lock();
    try {
      pendingMarks.add(hash);
      maybeFlushPendingMarks();
    } finally {
      markLock.unlock();
    }
  }

  private void maybeFlushPendingMarks() {
    if (pendingMarks.size() > MARKS_PER_TRANSACTION) {
      flushPendingMarks();
    }
  }

  void flushPendingMarks() {
    markLock.lock();
    try {
      final Transaction transaction = markStorage.startTransaction();
      pendingMarks.forEach(node -> transaction.put(node, IN_USE));
      transaction.commit();
      pendingMarks.clear();
    } finally {
      markLock.unlock();
    }
  }

  private void markNewNodes(final Collection<Bytes32> nodeHashes) {
    markedNodesCounter.inc(nodeHashes.size());
    markLock.lock();
    try {
      pendingMarks.addAll(nodeHashes);
      maybeFlushPendingMarks();
    } finally {
      markLock.unlock();
    }
  }
}
