package tech.pegasys.pantheon.services.kvstore;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.services.kvstore.KeyValueStorage.Transaction;
import tech.pegasys.pantheon.services.kvstore.SegmentedKeyValueStorage.Segment;

import java.util.ArrayList;
import java.util.Optional;

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
        return ColumnarRocksDbKeyValueStorage.create(new RocksDbConfiguration.Builder().databaseDir(folder.newFolder().toPath()).build(), new ArrayList<Segment>(){}, new NoOpMetricsSystem());
    }
}
