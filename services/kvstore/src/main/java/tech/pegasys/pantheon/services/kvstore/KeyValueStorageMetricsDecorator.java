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
package tech.pegasys.pantheon.services.kvstore;

import tech.pegasys.pantheon.metrics.OperationTimer;
import tech.pegasys.pantheon.plugin.services.exception.StorageException;
import tech.pegasys.pantheon.plugin.services.storage.KeyValueStorage;
import tech.pegasys.pantheon.plugin.services.storage.KeyValueStorageTransaction;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

public class KeyValueStorageMetricsDecorator implements KeyValueStorage {

  private final KeyValueStorageMetrics metrics;
  private final KeyValueStorage storage;

  public KeyValueStorageMetricsDecorator(
      final KeyValueStorage storage, final KeyValueStorageMetrics metrics) {
    this.storage = storage;
    this.metrics = metrics;
  }

  @Override
  public void clear() throws StorageException {
    storage.clear();
  }

  @Override
  public void close() throws IOException {
    storage.close();
  }

  @Override
  public boolean containsKey(final byte[] key) throws StorageException {
    return storage.containsKey(key);
  }

  @Override
  public Optional<byte[]> get(final byte[] key) throws StorageException {
    try (final OperationTimer.TimingContext ignored = metrics.getReadLatency().startTimer()) {
      return storage.get(key);
    }
  }

  @Override
  public long removeAllKeysUnless(final Predicate<byte[]> retainCondition) throws StorageException {
    return storage.removeAllKeysUnless(retainCondition);
  }

  @Override
  public KeyValueStorageTransaction startTransaction() throws StorageException {
    return new KeyValueStorageTransactionMetricsDecorator(storage.startTransaction());
  }

  class KeyValueStorageTransactionMetricsDecorator implements KeyValueStorageTransaction {

    private final KeyValueStorageTransaction transaction;

    private KeyValueStorageTransactionMetricsDecorator(
        final KeyValueStorageTransaction transaction) {
      this.transaction = transaction;
    }

    @Override
    public void put(final byte[] key, final byte[] value) {
      try (final OperationTimer.TimingContext ignored = metrics.getWriteLatency().startTimer()) {
        transaction.put(key, value);
      }
    }

    @Override
    public void remove(final byte[] key) {
      try (final OperationTimer.TimingContext ignored = metrics.getRemoveLatency().startTimer()) {
        transaction.remove(key);
      }
    }

    @Override
    public void commit() throws StorageException {
      try (final OperationTimer.TimingContext ignored = metrics.getCommitLatency().startTimer()) {
        transaction.commit();
      }
    }

    @Override
    public void rollback() {
      transaction.rollback();
      metrics.getRollbackCount().inc();
    }
  }
}
