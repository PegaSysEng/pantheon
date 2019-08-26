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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.metrics.Counter;
import tech.pegasys.pantheon.metrics.OperationTimer;
import tech.pegasys.pantheon.plugin.services.storage.KeyValueStorage;
import tech.pegasys.pantheon.plugin.services.storage.KeyValueStorageTransaction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class KeyValueStorageMetricsDecoratorTest {

  @Mock private KeyValueStorageMetrics metrics;
  @Mock private KeyValueStorage storage;
  @Mock private KeyValueStorageTransaction transaction;
  @Mock private OperationTimer readLatency;
  @Mock private OperationTimer removeLatency;
  @Mock private OperationTimer writeLatency;
  @Mock private OperationTimer commitLatency;
  @Mock private Counter rollbackCount;

  private KeyValueStorageMetricsDecorator decorator;

  @Before
  public void setUp() {
    when(storage.startTransaction()).thenReturn(transaction);
    when(metrics.getReadLatency()).thenReturn(readLatency);
    when(metrics.getRemoveLatency()).thenReturn(removeLatency);
    when(metrics.getWriteLatency()).thenReturn(writeLatency);
    when(metrics.getCommitLatency()).thenReturn(commitLatency);
    when(metrics.getRollbackCount()).thenReturn(rollbackCount);
    decorator = new KeyValueStorageMetricsDecorator(storage, metrics);
  }

  @Test
  public void clearMustPropagateWithoutUpdatingMetrics() {

    decorator.clear();

    verifyZeroInteractions(metrics);
    verify(storage).clear();
    verifyNoMoreInteractions(storage);
  }

  @Test
  public void closeMustPropagateWithoutUpdatingMetrics() throws IOException {

    decorator.close();

    verify(storage).close();
    verifyNoMoreInteractions(storage);
    verifyZeroInteractions(metrics);
  }

  @Test
  public void containsKeyMustPropagateWithoutUpdatingMetrics() {
    final byte[] key = "testing containsKey".getBytes(StandardCharsets.UTF_8);

    decorator.containsKey(key);

    verify(storage).containsKey(key);
    verifyNoMoreInteractions(storage);
    verifyZeroInteractions(metrics);
  }

  @Test
  public void getMustPropagateAndUpdateMetrics() {
    final byte[] key = "testing get".getBytes(StandardCharsets.UTF_8);
    final Optional<byte[]> expectedValue =
        Optional.of("testing get value".getBytes(StandardCharsets.UTF_8));
    when(storage.get(any())).thenReturn(expectedValue);

    final Optional<byte[]> value = decorator.get(key);

    assertThat(value).isEqualTo(expectedValue);
    verify(storage).get(key);
    verifyNoMoreInteractions(storage);
    verify(metrics).getReadLatency();
    verifyNoMoreInteractions(storage);
    verify(readLatency).startTimer();
    verifyNoMoreInteractions(readLatency);
  }

  @Test
  public void removeAllKeysUnlessMustPropagateWithoutUpdatingMetrics() {
    final long expectedValue = 99;
    when(storage.removeAllKeysUnless(any())).thenReturn(expectedValue);
    final Predicate<byte[]> retainCondition = a -> true;

    final long value = decorator.removeAllKeysUnless(retainCondition);

    assertThat(value).isEqualTo(expectedValue);
    verify(storage).removeAllKeysUnless(retainCondition);
    verifyNoMoreInteractions(storage);
    verifyZeroInteractions(metrics);
  }

  @Test
  public void transactionPutMustPropagateAndUpdateMetrics() {
    final byte[] key = "testing put key".getBytes(StandardCharsets.UTF_8);
    final byte[] value = "testing put value".getBytes(StandardCharsets.UTF_8);

    decorator.startTransaction().put(key, value);

    verify(storage).startTransaction();
    verifyNoMoreInteractions(storage);
    verify(transaction).put(key, value);
    verifyNoMoreInteractions(transaction);
    verify(writeLatency).startTimer();
    verifyNoMoreInteractions(writeLatency);
    verify(metrics).getWriteLatency();
    verifyNoMoreInteractions(metrics);
  }

  @Test
  public void transactionRemoveMustPropagateAndUpdateMetrics() {
    final byte[] key = "testing remove key".getBytes(StandardCharsets.UTF_8);

    decorator.startTransaction().remove(key);

    verify(storage).startTransaction();
    verifyNoMoreInteractions(storage);
    verify(transaction).remove(key);
    verifyNoMoreInteractions(transaction);
    verify(removeLatency).startTimer();
    verifyNoMoreInteractions(removeLatency);
    verify(metrics).getRemoveLatency();
    verifyNoMoreInteractions(metrics);
  }

  @Test
  public void transactionCommitMustPropagateAndUpdateMetrics() {

    decorator.startTransaction().commit();

    verify(storage).startTransaction();
    verifyNoMoreInteractions(storage);
    verify(transaction).commit();
    verifyNoMoreInteractions(transaction);
    verify(commitLatency).startTimer();
    verifyNoMoreInteractions(commitLatency);
    verify(metrics).getCommitLatency();
    verifyNoMoreInteractions(metrics);
  }

  @Test
  public void transactionRollbackMustPropagateAndUpdateMetrics() {

    decorator.startTransaction().rollback();

    verify(storage).startTransaction();
    verifyNoMoreInteractions(storage);
    verify(transaction).rollback();
    verifyNoMoreInteractions(transaction);
    verify(rollbackCount).inc();
    verifyNoMoreInteractions(rollbackCount);
    verify(metrics).getRollbackCount();
    verifyNoMoreInteractions(metrics);
  }
}
