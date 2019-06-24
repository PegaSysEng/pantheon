package tech.pegasys.pantheon.services.kvstore;

import io.vertx.ext.web.common.template.test;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rocksdb.ColumnFamilyHandle;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.RocksDbStorageProvider;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.services.kvstore.SegmentedKeyValueStorage.Transaction;
import tech.pegasys.pantheon.services.kvstore.SegmentedKeyValueStorage.Segment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static tech.pegasys.pantheon.ethereum.storage.keyvalue.RocksDbStorageProvider.*;

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
