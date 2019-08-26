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
package tech.pegasys.pantheon.ethereum.storage.keyvalue;

import static com.google.common.base.Preconditions.checkNotNull;
import static tech.pegasys.pantheon.ethereum.storage.keyvalue.KeyValueSegmentIdentifier.BLOCKCHAIN;
import static tech.pegasys.pantheon.ethereum.storage.keyvalue.KeyValueSegmentIdentifier.PRIVATE_STATE;
import static tech.pegasys.pantheon.ethereum.storage.keyvalue.KeyValueSegmentIdentifier.PRIVATE_TRANSACTIONS;
import static tech.pegasys.pantheon.ethereum.storage.keyvalue.KeyValueSegmentIdentifier.PRUNING_STATE;
import static tech.pegasys.pantheon.ethereum.storage.keyvalue.KeyValueSegmentIdentifier.WORLD_STATE;

import tech.pegasys.pantheon.plugin.services.storage.KeyValueStorage;
import tech.pegasys.pantheon.plugin.services.storage.KeyValueStorageFactory;
import tech.pegasys.pantheon.services.kvstore.LimitedInMemoryKeyValueStorage;
import tech.pegasys.pantheon.services.kvstore.KeyValueStorageMetrics;

public class KeyValueStorageProviderBuilder {

  private static final long DEFAULT_WORLD_STATE_PREIMAGE_CACHE_SIZE = 5_000L;

  private long worldStatePreimageCacheSize = DEFAULT_WORLD_STATE_PREIMAGE_CACHE_SIZE;
  private KeyValueStorageFactory storageFactory;

  private KeyValueStorageMetrics metrics;

  public KeyValueStorageProviderBuilder withStorageFactory(
      final KeyValueStorageFactory storageFactory) {
    this.storageFactory = storageFactory;
    return this;
  }

  public KeyValueStorageProviderBuilder withMetrics(final KeyValueStorageMetrics metrics) {
    this.metrics = metrics;
    return this;
  }

  public KeyValueStorageProviderBuilder withWorldStatePreimageCacheSize(
      final long worldStatePreimageCacheSize) {
    this.worldStatePreimageCacheSize = worldStatePreimageCacheSize;
    return this;
  }

  public KeyValueStorageProvider build() {
    checkNotNull(storageFactory, "Cannot build a storage provider without a storage factory.");

    final KeyValueStorage worldStatePreImageStorage =
        new LimitedInMemoryKeyValueStorage(worldStatePreimageCacheSize);

    return new KeyValueStorageProvider(
        addMetrics(storageFactory.create(BLOCKCHAIN)),
        addMetrics(storageFactory.create(WORLD_STATE)),
        addMetrics(worldStatePreImageStorage),
        addMetrics(storageFactory.create(PRIVATE_TRANSACTIONS)),
        addMetrics(storageFactory.create(PRIVATE_STATE)),
        addMetrics(storageFactory.create(PRUNING_STATE)),
        storageFactory.isSegmentIsolationSupported());
  }

  private KeyValueStorage addMetrics(final KeyValueStorage storage){
    return metrics == null ? storage : new KeyValueStorageMetricsDecorator(storage, metrics);
  }
}
