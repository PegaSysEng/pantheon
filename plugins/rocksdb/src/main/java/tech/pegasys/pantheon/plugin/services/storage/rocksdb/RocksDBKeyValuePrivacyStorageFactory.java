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
package tech.pegasys.pantheon.plugin.services.storage.rocksdb;

import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.plugin.services.storage.SegmentIdentifier;

import java.nio.file.Path;
import java.util.List;

@Deprecated
public class RocksDBKeyValuePrivacyStorageFactory extends RocksDBKeyValueStorageFactory {

  private static final String PRIVATE_DATABASE_PATH = "private";

  public RocksDBKeyValuePrivacyStorageFactory(
      final Path dataDir,
      final List<SegmentIdentifier> segments,
      final MetricsSystem metricsSystem) {
    super(getPrivacyConfiguration(dataDir), segments, metricsSystem);
  }

  @Override
  public String getName() {
    return "rocksdb-privacy";
  }

  private static RocksDBConfiguration getPrivacyConfiguration(final Path dataDir) {
    final Path privateDbPath = dataDir.resolve(PRIVATE_DATABASE_PATH);

    return RocksDBConfiguration.builder().databaseDir(privateDbPath).label("private_state").build();
  }
}
