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
import tech.pegasys.pantheon.plugin.services.exception.StorageException;
import tech.pegasys.pantheon.plugin.services.storage.KeyValueStorage;
import tech.pegasys.pantheon.plugin.services.storage.KeyValueStorageFactory;
import tech.pegasys.pantheon.plugin.services.storage.SegmentIdentifier;
import tech.pegasys.pantheon.services.kvstore.SegmentedKeyValueStorage;
import tech.pegasys.pantheon.services.kvstore.SegmentedKeyValueStorageAdapter;
import tech.pegasys.pantheon.services.util.DatabaseMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RocksDBKeyValueStorageFactory implements KeyValueStorageFactory {

  private static final Logger LOG = LogManager.getLogger();
  private static final int DEFAULT_VERSION = 1;
  private static final Set<Integer> SUPPORTED_VERSION = Set.of(0, 1);

  private final boolean isSegmentIsolationSupported;
  private final SegmentedKeyValueStorage<?> segmentedStorage;
  private final KeyValueStorage unsegmentedStorage;

  public RocksDBKeyValueStorageFactory(
      final RocksDBConfiguration configuration,
      final List<SegmentIdentifier> segments,
      final MetricsSystem metricsSystem) {

    try {
      this.isSegmentIsolationSupported = databaseVersion(configuration) == DEFAULT_VERSION;
    } catch (final IOException e) {
      LOG.error("Failed to retrieve the RocksDB database meta version: {}", e.getMessage());
      throw new StorageException(e.getMessage(), e);
    }

    if (isSegmentIsolationSupported) {
      this.unsegmentedStorage = null;
      this.segmentedStorage =
          new RocksDBColumnarKeyValueStorage(configuration, segments, metricsSystem);
    } else {
      this.unsegmentedStorage = new RocksDBKeyValueStorage(configuration, metricsSystem);
      this.segmentedStorage = null;
    }
  }

  @Override
  public String getName() {
    return "RocksDB";
  }

  @Override
  public KeyValueStorage create(final SegmentIdentifier segment) throws StorageException {
    return isSegmentIsolationSupported
        ? new SegmentedKeyValueStorageAdapter<>(segment, segmentedStorage)
        : unsegmentedStorage;
  }

  @Override
  public boolean isSegmentIsolationSupported() {
    return isSegmentIsolationSupported;
  }

  private int databaseVersion(final RocksDBConfiguration configuration) throws IOException {
    final Path databaseDir = configuration.getDatabaseDir();
    final boolean databaseExists = databaseDir.resolve("IDENTITY").toFile().exists();
    final int databaseVersion;
    if (databaseExists) {
      databaseVersion = DatabaseMetadata.fromDirectory(databaseDir).getVersion();
      LOG.info("Existing database detected at {}. Version {}", databaseDir, databaseVersion);
    } else {
      databaseVersion = DEFAULT_VERSION;
      LOG.info(
          "No existing database detected at {}. Using version {}", databaseDir, databaseVersion);
      Files.createDirectories(databaseDir);
      new DatabaseMetadata(databaseVersion).writeToDirectory(databaseDir);
    }

    if (!SUPPORTED_VERSION.contains(databaseVersion)) {
      final String message = "Unsupported RocksDB Metadata version of: " + databaseVersion;
      LOG.error(message);
      throw new StorageException(message);
    }

    return databaseVersion;
  }
}
