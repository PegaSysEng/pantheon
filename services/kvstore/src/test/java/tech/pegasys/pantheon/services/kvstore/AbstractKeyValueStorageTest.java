/*
 * Copyright 2018 ConsenSys AG.
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import tech.pegasys.pantheon.plugin.services.storage.KeyValueStorage;
import tech.pegasys.pantheon.plugin.services.storage.KeyValueStorageTransaction;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.bytes.BytesValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public abstract class AbstractKeyValueStorageTest {

  protected abstract KeyValueStorage createStore() throws Exception;

  @Test
  public void twoStoresAreIndependent() throws Exception {
    final KeyValueStorage store1 = createStore();
    final KeyValueStorage store2 = createStore();

    final KeyValueStorageTransaction tx = store1.startTransaction();
    final byte[] key = bytesFromHexString("0001");
    final byte[] value = bytesFromHexString("0FFF");

    tx.put(key, value);
    tx.commit();

    final Optional<byte[]> result = store2.get(key);
    assertEquals(Optional.empty(), result);
  }

  @Test
  public void put() throws Exception {
    final KeyValueStorage store = createStore();
    final byte[] key = bytesFromHexString("0F");
    final byte[] firstValue = bytesFromHexString("0ABC");
    final byte[] secondValue = bytesFromHexString("0DEF");

    KeyValueStorageTransaction tx = store.startTransaction();
    tx.put(key, firstValue);
    tx.commit();
    assertEquals(Optional.of(firstValue), store.get(key));

    tx = store.startTransaction();
    tx.put(key, secondValue);
    tx.commit();
    assertEquals(Optional.of(secondValue), store.get(key));
  }

  @Test
  public void containsKey() throws Exception {
    final KeyValueStorage store = createStore();
    final byte[] key = bytesFromHexString("ABCD");
    final byte[] value = bytesFromHexString("DEFF");

    assertFalse(store.get(key).isPresent());

    final KeyValueStorageTransaction transaction = store.startTransaction();
    transaction.put(key, value);
    transaction.commit();

    assertTrue(store.get(key).isPresent());
  }

  @Test
  public void removeExisting() throws Exception {
    final KeyValueStorage store = createStore();
    final byte[] key = bytesFromHexString("0F");
    final byte[] value = bytesFromHexString("0ABC");

    KeyValueStorageTransaction tx = store.startTransaction();
    tx.put(key, value);
    tx.commit();

    tx = store.startTransaction();
    tx.remove(key);
    tx.commit();

    assertEquals(Optional.empty(), store.get(key));
  }

  @Test
  public void removeExistingSameTransaction() throws Exception {
    final KeyValueStorage store = createStore();
    final byte[] key = bytesFromHexString("0F");
    final byte[] value = bytesFromHexString("0ABC");

    KeyValueStorageTransaction tx = store.startTransaction();
    tx.put(key, value);
    tx.remove(key);
    tx.commit();

    assertEquals(Optional.empty(), store.get(key));
  }

  @Test
  public void removeNonExistent() throws Exception {
    final KeyValueStorage store = createStore();
    final byte[] key = bytesFromHexString("0F");

    KeyValueStorageTransaction tx = store.startTransaction();
    tx.remove(key);
    tx.commit();

    assertEquals(Optional.empty(), store.get(key));
  }

  @Test
  public void concurrentUpdate() throws Exception {
    final int keyCount = 1000;
    final KeyValueStorage store = createStore();

    final CountDownLatch finishedLatch = new CountDownLatch(2);
    final Function<byte[], Thread> updater =
        (value) ->
            new Thread(
                () -> {
                  try {
                    for (int i = 0; i < keyCount; i++) {
                      KeyValueStorageTransaction tx = store.startTransaction();
                      tx.put(BytesValues.toMinimalBytes(i).getArrayUnsafe(), value);
                      tx.commit();
                    }
                  } finally {
                    finishedLatch.countDown();
                  }
                });

    // Run 2 concurrent transactions that write a bunch of values to the same keys
    final byte[] a = BytesValue.of(10).getArrayUnsafe();
    final byte[] b = BytesValue.of(20).getArrayUnsafe();
    updater.apply(a).start();
    updater.apply(b).start();

    finishedLatch.await();

    for (int i = 0; i < keyCount; i++) {
      final byte[] key = BytesValues.toMinimalBytes(i).getArrayUnsafe();
      final byte[] actual = store.get(key).get();
      assertTrue(Arrays.equals(actual, a) || Arrays.equals(actual, b));
    }

    store.close();
  }

  @Test
  public void transactionCommit() throws Exception {
    final KeyValueStorage store = createStore();
    // Add some values
    KeyValueStorageTransaction tx = store.startTransaction();
    tx.put(bytesOf(1), bytesOf(1));
    tx.put(bytesOf(2), bytesOf(2));
    tx.put(bytesOf(3), bytesOf(3));
    tx.commit();

    // Start transaction that adds, modifies, and removes some values
    tx = store.startTransaction();
    tx.put(bytesOf(2), bytesOf(3));
    tx.put(bytesOf(2), bytesOf(4));
    tx.remove(bytesOf(3));
    tx.put(bytesOf(4), bytesOf(8));

    // Check values before committing have not changed
    assertArrayEquals(bytesOf(1), store.get(bytesOf(1)).get());
    assertArrayEquals(bytesOf(2), store.get(bytesOf(2)).get());
    assertArrayEquals(bytesOf(3), store.get(bytesOf(3)).get());
    assertEquals(Optional.empty(), store.get(bytesOf(4)));

    tx.commit();

    // Check that values have been updated after commit
    assertArrayEquals(bytesOf(1), store.get(bytesOf(1)).get());
    assertArrayEquals(bytesOf(4), store.get(bytesOf(2)).get());
    assertEquals(Optional.empty(), store.get(bytesOf(3)));
    assertArrayEquals(bytesOf(8), store.get(bytesOf(4)).get());
  }

  @Test
  public void transactionRollback() throws Exception {
    final KeyValueStorage store = createStore();
    // Add some values
    KeyValueStorageTransaction tx = store.startTransaction();
    tx.put(bytesOf(1), bytesOf(1));
    tx.put(bytesOf(2), bytesOf(2));
    tx.put(bytesOf(3), bytesOf(3));
    tx.commit();

    // Start transaction that adds, modifies, and removes some values
    tx = store.startTransaction();
    tx.put(bytesOf(2), bytesOf(3));
    tx.put(bytesOf(2), bytesOf(4));
    tx.remove(bytesOf(3));
    tx.put(bytesOf(4), bytesOf(8));

    // Check values before committing have not changed
    assertArrayEquals(bytesOf(1), store.get(bytesOf(1)).get());
    assertArrayEquals(bytesOf(2), store.get(bytesOf(2)).get());
    assertArrayEquals(bytesOf(3), store.get(bytesOf(3)).get());
    assertEquals(Optional.empty(), store.get(bytesOf(4)));

    tx.rollback();

    // Check that values have not changed after rollback
    assertArrayEquals(bytesOf(1), store.get(bytesOf(1)).get());
    assertArrayEquals(bytesOf(2), store.get(bytesOf(2)).get());
    assertArrayEquals(bytesOf(3), store.get(bytesOf(3)).get());
    assertEquals(Optional.empty(), store.get(bytesOf(4)));
  }

  @Test
  public void transactionCommitEmpty() throws Exception {
    final KeyValueStorage store = createStore();
    final KeyValueStorageTransaction tx = store.startTransaction();
    tx.commit();
  }

  @Test
  public void transactionRollbackEmpty() throws Exception {
    final KeyValueStorage store = createStore();
    final KeyValueStorageTransaction tx = store.startTransaction();
    tx.rollback();
  }

  @Test(expected = IllegalStateException.class)
  public void transactionPutAfterCommit() throws Exception {
    final KeyValueStorage store = createStore();
    final KeyValueStorageTransaction tx = store.startTransaction();
    tx.commit();
    tx.put(bytesOf(1), bytesOf(1));
  }

  @Test(expected = IllegalStateException.class)
  public void transactionRemoveAfterCommit() throws Exception {
    final KeyValueStorage store = createStore();
    final KeyValueStorageTransaction tx = store.startTransaction();
    tx.commit();
    tx.remove(bytesOf(1));
  }

  @Test(expected = IllegalStateException.class)
  public void transactionPutAfterRollback() throws Exception {
    final KeyValueStorage store = createStore();
    final KeyValueStorageTransaction tx = store.startTransaction();
    tx.rollback();
    tx.put(bytesOf(1), bytesOf(1));
  }

  @Test(expected = IllegalStateException.class)
  public void transactionRemoveAfterRollback() throws Exception {
    final KeyValueStorage store = createStore();
    final KeyValueStorageTransaction tx = store.startTransaction();
    tx.rollback();
    tx.remove(bytesOf(1));
  }

  @Test(expected = IllegalStateException.class)
  public void transactionCommitAfterRollback() throws Exception {
    final KeyValueStorage store = createStore();
    final KeyValueStorageTransaction tx = store.startTransaction();
    tx.rollback();
    tx.commit();
  }

  @Test(expected = IllegalStateException.class)
  public void transactionCommitTwice() throws Exception {
    final KeyValueStorage store = createStore();
    final KeyValueStorageTransaction tx = store.startTransaction();
    tx.commit();
    tx.commit();
  }

  @Test(expected = IllegalStateException.class)
  public void transactionRollbackAfterCommit() throws Exception {
    final KeyValueStorage store = createStore();
    final KeyValueStorageTransaction tx = store.startTransaction();
    tx.commit();
    tx.rollback();
  }

  @Test(expected = IllegalStateException.class)
  public void transactionRollbackTwice() throws Exception {
    final KeyValueStorage store = createStore();
    final KeyValueStorageTransaction tx = store.startTransaction();
    tx.rollback();
    tx.rollback();
  }

  @Test
  public void twoTransactions() throws Exception {
    final KeyValueStorage store = createStore();

    final KeyValueStorageTransaction tx1 = store.startTransaction();
    final KeyValueStorageTransaction tx2 = store.startTransaction();

    tx1.put(bytesOf(1), bytesOf(1));
    tx2.put(bytesOf(2), bytesOf(2));

    tx1.commit();
    tx2.commit();

    assertArrayEquals(bytesOf(1), store.get(bytesOf(1)).get());
    assertArrayEquals(bytesOf(2), store.get(bytesOf(2)).get());
  }

  @Test
  public void transactionIsolation() throws Exception {
    final int keyCount = 1000;
    final KeyValueStorage store = createStore();

    final CountDownLatch finishedLatch = new CountDownLatch(2);
    final Function<byte[], Thread> txRunner =
        (value) ->
            new Thread(
                () -> {
                  final KeyValueStorageTransaction tx = store.startTransaction();
                  for (int i = 0; i < keyCount; i++) {
                    tx.put(BytesValues.toMinimalBytes(i).getArrayUnsafe(), value);
                  }
                  try {
                    tx.commit();
                  } finally {
                    finishedLatch.countDown();
                  }
                });

    // Run 2 concurrent transactions that write a bunch of values to the same keys
    final byte[] a = bytesOf(10);
    final byte[] b = bytesOf(20);
    txRunner.apply(a).start();
    txRunner.apply(b).start();

    finishedLatch.await();

    // Check that transaction results are isolated (not interleaved)
    final List<byte[]> finalValues = new ArrayList<>(keyCount);
    for (int i = 0; i < keyCount; i++) {
      final byte[] key = BytesValues.toMinimalBytes(i).getArrayUnsafe();
      finalValues.add(store.get(key).get());
    }

    // Expecting the same value for all entries
    final byte[] expected = finalValues.get(0);
    for (final byte[] actual : finalValues) {
      assertEquals(expected, actual);
    }

    assertTrue(expected.equals(a) || expected.equals(b));

    store.close();
  }

  /*
   * Used to mimic the wrapping with BytesValue performed in Pantheon
   */
  private byte[] bytesFromHexString(final String hex) {
    return BytesValue.fromHexString("0001").getArrayUnsafe();
  }

  private byte[] bytesOf(final int... bytes) {
    return BytesValue.of(bytes).getArrayUnsafe();
  }
}
