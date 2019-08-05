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

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

public class RocksDBKeyValueStorage implements KeyValueStorage {

  @Override
  public void clear() throws StorageException {
    // TODO code
  }

  @Override
  public Optional<byte[]> get(final byte[] key) throws StorageException {
    return Optional.empty();
    // TODO code
  }

  @Override
  public long removeAllKeysUnless(final Predicate<byte[]> retainCondition) throws StorageException {
    // TODO code
    return 0;
  }

  @Override
  public KeyValueStorageTransaction startTransaction() throws StorageException {
    // TODO code
    return null;
  }

  @Override
  public void close() throws IOException {
    // TODO code
  }
}
