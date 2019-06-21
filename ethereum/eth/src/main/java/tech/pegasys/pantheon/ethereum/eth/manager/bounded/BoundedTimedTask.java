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
package tech.pegasys.pantheon.ethereum.eth.manager.bounded;

import static java.time.Duration.between;
import static java.time.Instant.now;

import java.time.Duration;
import java.time.Instant;

public class BoundedTimedTask implements TimedTask {

  private final String name;
  private final Runnable task;
  private final Instant startedAt;

  private BoundedTimedTask(final String name, final Runnable task) {
    this.name = name;
    this.task = task;
    this.startedAt = now();
  }

  public static TimedTask fromRunnable(final String name, final Runnable task) {
    return new BoundedTimedTask(name, task);
  }

  public static TimedTask fromRunnable(final Runnable task) {
    return fromRunnable("", task);
  }

  @Override
  public Instant startedAt() {
    return startedAt;
  }

  @Override
  public Duration age() {
    return between(startedAt(), now());
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void run() {
    task.run();
  }
}
