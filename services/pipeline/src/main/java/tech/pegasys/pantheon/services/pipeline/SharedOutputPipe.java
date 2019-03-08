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

import java.util.concurrent.atomic.AtomicInteger;

public class SharedOutputPipe<T> implements OutputPipe<T> {
  private final OutputPipe<T> delegate;
  private final AtomicInteger remainingClosesRequired;

  public SharedOutputPipe(final OutputPipe<T> delegate, final int closesRequired) {
    this.delegate = delegate;
    this.remainingClosesRequired = new AtomicInteger(closesRequired);
  }

  @Override
  public boolean isOpen() {
    return delegate.isOpen();
  }

  @Override
  public void put(final T value) {
    delegate.put(value);
  }

  @Override
  public void close() {
    if (remainingClosesRequired.decrementAndGet() == 0) {
      delegate.close();
    }
  }

  @Override
  public void abort() {
    delegate.abort();
  }

  @Override
  public boolean hasRemainingCapacity() {
    return delegate.hasRemainingCapacity();
  }
}
