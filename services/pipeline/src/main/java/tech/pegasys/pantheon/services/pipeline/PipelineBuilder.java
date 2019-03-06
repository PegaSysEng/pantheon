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

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PipelineBuilder<T> {

  private final Collection<Runnable> stages;
  private final InputPipe<T> pipeEnd;
  private final int bufferSize;

  public PipelineBuilder(
      final Collection<Runnable> stages, final InputPipe<T> pipeEnd, final int bufferSize) {
    this.stages = stages;
    this.pipeEnd = pipeEnd;
    this.bufferSize = bufferSize;
  }

  public static <T> PipelineBuilder<T> createPipelineFrom(
      final Iterator<T> taskStream, final int bufferSize) {
    final Pipe<T> pipe = new Pipe<>(bufferSize);
    final SourceStage<T> sourceStage = new SourceStage<>(taskStream, pipe);
    return new PipelineBuilder<>(singleton(sourceStage), pipe, bufferSize);
  }

  public static <T> PipelineBuilder<T> createPipelineFrom(
      final InputPipe<T> pipe, final int bufferSize) {
    return new PipelineBuilder<>(emptyList(), pipe, bufferSize);
  }

  public <O> PipelineBuilder<O> thenProcessWith(final Function<T, O> processor) {
    final Pipe<O> outputPipe = new Pipe<>(bufferSize);
    final ProcessStage<T, O> processStage = new ProcessStage<>(pipeEnd, outputPipe, processor);
    return addStage(processStage, outputPipe);
  }

  public PipelineBuilder<List<T>> inBatches(final int maximumBatchSize) {
    final Pipe<List<T>> outputPipe = new Pipe<>(bufferSize / maximumBatchSize + 1);
    final BatchingStage<T> batchingStage =
        new BatchingStage<>(pipeEnd, outputPipe, maximumBatchSize);
    return addStage(batchingStage, outputPipe);
  }

  public Pipeline andFinishWith(final Consumer<T> completer) {
    return new Pipeline(stages, new CompleterStage<>(pipeEnd, completer));
  }

  private <O> PipelineBuilder<O> addStage(final Runnable stage, final Pipe<O> outputPipe) {
    final ArrayList<Runnable> newStages = new ArrayList<>(stages);
    newStages.add(stage);
    return new PipelineBuilder<>(newStages, outputPipe, bufferSize);
  }
}
