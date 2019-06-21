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

import static java.util.Objects.requireNonNull;

import tech.pegasys.pantheon.metrics.Counter;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.metrics.PantheonMetricCategory;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BoundedTimedQueue extends LinkedBlockingDeque<Runnable> {
  private static final Logger LOG = LogManager.getLogger();

  private final long keepAlive;
  private final TemporalUnit keepAliveUnit;
  private final long evictionPollTimeout;
  private final TimeUnit evictionPollTimeoutUnit;
  private final MetricsSystem metricsSystem;
  private final String metricName;
  private final Counter totalEvictedTaskCounter;

  public BoundedTimedQueue() {
    this(
        Default.CAPACITY,
        Default.KEEP_ALIVE,
        Default.KEEP_ALIVE_UNIT,
        Default.EVICTION_POLL_TIMEOUT,
        Default.EVICTION_POLL_TIMEOUT_UNIT,
        Default.METRICS_SYSTEM);
  }

  public BoundedTimedQueue(
      final int capacity,
      final long keepAlive,
      final TemporalUnit keepAliveUnit,
      final long evictionPollTimeout,
      final TimeUnit evictionPollTimeoutUnit,
      final MetricsSystem metricsSystem) {
    super(capacity);
    this.keepAlive = keepAlive;
    this.keepAliveUnit = keepAliveUnit;
    this.evictionPollTimeout = evictionPollTimeout;
    this.evictionPollTimeoutUnit = evictionPollTimeoutUnit;
    this.metricsSystem = metricsSystem;
    this.metricName = this.getClass().getSimpleName().toLowerCase(Locale.US).replace('-', '_');
    this.totalEvictedTaskCounter =
        this.metricsSystem.createCounter(
            PantheonMetricCategory.EXECUTORS,
            metricName + "_rejected_tasks_total",
            "Total number of tasks rejected by this working queue.");
  }

  @Override
  public boolean offer(final Runnable runnable) {
    try {
      requireNonNull(runnable);
      if (!(runnable instanceof TimedTask)) {
        return false;
      }
      final TimedTask task = (TimedTask) runnable;
      if (!isEligible(task)) {
        LOG.debug("Task does not match time policy, rejecting task.");
        return false;
      }
      final boolean offered = super.offer(task);
      if (offered) {
        return true;
      }
      // Try to evict tasks based on time policy.
      int evictedTaskCounter = processEviction();
      if (evictedTaskCounter == 0) {
        // No task has been evicted, rejecting new task.
        LOG.warn(
            "Task has been rejected because of maximum capacity of the queue has been reached and no candidate task to remove from the working queue.");
        return false;
      }
      LOG.warn("{} task(s) evicted from the working queue because too old.", evictedTaskCounter);
      totalEvictedTaskCounter.inc(evictedTaskCounter);
      // Check if task is still eligible after eviction process.
      if (isEligible(task) && super.offer(task)) {
        return true;
      } else {
        LOG.warn("Task insertion failed after eviction process, rejecting the task.");
        return false;
      }
    } catch (InterruptedException e) {
      LOG.warn("Eviction aborted.", e);
      return false;
    }
  }

  /**
   * Process eviction of tasks too old given keepAlive.
   *
   * @return The number of tasks evicted.
   * @throws InterruptedException
   */
  private int processEviction() throws InterruptedException {
    int evictedTaskCounter = 0;
    boolean continueEviction = true;
    TimedTask lastPolledTask = null;
    // Removing tasks that are too old.
    while (continueEviction) {
      lastPolledTask = (TimedTask) poll(evictionPollTimeout, evictionPollTimeoutUnit);
      if (lastPolledTask == null) {
        continueEviction = false;
      } else {
        continueEviction = isTooOld(lastPolledTask);
        if (continueEviction) {
          evictedTaskCounter++;
        }
      }
    }
    // Re inject last polled task if not too old.
    if (lastPolledTask != null && !isTooOld(lastPolledTask)) {
      super.addFirst(lastPolledTask);
    }
    return evictedTaskCounter;
  }

  /**
   * Check if the given task is eligible for insertion in the queue.
   *
   * @param task The task to check.
   * @return If the task is eligible.
   */
  private boolean isEligible(final TimedTask task) {
    return !isTooOld(task);
  }

  private boolean isTooOld(final TimedTask task) {
    return task.age().get(keepAliveUnit) >= keepAlive;
  }

  public static final class Default {
    public static final int CAPACITY = 1000000;
    public static final long KEEP_ALIVE = 60;
    public static final TemporalUnit KEEP_ALIVE_UNIT = ChronoUnit.SECONDS;
    public static final long EVICTION_POLL_TIMEOUT = 10;
    public static final TimeUnit EVICTION_POLL_TIMEOUT_UNIT = TimeUnit.SECONDS;
    public static final MetricsSystem METRICS_SYSTEM = new NoOpMetricsSystem();
  }
}
