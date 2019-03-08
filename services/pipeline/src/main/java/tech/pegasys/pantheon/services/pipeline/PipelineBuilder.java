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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Supports building a new pipeline. Pipelines are comprised of a source, various processing stages
 * and a consumer, each of which run in their own thread.
 *
 * <p>The pipeline completes when all items from the source have passed through each stage and are
 * received by the consumer. The pipeline will halt immediately if an exception is thrown from any
 * processing stage.
 *
 * @param <T> the output type of the last stage in the pipeline.
 */
public class PipelineBuilder<T> {

  private final PipelineSource<?> pipelineSource;
  private final Collection<Runnable> stages;
  private final Collection<Pipe<?>> pipes;
  private final InputPipe<T> pipeEnd;
  private final int bufferSize;

  public PipelineBuilder(
      final PipelineSource<?> pipelineSource,
      final Collection<Runnable> stages,
      final Collection<Pipe<?>> pipes,
      final InputPipe<T> pipeEnd,
      final int bufferSize) {
    checkArgument(!pipes.isEmpty(), "Must have at least one pipe in a pipeline");
    this.pipelineSource = pipelineSource;
    this.stages = stages;
    this.pipes = pipes;
    this.pipeEnd = pipeEnd;
    this.bufferSize = bufferSize;
  }

  /**
   * Create a new pipeline that processes inputs from <i>source</i>. The pipeline completes when
   * <i>source</i> returns <code>false</code> from {@link Iterator#hasNext()} and the last item has
   * been reached the end of the pipeline.
   *
   * @param source the source to pull items from for processing.
   * @param bufferSize the number of items to be buffered between each stage in the pipeline.
   * @param <T> the type of items input into the pipeline.
   * @return a {@link PipelineBuilder} ready to extend the pipeline with additional stages.
   */
  public static <T> PipelineBuilder<T> createPipelineFrom(
      final Iterator<T> source, final int bufferSize) {
    final Pipe<T> pipe = new Pipe<>(bufferSize);
    final StreamSourceStage<T> sourceStage = new StreamSourceStage<>(source, pipe);
    return new PipelineBuilder<>(
        sourceStage, singleton(sourceStage), singleton(pipe), pipe, bufferSize);
  }

  /**
   * Create a new pipeline that processes inputs added to <i>pipe</i>. The pipeline completes when
   * <i>pipe</i> is closed and the last item has been reached the end of the pipeline.
   *
   * @param pipe the pipe feeding the pipeline.
   * @param <T> the type of items input into the pipeline.
   * @return a {@link PipelineBuilder} ready to extend the pipeline with additional stages.
   */
  public static <T> PipelineBuilder<T> createPipelineFrom(final Pipe<T> pipe) {
    return new PipelineBuilder<>(
        new ExternalPipelineSource<>(pipe), emptyList(), singleton(pipe), pipe, pipe.getCapacity());
  }

  /**
   * Adds a 1-to-1 processing stage to the pipeline. A single thread processes each item in the
   * pipeline with <i>processor</i> outputting its return value to the next stage.
   *
   * @param processor the processing to apply to each item.
   * @param <O> the output type for this processing step.
   * @return a {@link PipelineBuilder} ready to extend the pipeline with additional stages.
   */
  public <O> PipelineBuilder<O> thenProcess(final Function<T, O> processor) {
    final SingleStepStage<T, O> singleStepStage = new ProcessStage<>(processor);
    return addStage(singleStepStage);
  }

  /**
   * Adds a 1-to-1 processing stage to the pipeline. Multiple threads processes items in the
   * pipeline concurrently with <i>processor</i> outputting its return value to the next stage.
   *
   * <p>Note: The order of items is not preserved.
   *
   * @param processor the processing to apply to each item.
   * @param <O> the output type for this processing step.
   * @return a {@link PipelineBuilder} ready to extend the pipeline with additional stages.
   */
  public <O> PipelineBuilder<O> thenProcessInParallel(
      final Function<T, O> processor, final int numberOfThreads) {
    final Pipe<O> newPipeEnd = new Pipe<>(bufferSize);
    final OutputPipe<O> outputPipe = new SharedOutputPipe<>(newPipeEnd, numberOfThreads);
    final ArrayList<Runnable> newStages = new ArrayList<>(stages);
    for (int i = 0; i < numberOfThreads; i++) {
      final Runnable processStage =
          new InputOutputStage<>(pipeEnd, outputPipe, new ProcessStage<>(processor));
      newStages.add(processStage);
    }
    return new PipelineBuilder<>(
        pipelineSource, newStages, concat(pipes, newPipeEnd), newPipeEnd, bufferSize);
  }

