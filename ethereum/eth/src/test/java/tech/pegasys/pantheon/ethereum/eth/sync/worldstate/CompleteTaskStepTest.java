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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateStorage;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import org.junit.Test;

public class CompleteTaskStepTest {

  private static final Hash ROOT_HASH = Hash.hash(BytesValue.of(1, 2, 3));
  private final WorldStateStorage worldStateStorage = mock(WorldStateStorage.class);
  private final WorldDownloadState downloadState = mock(WorldDownloadState.class);
  private final BlockHeader blockHeader =
      new BlockHeaderTestFixture().stateRoot(ROOT_HASH).buildHeader();

  private final CompleteTaskStep completeTaskStep =
      new CompleteTaskStep(worldStateStorage, new NoOpMetricsSystem());

  @Test
  public void shouldMarkTaskAsFailedIfItDoesNotHaveData() {
    final StubTask task = new StubTask(NodeDataRequest.createAccountDataRequest(ROOT_HASH));

    completeTaskStep.markAsCompleteOrFailed(blockHeader, downloadState, task);

    assertThat(task.isCompleted()).isFalse();
    assertThat(task.isFailed()).isTrue();
    verify(downloadState).notifyTaskAvailable();
  }

  @Test
  public void shouldEnqueueChildrenAndMarkCompleteWhenTaskHasData() {
    // Use an arbitrary but actually valid trie node to get children from.
    final Hash hash =
        Hash.fromHexString("0xbb1f7ff2c72bfc5aeab245db5951352717cb9b236965a556a56de4ccdac8f85c");
    final BytesValue data =
        BytesValue.fromHexString(
            "0xf843a02067b0e3c82238f4f2d1a50a8b3a44f96311d77b148c30dc0ef863e1a060dcb6a1a0e8bf75989ccc3c3918b8ff26d0a494e090e4bf7dff75e064ddf06e4271bafeb4");
    final StubTask task = new StubTask(NodeDataRequest.createAccountDataRequest(hash));
    task.getData().setData(data);

    completeTaskStep.markAsCompleteOrFailed(blockHeader, downloadState, task);

    assertThat(task.isCompleted()).isTrue();
    assertThat(task.isFailed()).isFalse();
    verify(downloadState).enqueueRequests(refEq(task.getData().getChildRequests()));
    verify(downloadState).checkCompletion(worldStateStorage, blockHeader);
  }
}
