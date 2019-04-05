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
package tech.pegasys.pantheon.ethereum.core;

import static java.nio.charset.StandardCharsets.UTF_8;

import tech.pegasys.pantheon.ethereum.privacy.PrivateStateStorage;
import tech.pegasys.pantheon.ethereum.privacy.PrivateTransactionStorage;
import tech.pegasys.pantheon.ethereum.storage.StorageProvider;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.RocksDbStorageProvider;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateArchive;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateStorage;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.services.kvstore.RocksDbConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import com.google.common.io.Files;

public class PrivacyParametersBuilder {
  private final String PRIVATE_DATABASE_PATH = "private";
  private final String PRIVATE_STATE_DATABASE_PATH = "privateState";

  private boolean enabled;
  private URI enclaveUrl;
  private Integer privacyAddress = Address.PRIVACY;
  private MetricsSystem metricsSystem = new NoOpMetricsSystem();
  private Path dataDir;
  private File enclavePublicKeyFile;
  private String enclavePublicKey;

  public PrivacyParametersBuilder setPrivacyAddress(final Integer privacyAddress) {
    this.privacyAddress = privacyAddress;
    return this;
  }

  public PrivacyParametersBuilder setEnclaveUrl(final URI enclaveUrl) {
    this.enclaveUrl = enclaveUrl;
    return this;
  }

  public PrivacyParametersBuilder setEnabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public PrivacyParametersBuilder setMetricsSystem(final MetricsSystem metricsSystem) {
    this.metricsSystem = metricsSystem;
    return this;
  }

  public PrivacyParametersBuilder setDataDir(final Path dataDir) {
    this.dataDir = dataDir;
    return this;
  }

  public PrivacyParameters build() throws IOException {
    PrivacyParameters config = new PrivacyParameters();
    if (enabled) {
      Path privateDbPath = dataDir.resolve(PRIVATE_DATABASE_PATH);
      StorageProvider privateStorageProvider =
          RocksDbStorageProvider.create(
              new RocksDbConfiguration.Builder().databaseDir(privateDbPath).build(), metricsSystem);
      WorldStateStorage privateWorldStateStorage = privateStorageProvider.createWorldStateStorage();
      WorldStateArchive privateWorldStateArchive = new WorldStateArchive(privateWorldStateStorage);

      Path privateStateDbPath = dataDir.resolve(PRIVATE_STATE_DATABASE_PATH);
      StorageProvider privateStateStorageProvider =
          RocksDbStorageProvider.create(
              new RocksDbConfiguration.Builder().databaseDir(privateStateDbPath).build(),
              metricsSystem);
      PrivateTransactionStorage privateTransactionStorage =
          privateStateStorageProvider.createPrivateTransactionStorage();
      PrivateStateStorage privateStateStorage =
          privateStateStorageProvider.createPrivateStateStorage();

      config.setPrivateWorldStateArchive(privateWorldStateArchive);
      config.setEnclavePublicKey(enclavePublicKey);
      config.setEnclavePublicKeyFile(enclavePublicKeyFile);
      config.setPrivateStorageProvider(privateStorageProvider);
      config.setPrivateTransactionStorage(privateTransactionStorage);
      config.setPrivateStateStorage(privateStateStorage);
    }
    config.setEnabled(enabled);
    config.setEnclaveUri(enclaveUrl);
    config.setPrivacyAddress(privacyAddress);
    return config;
  }

  public PrivacyParametersBuilder setEnclavePublicKeyUsingFile(final File publicKeyFile)
      throws IOException {
    this.enclavePublicKeyFile = publicKeyFile;
    this.enclavePublicKey = Files.asCharSource(publicKeyFile, UTF_8).read();
    return this;
  }
}