  /**
   * Adds a 1-to-1, asynchronous processing stage to the pipeline. A single thread reads items from
   * the input and calls <i>processor</i> to begin processing. While a single thread is used to
   * begin processing, up to <i>maxConcurrency</i> items may be in progress concurrently. When the
   * returned {@link CompletableFuture} completes successfully the result is passed to the next
   * stage.
   *
   * <p>If the returned {@link CompletableFuture} completes exceptionally the pipeline will abort.
   *
   * <p>Note: The order of items is not preserved.
   *
   * @param processor the processing to apply to each item.
   * @param <O> the output type for this processing step.
   * @return a {@link PipelineBuilder} ready to extend the pipeline with additional stages.
   */
  public <O> PipelineBuilder<O> thenProcessAsync(
      final Function<T, CompletableFuture<O>> processor, final int maxConcurrency) {
    return addStage(new AsyncProcessStage<>(processor, maxConcurrency));
  }

  /**
   * Batches items into groups of at most <i>maximumBatchSize</i>. Batches are created eagerly to
   * minimize delay so may not be full.
   *
   * <p>Order of items is preserved.
   *
   * <p>The output buffer size is reduced to <code>bufferSize / maximumBatchSize + 1</code>.
   *
   * @param maximumBatchSize the maximum number of items to include in a batch.
   * @return a {@link PipelineBuilder} ready to extend the pipeline with additional stages.
   */
  public PipelineBuilder<List<T>> inBatches(final int maximumBatchSize) {
    return addStage(new BatchingStage<>(maximumBatchSize), bufferSize / maximumBatchSize + 1);
  }

  /**
   * Adds a 1-to-many processing stage to the pipeline. For each item in the stream, <i>mapper</i>
   * is called and each item of the {@link Stream} it returns is output as an individual item. The
   * returned Stream may be empty to remove an item.
   *
   * <p>This can be used to reverse the effect of {@link #inBatches(int)} with:
   *
   * <pre>thenFlatMap(List::stream, newBufferSize)</pre>
   *
   * @param mapper the function to process each item with.
   * @param newBufferSize the output buffer size to use from this stage onwards.
   * @param <O> the type of items to be output from this stage.
   * @return a {@link PipelineBuilder} ready to extend the pipeline with additional stages.
   */
  public <O> PipelineBuilder<O> thenFlatMap(
      final Function<T, Stream<O>> mapper, final int newBufferSize) {
    return addStage(new FlatMapStage<>(mapper), newBufferSize);
  }

  /**
   * End the pipeline with a {@link Consumer} that is the last stage of the pipeline.
   *
   * @param completer the {@link Consumer} that accepts the final output of the pipeline.
   * @return the constructed pipeline ready to execute.
   */
  public Pipeline andFinishWith(final Consumer<T> completer) {
    return new Pipeline(pipelineSource, stages, pipes, new CompleterStage<>(pipeEnd, completer));
  }

  private <O> PipelineBuilder<O> addStage(final SingleStepStage<T, O> singleStepStage) {
    return addStage(singleStepStage, bufferSize);
  }

  private <O> PipelineBuilder<O> addStage(
      final SingleStepStage<T, O> singleStepStage, final int newBufferSize) {
    final Pipe<O> outputPipe = new Pipe<>(newBufferSize);
    final Runnable processStage = new InputOutputStage<>(pipeEnd, outputPipe, singleStepStage);
    return addStage(processStage, outputPipe);
  }

  private <O> PipelineBuilder<O> addStage(final Runnable stage, final Pipe<O> outputPipe) {
    final List<Runnable> newStages = concat(stages, stage);
    return new PipelineBuilder<>(
        pipelineSource, newStages, concat(pipes, outputPipe), outputPipe, bufferSize);
  }

  private <X> List<X> concat(final Collection<X> existing, final X newItem) {
    final List<X> newList = new ArrayList<>(existing);
    newList.add(newItem);
    return newList;
  }
}
