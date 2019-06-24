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

import static org.junit.Assert.assertEquals;
import static tech.pegasys.pantheon.ethereum.storage.keyvalue.RocksDbStorageProvider.*;

import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.services.kvstore.SegmentedKeyValueStorage.Transaction;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ColumnarRocksDbKeyValueStorageTest extends AbstractKeyValueStorageTest {
  @Rule public final TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void twoSegmentsAreIndependent() throws Exception {
    final SegmentedKeyValueStorage store = createSegmentedStore();

    Transaction tx = store.startTransaction();
    tx.put(
        store.getSegmentIdentifierByName(RocksDbSegment.BLOCKCHAIN),
        BytesValue.fromHexString("0001"),
        BytesValue.fromHexString("0FFF"));
    tx.commit();
    final Optional<BytesValue> result =
        store.get(
            store.getSegmentIdentifierByName(RocksDbSegment.WORLD_STATE),
            BytesValue.fromHexString("0001"));
    assertEquals(Optional.empty(), result);
  }

  private SegmentedKeyValueStorage createSegmentedStore() throws Exception {
    return ColumnarRocksDbKeyValueStorage.create(
        new RocksDbConfiguration.Builder().databaseDir(folder.newFolder().toPath()).build(),
        Arrays.asList(RocksDbSegment.WORLD_STATE, RocksDbSegment.BLOCKCHAIN),
        new NoOpMetricsSystem());
  }

  @Override
  protected KeyValueStorage createStore() throws Exception {
    return new SegmentedKeyValueStorageAdapter<>(
        RocksDbSegment.WORLD_STATE, createSegmentedStore());
  }
}
