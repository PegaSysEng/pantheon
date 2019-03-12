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
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateStorage;
import tech.pegasys.pantheon.metrics.Counter;
import tech.pegasys.pantheon.metrics.MetricCategory;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.services.queue.TaskQueue.Task;

public class CompleteTaskStep {

  private final WorldStateStorage worldStateStorage;
  private final Counter completedRequestsCounter;
  private final Counter retriedRequestsCounter;

  public CompleteTaskStep(
      final WorldStateStorage worldStateStorage, final MetricsSystem metricsSystem) {
    this.worldStateStorage = worldStateStorage;

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
  }

  public void markAsCompleteOrFailed(
      final BlockHeader header,
      final WorldDownloadState downloadState,
      final Task<NodeDataRequest> task) {
    if (task.getData().getData() != null) {
      completedRequestsCounter.inc();
      task.markCompleted();
      if (downloadState.shouldRequestRootNode()) {
        downloadState.enqueueRequest(
            NodeDataRequest.createAccountDataRequest(header.getStateRoot()));
      }
      downloadState.checkCompletion(worldStateStorage, header);
    } else {
      retriedRequestsCounter.inc();
      task.markFailed();
    }
  }
}
