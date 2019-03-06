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
package tech.pegasys.pantheon.services.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class Pipe<T> implements InputPipe<T>, OutputPipe<T> {
  private static final Logger LOG = LogManager.getLogger();
  private final BlockingQueue<T> queue;
  private final AtomicBoolean closed = new AtomicBoolean();

  public Pipe(final int capacity) {
    queue = new ArrayBlockingQueue<>(capacity);
  }

  @Override
  public boolean isOpen() {
    return !closed.get();
  }

  @Override
  public void close() {
    closed.set(true);
  }

  @Override
  public boolean hasMore() {
    return !closed.get() || !queue.isEmpty();
  }

  @Override
  public T get() {
    while (isOpen() || !queue.isEmpty()) {
      try {
        final T value = queue.poll(1, TimeUnit.SECONDS);
        if (value != null) {
          return value;
        }
      } catch (final InterruptedException e) {
        LOG.trace("Interrupted while waiting for next item", e);
      }
    }
    return null;
  }

  @Override
  public List<T> getBatch(final int maximumBatchSize) {
    final T nextItem = get();
    if (nextItem == null) {
      return Collections.emptyList();
    }
    final List<T> batch = new ArrayList<>();
    batch.add(nextItem);
    queue.drainTo(batch, maximumBatchSize - 1);
    return batch;
  }

  @Override
  public void put(final T value) {
    while (isOpen()) {
      try {
        if (queue.offer(value, 1, TimeUnit.SECONDS)) {
          return;
        }
      } catch (final InterruptedException e) {
        LOG.trace("Interrupted while waiting to add to output", e);
      }
    }
  }
}
