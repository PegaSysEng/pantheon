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

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Pipeline {
  private static final Logger LOG = LogManager.getLogger();
  private final PipelineSource<?> pipelineSource;
  private final Collection<Runnable> stages;
  private final Collection<Pipe<?>> pipes;
  private final CompleterStage<?> completerStage;
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final CompletableFuture<?> overallFuture = new CompletableFuture<>();
  private volatile List<Future<?>> futures;

  public Pipeline(
      final PipelineSource<?> pipelineSource,
      final Collection<Runnable> stages,
      final Collection<Pipe<?>> pipes,
      final CompleterStage<?> completerStage) {
    this.pipelineSource = pipelineSource;
    this.stages = stages;
    this.pipes = pipes;
    this.completerStage = completerStage;
  }

  public CompletableFuture<?> start(final ExecutorService executorService) {
    if (!started.compareAndSet(false, true)) {
      return overallFuture;
    }
    futures =
        Stream.concat(stages.stream(), Stream.of(completerStage))
            .map(task -> runWithErrorHandling(executorService, task))
            .collect(toList());
    completerStage
        .getFuture()
        .whenComplete(
            (result, error) -> {
              synchronized (this) {
                if (error != null) {
                  overallFuture.completeExceptionally(error);
                } else {
                  overallFuture.complete(null);
                }
              }
            });
    return overallFuture;
  }

  public void abort() {
    final CancellationException exception = new CancellationException("Pipeline aborted");
    abort(exception);
  }

  private Future<?> runWithErrorHandling(
      final ExecutorService executorService, final Runnable task) {
    return executorService.submit(
        () -> {
          try {
            task.run();
          } catch (final Throwable t) {
            LOG.debug("Unhandled exception in pipeline. Aborting.", t);
            abort(t);
          }
        });
  }

  private void abort(final Throwable error) {
    synchronized (this) {
      pipelineSource.abort();
      pipes.forEach(Pipe::abort);
      futures.forEach(future -> future.cancel(true));
      overallFuture.completeExceptionally(error);
    }
  }
}
