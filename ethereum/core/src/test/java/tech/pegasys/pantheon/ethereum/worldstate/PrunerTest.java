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

import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.WorldStateKeyValueStorage;
import tech.pegasys.pantheon.ethereum.vm.TestBlockchain;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.services.kvstore.InMemoryKeyValueStorage;

import org.junit.Test;

public class PrunerTest {

  private final NoOpMetricsSystem metricsSystem = new NoOpMetricsSystem();

  @Test
  public void doesNotLoseStateDuringReorg() {
    final InMemoryKeyValueStorage markStorage = new InMemoryKeyValueStorage();
    final InMemoryKeyValueStorage stateStorage = new InMemoryKeyValueStorage();
    final WorldStateStorage worldStateStorage = new WorldStateKeyValueStorage(stateStorage);
    final WorldStateArchive worldStateArchive = new WorldStateArchive(worldStateStorage);
    final Blockchain blockchain = new TestBlockchain();
    final Pruner pruner =
        new Pruner(
            new MarkSweepPruner(worldStateStorage, markStorage, metricsSystem), blockchain, 2);
    pruner.start();
  }
}
