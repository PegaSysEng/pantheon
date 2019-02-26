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
package tech.pegasys.pantheon.ethereum.eth.sync.worldstate;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.eth.manager.task.EthTask;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateStorage;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateStorage.Updater;
import tech.pegasys.pantheon.services.queue.TaskQueue;
import tech.pegasys.pantheon.services.queue.TaskQueue.Task;
import tech.pegasys.pantheon.util.ExceptionUtils;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class WorldDownloadState {
  private static final Logger LOG = LogManager.getLogger();

  private final TaskQueue<NodeDataRequest> pendingRequests;
  private final ArrayBlockingQueue<Task<NodeDataRequest>> requestsToPersist;
  private final int maxOutstandingRequests;
  private final Set<EthTask<?>> outstandingRequests =
      Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final AtomicBoolean sendingRequests = new AtomicBoolean(false);
  private final CompletableFuture<Void> future;
  private final CompletableFuture<Void> tearDownFuture;
  private boolean waitingForNewPeer = false;
  private BytesValue rootNodeData;
  private EthTask<?> persistenceTask;

  public WorldDownloadState(
      final TaskQueue<NodeDataRequest> pendingRequests,
      final ArrayBlockingQueue<Task<NodeDataRequest>> requestsToPersist,
      final int maxOutstandingRequests) {
    this.pendingRequests = pendingRequests;
    this.requestsToPersist = requestsToPersist;
    this.maxOutstandingRequests = maxOutstandingRequests;
    this.future = new CompletableFuture<>();
    this.tearDownFuture = new CompletableFuture<>();
    this.future.whenComplete(this::doCancel);
    this.tearDownFuture.exceptionally(
        error -> {
          // Propagate cancellation back to our internal future.
          if (error instanceof CancellationException) {
            this.future.cancel(true);
          }
          return null;
        });
  }

  private synchronized void doCancel(final Void result, final Throwable error) {
    // Handle cancellations
    if (future.isCancelled()) {
      LOG.info("World state download cancelled");
    } else if (error != null) {
      if (!(ExceptionUtils.rootCause(error) instanceof StalledDownloadException)) {
        LOG.info("World state download failed. ", error);
      }
    }
    if (persistenceTask != null) {
      persistenceTask.cancel();
    }
    for (final EthTask<?> outstandingRequest : outstandingRequests) {
      outstandingRequest.cancel();
    }
    pendingRequests.clear();
    requestsToPersist.clear();
    if (error != null) {
      tearDownFuture.completeExceptionally(error);
    } else {
      tearDownFuture.complete(result);
    }
  }

  public void whileAdditionalRequestCanBeSent(final Runnable action) {
    while (shouldRequestNodeData()) {
      if (sendingRequests.compareAndSet(false, true)) {
        try {
          action.run();
        } finally {
          sendingRequests.set(false);
        }
      } else {
        break;
      }
    }
  }

  public synchronized void setWaitingForNewPeer(final boolean waitingForNewPeer) {
    this.waitingForNewPeer = waitingForNewPeer;
  }

  public synchronized void addOutstandingTask(final EthTask<?> task) {
    outstandingRequests.add(task);
  }

  public synchronized void removeOutstandingTask(final EthTask<?> task) {
    outstandingRequests.remove(task);
  }

  public int getOutstandingRequestCount() {
    return outstandingRequests.size();
  }

  private synchronized boolean shouldRequestNodeData() {
    return !future.isDone()
        && outstandingRequests.size() < maxOutstandingRequests
        && !pendingRequests.isEmpty()
        && !waitingForNewPeer;
  }

  public CompletableFuture<Void> getFuture() {
    return tearDownFuture;
  }

  public synchronized void setPersistenceTask(final EthTask<?> persistenceTask) {
    this.persistenceTask = persistenceTask;
  }

  public synchronized void enqueueRequest(final NodeDataRequest request) {
    if (!future.isDone()) {
      pendingRequests.enqueue(request);
    }
  }

  public synchronized void enqueueRequests(final Stream<NodeDataRequest> requests) {
    if (!future.isDone()) {
      requests.forEach(pendingRequests::enqueue);
    }
  }

  public synchronized void markAsFailed(final Task<NodeDataRequest> task) {
    if (!future.isDone()) {
      task.markFailed();
    }
  }

  public synchronized Task<NodeDataRequest> dequeueRequest() {
    if (future.isDone()) {
      return null;
    }
    return pendingRequests.dequeue();
  }

  public synchronized void setRootNodeData(final BytesValue rootNodeData) {
    this.rootNodeData = rootNodeData;
  }

  public ArrayBlockingQueue<Task<NodeDataRequest>> getRequestsToPersist() {
    return requestsToPersist;
  }

  public void addToPersistenceQueue(final Task<NodeDataRequest> task) {
    while (!future.isDone()) {
      try {
        if (requestsToPersist.offer(task, 1, TimeUnit.SECONDS)) {
          break;
        }
      } catch (final InterruptedException e) {
        markAsFailed(task);
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  public int getPersistenceQueueSize() {
    return requestsToPersist.size();
  }

  public synchronized void markAsStalled(final int maxNodeRequestRetries) {
    final String message =
        "Download stalled due to too many failures to retrieve node data (>"
            + maxNodeRequestRetries
            + " failures)";
    final WorldStateDownloaderException e = new StalledDownloadException(message);
    future.completeExceptionally(e);
  }

  public synchronized boolean checkCompletion(
      final WorldStateStorage worldStateStorage, final BlockHeader header) {
    if (!future.isDone() && pendingRequests.allTasksCompleted()) {
      final Updater updater = worldStateStorage.updater();
      updater.putAccountStateTrieNode(header.getStateRoot(), rootNodeData);
      updater.commit();
      future.complete(null);
      LOG.info("Finished downloading world state from peers");
      return true;
    } else {
      return false;
    }
  }

  public synchronized boolean isDownloading() {
    return !future.isDone();
  }
}
