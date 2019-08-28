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
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.plugin.PantheonContext;
import tech.pegasys.pantheon.plugin.PantheonPlugin;
import tech.pegasys.pantheon.plugin.services.PantheonConfiguration;
import tech.pegasys.pantheon.plugin.services.PicoCLIOptions;
import tech.pegasys.pantheon.plugin.services.StorageService;
import tech.pegasys.pantheon.plugin.services.storage.SegmentIdentifier;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RocksDBPlugin implements PantheonPlugin {

  private static final Logger LOG = LogManager.getLogger();
  private static final String NAME = "rocksdb";

  private final RocksDBCLIOptions options;
  private PantheonContext context;
  private RocksDBKeyValueStorageFactory factory;
  private RocksDBKeyValuePrivacyStorageFactory privacyFactory;

  public RocksDBPlugin() {
    this.options = RocksDBCLIOptions.create();
  }

  @Override
  public void register(final PantheonContext context) {
    LOG.info("Registering plugin");
    this.context = context;

    context
        .getService(PicoCLIOptions.class)
        .ifPresent(picoCLIOptions -> picoCLIOptions.addPicoCLIOptions(NAME, options));
    LOG.info("Plugin registered.");
  }

  @Override
  public void start() {
    LOG.info("Starting plugin.");
    LOG.debug("Applied configuration: {}", options.toString());
    context
        .getService(StorageService.class)
        .ifPresentOrElse(
            this::createAndRegister,
            () -> LOG.error("Failed to start due to missing StorageService."));
  }

  @Override
  public void stop() {
    try {
      if (factory != null) {
        factory.close();
      }

      if (privacyFactory != null) {
        privacyFactory.close();
      }
    } catch (final IOException e) {
      LOG.error("Failed to stop plugin: " + e.getMessage(), e);
    }
  }

  private void createAndRegister(final StorageService service) {
    // TODO get the metrics system from a service (code will arrive soon on master - MT)
    final MetricsSystem metricsSystem = new NoOpMetricsSystem();

    context
        .getService(PantheonConfiguration.class)
        .ifPresentOrElse(
            (commonConfiguration) -> {
              final Path dataDir = commonConfiguration.getStoragePath();
              final RocksDBConfiguration configuration =
                  options.toDomainObject().databaseDir(dataDir).build();
              final List<SegmentIdentifier> segments = service.getAllSegmentIdentifiers();

              factory = new RocksDBKeyValueStorageFactory(configuration, segments, metricsSystem);
              privacyFactory =
                  new RocksDBKeyValuePrivacyStorageFactory(dataDir, segments, metricsSystem);

              service.registerKeyValueStorage(factory);
              service.registerKeyValueStorage(privacyFactory);
            },
            () -> LOG.error("Failed to start due to missing PantheonConfiguration."));
  }
}
