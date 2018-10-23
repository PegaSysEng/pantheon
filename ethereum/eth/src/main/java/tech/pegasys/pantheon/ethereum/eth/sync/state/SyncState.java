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
package tech.pegasys.pantheon.ethereum.eth.sync.state;

import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.SyncStatus;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.util.Subscribers;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SyncState {
  private final Blockchain blockchain;
  private final EthContext ethContext;

  private final long startingBlock;
  private final AtomicBoolean lastInSync = new AtomicBoolean(false);
  private final Subscribers<Consumer<Boolean>> inSyncSubscribers = new Subscribers<>();
  private Optional<SyncTarget> syncTarget = Optional.empty();
  private long chainHeightListenerId;

  public SyncState(final Blockchain blockchain, final EthContext ethContext) {
    this.blockchain = blockchain;
    this.ethContext = ethContext;
    this.startingBlock = chainHeadNumber();
    blockchain.observeBlockAdded(
        (event, chain) -> {
          if (event.isNewCanonicalHead()) {
            checkInSync();
          }
        });
  }

  public void addInSyncObserver(final Consumer<Boolean> observer) {
    inSyncSubscribers.subscribe(observer);
  }

  public SyncStatus syncStatus() {
    return new SyncStatus(startingBlock(), chainHeadNumber(), bestChainHeight());
  }

  public long startingBlock() {
    return startingBlock;
  }

  public long chainHeadNumber() {
    return blockchain.getChainHeadBlockNumber();
  }

  public UInt256 chainHeadTotalDifficulty() {
    return blockchain.getChainHead().getTotalDifficulty();
  }

  public Optional<SyncTarget> syncTarget() {
    return syncTarget;
  }

  public SyncTarget setSyncTarget(final EthPeer peer, final BlockHeader commonAncestor) {
    final SyncTarget syncTarget = new SyncTarget(peer, commonAncestor);
    replaceSyncTarget(Optional.of(syncTarget));
    return syncTarget;
  }

  public boolean isInSync() {
    return syncTarget
        .map(t -> t.estimatedTargetHeight() <= blockchain.getChainHeadBlockNumber())
        .orElse(false);
  }

  public void setCommonAncestor(final BlockHeader commonAncestor) {
    syncTarget.ifPresent(target -> target.setCommonAncestor(commonAncestor));
  }

  public void clearSyncTarget() {
    replaceSyncTarget(Optional.empty());
  }

  private void replaceSyncTarget(final Optional<SyncTarget> newTarget) {
    syncTarget.ifPresent(this::removeEstimatedHeightListener);
    syncTarget = newTarget;
    newTarget.ifPresent(this::addEstimatedHeightListener);
    checkInSync();
  }

  private void removeEstimatedHeightListener(final SyncTarget target) {
    target.removePeerChainEstimatedHeightListener(chainHeightListenerId);
  }

  private void addEstimatedHeightListener(final SyncTarget target) {
    chainHeightListenerId =
        target.addPeerChainEstimatedHeightListener(estimatedHeight -> checkInSync());
  }

  public long bestChainHeight() {
    final long localChainHeight = blockchain.getChainHeadBlockNumber();
    return bestChainHeight(localChainHeight);
  }

  public long bestChainHeight(final long localChainHeight) {
    return Math.max(
        localChainHeight,
        ethContext
            .getEthPeers()
            .bestPeer()
            .map(p -> p.chainState().getEstimatedHeight())
            .orElse(localChainHeight));
  }

  private void checkInSync() {
    final boolean currentSyncStatus = isInSync();
    if (lastInSync.compareAndSet(!currentSyncStatus, currentSyncStatus)) {
      inSyncSubscribers.forEach(c -> c.accept(currentSyncStatus));
    }
  }
}
