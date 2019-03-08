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

import static tech.pegasys.pantheon.services.pipeline.PipelineBuilder.createPipelineFrom;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.exceptions.EthTaskException;
import tech.pegasys.pantheon.ethereum.eth.manager.task.RetryingGetNodeDataFromPeerTask;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateStorage;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateStorage.Updater;
import tech.pegasys.pantheon.metrics.Counter;
import tech.pegasys.pantheon.metrics.LabelledMetric;
import tech.pegasys.pantheon.metrics.MetricCategory;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.services.pipeline.Pipeline;
import tech.pegasys.pantheon.services.queue.TaskQueue;
import tech.pegasys.pantheon.services.queue.TaskQueue.Task;
import tech.pegasys.pantheon.util.ExceptionUtils;
import tech.pegasys.pantheon.util.FutureUtils;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldStateDownloader {
  private static final Logger LOG = LogManager.getLogger();

  private final Counter completedRequestsCounter;
  private final Counter retriedRequestsCounter;
  private final Counter existingNodeCounter;
  private final MetricsSystem metricsSystem;

  private final EthContext ethContext;
  private final TaskQueue<NodeDataRequest> taskQueue;
  private final int hashCountPerRequest;
  private final int maxOutstandingRequests;
  private final int maxNodeRequestsWithoutProgress;
  private final WorldStateStorage worldStateStorage;

  private final AtomicReference<WorldDownloadState> downloadState = new AtomicReference<>();
  private final LabelledMetric<Counter> pipelineStepCounter;

  public WorldStateDownloader(
      final EthContext ethContext,
      final WorldStateStorage worldStateStorage,
      final TaskQueue<NodeDataRequest> taskQueue,
      final int hashCountPerRequest,
      final int maxOutstandingRequests,
      final int maxNodeRequestsWithoutProgress,
      final MetricsSystem metricsSystem) {
    this.ethContext = ethContext;
    this.worldStateStorage = worldStateStorage;
    this.taskQueue = taskQueue;
    this.hashCountPerRequest = hashCountPerRequest;
    this.maxOutstandingRequests = maxOutstandingRequests;
    this.maxNodeRequestsWithoutProgress = maxNodeRequestsWithoutProgress;
    this.metricsSystem = metricsSystem;

    metricsSystem.createLongGauge(
        MetricCategory.SYNCHRONIZER,
        "world_state_pending_requests_current",
        "Number of pending requests for fast sync world state download",
        taskQueue::size);

    completedRequestsCounter =
        metricsSystem.createCounter(
            MetricCategory.SYNCHRONIZER,
            "world_state_completed_requests_total",
            "Total number of node data requests completed as part of fast sync world state download");
    retriedRequestsCounter =
        metricsSystem.createCounter(
            MetricCategory.SYNCHRONIZER,
            "world_state_retried_requests_total",
            "Total number of node data requests repeated as part of fast sync world state download");

    existingNodeCounter =
        metricsSystem.createCounter(
            MetricCategory.SYNCHRONIZER,
            "world_state_existing_nodes_total",
            "Total number of node data requests completed using existing data");

    metricsSystem.createIntegerGauge(
        MetricCategory.SYNCHRONIZER,
        "world_state_node_requests_since_last_progress_current",
        "Number of world state requests made since the last time new data was returned",
        downloadStateValue(WorldDownloadState::getRequestsSinceLastProgress));

    pipelineStepCounter =
        metricsSystem.createLabelledCounter(
            MetricCategory.SYNCHRONIZER,
            "world_state_pipeline_processed_total",
            "Number of entries processed by each world state download pipeline stage",
            "step");
  }

  private Supplier<Integer> downloadStateValue(final Function<WorldDownloadState, Integer> getter) {
    return () -> {
      final WorldDownloadState state = this.downloadState.get();
      return state != null ? getter.apply(state) : 0;
    };
  }

  public CompletableFuture<Void> run(final BlockHeader header) {
    synchronized (this) {
      final WorldDownloadState oldDownloadState = this.downloadState.get();
      if (oldDownloadState != null && oldDownloadState.isDownloading()) {
        final CompletableFuture<Void> failed = new CompletableFuture<>();
        failed.completeExceptionally(
            new IllegalStateException(
                "Cannot run an already running " + this.getClass().getSimpleName()));
        return failed;
      }

      final Hash stateRoot = header.getStateRoot();
      if (worldStateStorage.isWorldStateAvailable(stateRoot)) {
        LOG.info(
            "World state already available for block {} ({}). State root {}",
            header.getNumber(),
            header.getHash(),
            stateRoot);
        return CompletableFuture.completedFuture(null);
      }
      LOG.info(
          "Begin downloading world state from peers for block {} ({}). State root {}",
          header.getNumber(),
          header.getHash(),
          stateRoot);

      // Room for the requests we expect to do in parallel plus some buffer but not unlimited.
      final int persistenceQueueCapacity = hashCountPerRequest * maxOutstandingRequests * 2;
      final WorldDownloadState newDownloadState =
          new WorldDownloadState(
              taskQueue,
              new ArrayBlockingQueue<>(persistenceQueueCapacity),
              maxOutstandingRequests,
              maxNodeRequestsWithoutProgress);
      this.downloadState.set(newDownloadState);

      if (!newDownloadState.downloadWasResumed()) {
        // Only queue the root node if we're starting a new download from scratch
        newDownloadState.enqueueRequest(NodeDataRequest.createAccountDataRequest(stateRoot));
      }

      final Pipeline pipeline =
          createPipelineFrom(new TaskQueueIterator(newDownloadState), persistenceQueueCapacity)
              .thenProcess(this::checkForLocalData)
              .inBatches(hashCountPerRequest)
              .thenProcessAsync(
                  requestTasks -> sendRequestForData(requestTasks, header, newDownloadState),
                  maxOutstandingRequests)
              .thenProcess(tasks -> persistBatch(tasks, header, newDownloadState))
              .theFlatMap(List::stream, persistenceQueueCapacity)
              .thenProcessInParallel(task -> enqueueChildren(task, header, newDownloadState), 3)
              .andFinishWith(task -> markAsCompleteOrFailed(header, newDownloadState, task));
      final ExecutorService pipelinePool = Executors.newCachedThreadPool();
      final CompletableFuture<Void> pipelineFuture =
          pipeline
              .start(pipelinePool)
              .thenApply(
                  unknown -> {
                    LOG.info("Pipeline completed");
                    pipelinePool.shutdown();
                    return null;
                  });
      pipelineFuture.exceptionally(
          error -> {
            LOG.error("Pipeline failed", error);
            return null;
          });
      FutureUtils.propagateResult(pipelineFuture, newDownloadState.getDownloadFuture());
      return newDownloadState.getDownloadFuture();
    }
  }

  private void markAsCompleteOrFailed(
      final BlockHeader header,
      final WorldDownloadState newDownloadState,
      final Task<NodeDataRequest> task) {
    pipelineStepCounter.labels("complete").inc();
    if (task.getData().getData() != null) {
      completedRequestsCounter.inc();
      task.markCompleted();
      newDownloadState.checkCompletion(worldStateStorage, header);
    } else {
      retriedRequestsCounter.inc();
      task.markFailed();
    }
  }

  private Task<NodeDataRequest> enqueueChildren(
      final Task<NodeDataRequest> task,
      final BlockHeader blockHeader,
      final WorldDownloadState downloadState) {
    pipelineStepCounter.labels("enqueueChildren").inc();
    final NodeDataRequest request = task.getData();
    // Only queue rootnode children if we started from scratch
    if (request.getData() != null
        && (!downloadState.downloadWasResumed() || !isRootState(blockHeader, request))) {

      downloadState.enqueueRequests(request.getChildRequests());
    }
    return task;
  }

  private Task<NodeDataRequest> checkForLocalData(final Task<NodeDataRequest> task) {
    pipelineStepCounter.labels("checkForLocalData").inc();
    final NodeDataRequest request = task.getData();
    worldStateStorage
        .getNodeData(request.getHash())
        .ifPresent(
            data -> {
              existingNodeCounter.inc();
              request.setData(data);
              request.setRequiresPersisting(false);
            });
    return task;
  }

  private CompletableFuture<List<Task<NodeDataRequest>>> sendRequestForData(
      final List<Task<NodeDataRequest>> requestTasks,
      final BlockHeader blockHeader,
      final WorldDownloadState downloadState) {
    pipelineStepCounter.labels("sendRequestForData").inc(requestTasks.size());
    final List<Hash> hashes =
        requestTasks.stream()
            .map(Task::getData)
            .map(NodeDataRequest::getHash)
            .distinct()
            .collect(Collectors.toList());
    return sendRequest(blockHeader, hashes)
        .thenApply(
            data -> {
              downloadState.requestComplete(!data.isEmpty());
              return data;
            })
        .exceptionally(
            error -> {
              final Throwable rootCause = ExceptionUtils.rootCause(error);
              if (!(rootCause instanceof TimeoutException
                  || rootCause instanceof InterruptedException
                  || rootCause instanceof CancellationException
                  || rootCause instanceof EthTaskException)) {
                LOG.debug("GetNodeDataRequest failed", error);
              }
              return Collections.emptyMap();
            })
        .thenApply(
            data -> {
              for (final Task<NodeDataRequest> task : requestTasks) {
                final NodeDataRequest request = task.getData();
                final BytesValue matchingData = data.get(request.getHash());
                if (matchingData != null) {
                  request.setData(matchingData);
                }
              }
              return requestTasks;
            });
  }

  private CompletableFuture<Map<Hash, BytesValue>> sendRequest(
      final BlockHeader blockHeader, final List<Hash> hashes) {
    return RetryingGetNodeDataFromPeerTask.forHashes(
            ethContext, hashes, blockHeader.getNumber(), metricsSystem)
        .run();
  }

  private List<Task<NodeDataRequest>> persistBatch(
      final List<Task<NodeDataRequest>> tasks,
      final BlockHeader blockHeader,
      final WorldDownloadState downloadState) {
    pipelineStepCounter.labels("persistBatch").inc(tasks.size());
    final Updater updater = worldStateStorage.updater();
    tasks.stream()
        .map(Task::getData)
        .filter(request -> request.getData() != null)
        .forEach(
            request -> {
              if (isRootState(blockHeader, request)) {
                downloadState.setRootNodeData(request.getData());
              } else {
                request.persist(updater);
              }
            });
    updater.commit();
    return tasks;
  }

  private class TaskQueueIterator implements Iterator<Task<NodeDataRequest>> {

    private final WorldDownloadState downloadState;

    public TaskQueueIterator(final WorldDownloadState downloadState) {
      this.downloadState = downloadState;
    }

    @Override
    public boolean hasNext() {
      return downloadState.isDownloading();
    }

    @Override
    public Task<NodeDataRequest> next() {
      pipelineStepCounter.labels("source").inc();
      return downloadState.dequeueRequestBlocking();
    }
  }

  public void cancel() {
    synchronized (this) {
      final WorldDownloadState downloadState = this.downloadState.get();
      if (downloadState != null) {
        downloadState.getDownloadFuture().cancel(true);
      }
    }
  }

  private boolean isRootState(final BlockHeader blockHeader, final NodeDataRequest request) {
    return request.getHash().equals(blockHeader.getStateRoot());
  }
}
