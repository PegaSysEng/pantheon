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
package tech.pegasys.pantheon.ethereum.eth.sync.tasks;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.TransactionReceipt;
import tech.pegasys.pantheon.ethereum.eth.manager.EthTask;
import tech.pegasys.pantheon.ethereum.eth.manager.ethtaskutils.RetryingMessageTaskTest;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetReceiptsForHeadersTaskTest
    extends RetryingMessageTaskTest<Map<BlockHeader, List<TransactionReceipt>>> {

  @Override
  protected Map<BlockHeader, List<TransactionReceipt>> generateDataToBeRequested() {
    // Setup data to be requested and expected response
    final Map<BlockHeader, List<TransactionReceipt>> blocks = new HashMap<>();
    for (long i = 0; i < 3; i++) {
      final BlockHeader header = blockchain.getBlockHeader(10 + i).get();
      blocks.put(header, blockchain.getTxReceipts(header.getHash()).get());
    }
    return blocks;
  }

  @Override
  protected EthTask<Map<BlockHeader, List<TransactionReceipt>>> createTask(
      final Map<BlockHeader, List<TransactionReceipt>> requestedData) {
    final List<BlockHeader> headersToComplete = new ArrayList<>(requestedData.keySet());
    return GetReceiptsForHeadersTask.forHeaders(
        ethContext, headersToComplete, maxRetries, NoOpMetricsSystem.NO_OP_LABELLED_TIMER);
  }
}
