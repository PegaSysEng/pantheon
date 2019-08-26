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

import tech.pegasys.pantheon.plugin.services.exception.StorageException;
import tech.pegasys.pantheon.plugin.services.storage.KeyValueStorage;
import tech.pegasys.pantheon.plugin.services.storage.KeyValueStorageTransaction;
import tech.pegasys.pantheon.services.kvstore.KeyValueStorageTransactionTransitionValidatorDecorator;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.LRUCache;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Statistics;
import org.rocksdb.TransactionDB;
import org.rocksdb.TransactionDBOptions;
import org.rocksdb.WriteOptions;

public class RocksDBKeyValueStorage implements KeyValueStorage {

  private static final Logger LOG = LogManager.getLogger();

  private final Options options;
  private final TransactionDBOptions txOptions;
  private final TransactionDB db;
  private final AtomicBoolean closed = new AtomicBoolean(false);

  public RocksDBKeyValueStorage(final RocksDBConfiguration rocksDbConfiguration) {
    RocksDbUtil.loadNativeLibrary();
    try {
      final Statistics stats = new Statistics();
      options =
          new Options()
              .setCreateIfMissing(true)
              .setMaxOpenFiles(rocksDbConfiguration.getMaxOpenFiles())
              .setTableFormatConfig(createBlockBasedTableConfig(rocksDbConfiguration))
              .setMaxBackgroundCompactions(rocksDbConfiguration.getMaxBackgroundCompactions())
              .setStatistics(stats);
      options.getEnv().setBackgroundThreads(rocksDbConfiguration.getBackgroundThreadCount());

      txOptions = new TransactionDBOptions();
      db = TransactionDB.open(options, txOptions, rocksDbConfiguration.getDatabaseDir().toString());
    } catch (final RocksDBException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public void clear() throws StorageException {
    try (final RocksIterator rocksIterator = db.newIterator()) {
      if (!rocksIterator.isValid()) {
        return;
      }
      rocksIterator.seekToFirst();
      final byte[] firstKey = rocksIterator.key();
      rocksIterator.seekToLast();
      if (!rocksIterator.isValid()) {
        return;
      }
      db.deleteRange(firstKey, rocksIterator.key());
    } catch (final RocksDBException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public boolean containsKey(final byte[] key) throws StorageException {
    return get(key).isPresent();
  }

  @Override
  public Optional<byte[]> get(final byte[] key) throws StorageException {
    throwIfClosed();

    try {
      return Optional.ofNullable(db.get(key));
    } catch (final RocksDBException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public long removeAllKeysUnless(final Predicate<byte[]> retainCondition) throws StorageException {
    long removedNodeCounter = 0;
    try (final RocksIterator rocksIterator = db.newIterator()) {
      rocksIterator.seekToFirst();
      while (rocksIterator.isValid()) {
        final byte[] key = rocksIterator.key();
        if (!retainCondition.test(key)) {
          removedNodeCounter++;
          db.delete(key);
        }
        rocksIterator.next();
      }
    } catch (final RocksDBException e) {
      throw new StorageException(e);
    }
    return removedNodeCounter;
  }

  @Override
  public KeyValueStorageTransaction startTransaction() throws StorageException {
    throwIfClosed();
    final WriteOptions options = new WriteOptions();
    return new KeyValueStorageTransactionTransitionValidatorDecorator(
        new RocksDBTransaction(db.beginTransaction(options), options));
  }

  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      txOptions.close();
      options.close();
      db.close();
    }
  }

  private BlockBasedTableConfig createBlockBasedTableConfig(final RocksDBConfiguration config) {
    final LRUCache cache = new LRUCache(config.getCacheCapacity());
    return new BlockBasedTableConfig().setBlockCache(cache);
  }

  private void throwIfClosed() {
    if (closed.get()) {
      LOG.error("Attempting to use a closed RocksDBKeyValueStorage");
      throw new IllegalStateException("Storage has been closed");
    }
  }
}
