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
import tech.pegasys.pantheon.services.queue.TaskQueue.Task;

public class EnqueueChildrenStep {

  public Task<NodeDataRequest> enqueueChildren(
      final Task<NodeDataRequest> task,
      final BlockHeader blockHeader,
      final WorldDownloadState downloadState) {
    final NodeDataRequest request = task.getData();
    // Only queue rootnode children if we started from scratch
    if (request.getData() != null
        && (!downloadState.downloadWasResumed() || !isRootState(blockHeader, request))) {

      downloadState.enqueueRequests(request.getChildRequests());
    }
    return task;
  }

  private boolean isRootState(final BlockHeader blockHeader, final NodeDataRequest request) {
    return request.getHash().equals(blockHeader.getStateRoot());
  }
}
