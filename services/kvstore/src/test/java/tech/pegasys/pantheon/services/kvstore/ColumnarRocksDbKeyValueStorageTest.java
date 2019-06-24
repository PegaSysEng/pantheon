package tech.pegasys.pantheon.services.kvstore;

import io.vertx.ext.web.common.template.test;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.RocksDbStorageProvider;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.services.kvstore.KeyValueStorage.Transaction;
import tech.pegasys.pantheon.services.kvstore.SegmentedKeyValueStorage.Segment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

import static tech.pegasys.pantheon.ethereum.storage.keyvalue.RocksDbStorageProvider.*;

public class ColumnarRocksDbKeyValueStorageTest extends AbstractKeyValueStorageTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void iterateThroughSegments() throws Exception {
//        final SegmentedKeyValueStorage store = createStore();
//
//        Transaction tx = store.startTransaction();
//        tx.put(BytesValue.fromHexString("0001"), BytesValue.fromHexString("0FFF"));
//        tx.commit();
//        final Optional<BytesValue> result = store2.get(BytesValue.fromHexString("0001"));
//        assertEquals(Optional.empty(), result);
    }

    @Override
    protected KeyValueStorage createStore() throws Exception {
        return new SegmentedKeyValueStorageAdapter<>(RocksDbSegment.WORLD_STATE, ColumnarRocksDbKeyValueStorage.create(
                new RocksDbConfiguration.Builder().databaseDir(folder.newFolder().toPath()).build(),
                Collections.singletonList(RocksDbSegment.WORLD_STATE),
                new NoOpMetricsSystem())
        );
    }
}
