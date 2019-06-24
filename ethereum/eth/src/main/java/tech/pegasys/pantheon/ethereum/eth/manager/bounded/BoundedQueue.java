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

import tech.pegasys.pantheon.metrics.Counter;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.metrics.PantheonMetricCategory;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class BoundedQueue extends LinkedBlockingDeque<Runnable> {
  public static final long DEFAULT_EVICTION_POLL_TIMEOUT = 10;
  public static final TimeUnit DEFAULT_EVICTION_POLL_TIMEOUT_UNIT = TimeUnit.SECONDS;

  private final long evictionPollTimeout;
  private final TimeUnit evictionPollTimeoutUnit;
  private final MetricsSystem metricsSystem;
  private final Counter totalEvictedTaskCounter;

  public BoundedQueue(
      final int capacity,
      final long evictionPollTimeout,
      final TimeUnit evictionPollTimeoutUnit,
      final String metricName,
      final MetricsSystem metricsSystem) {
    super(capacity);
    this.evictionPollTimeout = evictionPollTimeout;
    this.evictionPollTimeoutUnit = evictionPollTimeoutUnit;
    this.metricsSystem = metricsSystem;
    this.totalEvictedTaskCounter =
        this.metricsSystem.createCounter(
            PantheonMetricCategory.EXECUTORS,
            metricName + "_dropped_tasks_total",
            "Total number of tasks rejected by this working queue.");
  }

  @Override
  public boolean offer(final Runnable task) {
    try {
      if (!super.offer(task)) {
        poll(evictionPollTimeout, evictionPollTimeoutUnit);
        totalEvictedTaskCounter.inc();
        return super.offer(task);
      }
      return true;
    } catch (InterruptedException e) {
      return false;
    }
  }
}
