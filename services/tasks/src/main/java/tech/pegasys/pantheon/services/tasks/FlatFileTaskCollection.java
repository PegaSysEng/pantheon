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
package tech.pegasys.pantheon.services.tasks;

import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class FlatFileTaskCollection<T> implements TaskCollection<T> {

  private final Set<FlatFileTask<T>> outstandingTasks = new HashSet<>();

  private final Function<T, BytesValue> serializer;
  private final Function<BytesValue, T> deserializer;

  private final ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.SIZE);

  private final FileChannel fileChannel;

  private long size = 0;

  public FlatFileTaskCollection(
      final Path storageDirectory,
      final Function<T, BytesValue> serializer,
      final Function<BytesValue, T> deserializer) {
    this.serializer = serializer;
    this.deserializer = deserializer;
    try {
      fileChannel =
          FileChannel.open(
              storageDirectory.resolve("tasks"),
              StandardOpenOption.TRUNCATE_EXISTING,
              StandardOpenOption.READ,
              StandardOpenOption.WRITE,
              StandardOpenOption.CREATE);
    } catch (final IOException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public synchronized void add(final T taskData) {
    final BytesValue data = serializer.apply(taskData);
    try {
      final long offset = fileChannel.size();
      lengthBuffer.position(0);
      lengthBuffer.putInt(data.size());
      lengthBuffer.flip();
      fileChannel.write(lengthBuffer, offset);
      fileChannel.write(ByteBuffer.wrap(data.getArrayUnsafe()), offset + Integer.SIZE);
      size++;
    } catch (final IOException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public synchronized Task<T> remove() {
    if (isEmpty()) {
      return null;
    }
    try {
      lengthBuffer.position(0);
      lengthBuffer.limit(Integer.SIZE);
      readAndCheckLength(lengthBuffer, Integer.SIZE);
      final int dataLength = lengthBuffer.getInt(0);

      final ByteBuffer dataBuffer = ByteBuffer.allocate(dataLength);
      readAndCheckLength(dataBuffer, dataLength);
      final T data = deserializer.apply(BytesValue.wrapBuffer(dataBuffer));
      final FlatFileTask<T> task = new FlatFileTask<>(this, data);
      outstandingTasks.add(task);
      size--;
      return task;
    } catch (final IOException e) {
      throw new StorageException(e);
    }
  }

  private void readAndCheckLength(final ByteBuffer buffer, final int expectedLength)
      throws IOException {
    final int readBytes = fileChannel.read(buffer);
    if (readBytes != expectedLength) {
      throw new IllegalStateException(
          "Task queue corrupted. Expected to read "
              + expectedLength
              + " bytes but only got "
              + readBytes
              + " starting at position "
              + fileChannel.position()
              + " with size "
              + fileChannel.size());
    }
  }

  @Override
  public synchronized long size() {
    return size;
  }

  @Override
  public synchronized boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public synchronized void clear() {
    outstandingTasks.clear();
    try {
      fileChannel.position(0);
      fileChannel.truncate(0);
      size = 0;
    } catch (final IOException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public synchronized boolean allTasksCompleted() {
    return isEmpty() && outstandingTasks.isEmpty();
  }

  @Override
  public synchronized void close() {
    try {
      fileChannel.close();
    } catch (final IOException e) {
      throw new StorageException(e);
    }
  }

  private synchronized boolean markTaskCompleted(final FlatFileTask<T> task) {
    return outstandingTasks.remove(task);
  }

  private synchronized void handleFailedTask(final FlatFileTask<T> task) {
    if (markTaskCompleted(task)) {
      add(task.getData());
    }
  }

  public static class StorageException extends RuntimeException {
    StorageException(final Throwable t) {
      super(t);
    }
  }

  private static class FlatFileTask<T> implements Task<T> {
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final FlatFileTaskCollection<T> parentQueue;
    private final T data;

    private FlatFileTask(final FlatFileTaskCollection<T> parentQueue, final T data) {
      this.parentQueue = parentQueue;
      this.data = data;
    }

    @Override
    public T getData() {
      return data;
    }

    @Override
    public void markCompleted() {
      if (completed.compareAndSet(false, true)) {
        parentQueue.markTaskCompleted(this);
      }
    }

    @Override
    public void markFailed() {
      if (completed.compareAndSet(false, true)) {
        parentQueue.handleFailedTask(this);
      }
    }
  }
}
