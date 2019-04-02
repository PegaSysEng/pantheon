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

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CheckpointRangeSource implements Iterator<CheckpointRange> {
  private static final Logger LOG = LogManager.getLogger();

  private final CheckpointHeaderFetcher checkpointFetcher;
  private final SyncTargetChecker syncTargetChecker;
  private final EthPeer peer;
  private final int checkpointTimeoutsPermitted;
  private final int newHeaderWaitDuration;
  private final TimeUnit newHeaderWaitDurationUnit;

  private final Queue<CheckpointRange> retrievedRanges = new ArrayDeque<>();
  private BlockHeader lastRangeEnd;
  private Optional<CompletableFuture<List<BlockHeader>>> pendingCheckpointsRequest =
      Optional.empty();
  private int requestFailureCount = 0;

  public CheckpointRangeSource(
      final CheckpointHeaderFetcher checkpointFetcher,
      final SyncTargetChecker syncTargetChecker,
      final EthPeer peer,
      final BlockHeader commonAncestor,
      final int checkpointTimeoutsPermitted,
      final int newHeaderWaitDuration,
      final TimeUnit newHeaderWaitDurationUnit) {
    this.checkpointFetcher = checkpointFetcher;
    this.syncTargetChecker = syncTargetChecker;
    this.peer = peer;
    this.lastRangeEnd = commonAncestor;
    this.checkpointTimeoutsPermitted = checkpointTimeoutsPermitted;
    this.newHeaderWaitDuration = newHeaderWaitDuration;
    this.newHeaderWaitDurationUnit = newHeaderWaitDurationUnit;
  }

  @Override
  public boolean hasNext() {
    return !retrievedRanges.isEmpty()
        || (requestFailureCount < checkpointTimeoutsPermitted
            && syncTargetChecker.shouldContinueDownloadingFromSyncTarget(peer, lastRangeEnd));
  }

  @Override
  public CheckpointRange next() {
    if (!retrievedRanges.isEmpty()) {
      return retrievedRanges.poll();
    }
    if (pendingCheckpointsRequest.isPresent()) {
      return getCheckpointRangeFromPendingRequest();
    }
    pendingCheckpointsRequest =
        Optional.of(checkpointFetcher.getNextCheckpointHeaders(peer, lastRangeEnd));
    return getCheckpointRangeFromPendingRequest();
  }

  private CheckpointRange getCheckpointRangeFromPendingRequest() {
    final CompletableFuture<List<BlockHeader>> pendingRequest = pendingCheckpointsRequest.get();
    try {
      final List<BlockHeader> newCheckpointHeaders =
          pendingRequest.get(newHeaderWaitDuration, newHeaderWaitDurationUnit);
      pendingCheckpointsRequest = Optional.empty();
      if (newCheckpointHeaders.isEmpty()) {
        requestFailureCount++;
      } else {
        requestFailureCount = 0;
      }
      for (final BlockHeader checkpointHeader : newCheckpointHeaders) {
        retrievedRanges.add(new CheckpointRange(lastRangeEnd, checkpointHeader));
        lastRangeEnd = checkpointHeader;
      }
      return retrievedRanges.poll();
    } catch (final InterruptedException e) {
      LOG.trace("Interrupted while waiting for new checkpoint headers", e);
      return null;
    } catch (final ExecutionException e) {
      LOG.debug("Failed to retrieve new checkpoint headers", e);
      pendingCheckpointsRequest = Optional.empty();
      requestFailureCount++;
      return null;
    } catch (final TimeoutException e) {
      return null;
    }
  }

  public interface SyncTargetChecker {
    boolean shouldContinueDownloadingFromSyncTarget(EthPeer peer, BlockHeader lastCheckpointHeader);
  }
}
