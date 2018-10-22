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

import tech.pegasys.pantheon.ethereum.chain.BlockAddedEvent;
import tech.pegasys.pantheon.ethereum.chain.BlockAddedObserver;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.SyncStatus;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.util.Subscribers;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.Optional;
import java.util.function.Consumer;

public class SyncState implements BlockAddedObserver {
  private final Blockchain blockchain;
  private final EthContext ethContext;

  private final long startingBlock;
  private final PendingBlocks pendingBlocks;
  private Optional<SyncTarget> syncTarget = Optional.empty();
  private boolean lastInSync = false;

  private Subscribers<Consumer<Boolean>> inSyncSubscribers;

  public SyncState(
      final Blockchain blockchain, final EthContext ethContext, final PendingBlocks pendingBlocks) {
    this.blockchain = blockchain;
    this.ethContext = ethContext;
    this.startingBlock = chainHeadNumber();
    this.pendingBlocks = pendingBlocks;
    inSyncSubscribers = new Subscribers<>();
    blockchain.observeBlockAdded(this);
  }

  public long addInSyncObserver(final Consumer<Boolean> observer) {
    return inSyncSubscribers.subscribe(observer);
  }

  public void removeInSyncObserver(final long observerId) {
    inSyncSubscribers.unsubscribe(observerId);
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

  public PendingBlocks pendingBlocks() {
    return pendingBlocks;
  }

  public Optional<SyncTarget> syncTarget() {
    return syncTarget;
  }

  public SyncTarget setSyncTarget(final EthPeer peer, final BlockHeader commonAncestor) {
    final SyncTarget target = new SyncTarget(peer, commonAncestor);
    this.syncTarget = Optional.of(target);
    target.addPeerChainEstimatedHeightObserver(this::targetHeightChanged);
    checkInSync();
    return target;
  }

  private void checkInSync() {
    final boolean newInSync = getInSync();

    if (newInSync != lastInSync) {
      lastInSync = newInSync;
      inSyncSubscribers.forEach(c -> c.accept(newInSync));
    }
  }

  public boolean getInSync() {
    return syncTarget
        .map(t -> t.estimatedTargetHeight() <= blockchain.getChainHeadBlockNumber())
        .orElse(false);
  }

  public void setCommonAncestor(final BlockHeader commonAncestor) {
    syncTarget.ifPresent(target -> target.setCommonAncestor(commonAncestor));
  }

  public void clearSyncTarget() {
    this.syncTarget = Optional.empty();
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

  public void targetHeightChanged(final long height) {
    checkInSync();
  }

  @Override
  public void onBlockAdded(final BlockAddedEvent event, final Blockchain blockchain) {
    checkInSync();
  }
}
