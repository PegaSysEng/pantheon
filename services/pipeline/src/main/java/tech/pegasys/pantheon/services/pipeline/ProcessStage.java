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

import java.util.function.Function;

class ProcessStage<I, O> implements Runnable {

  private final InputPipe<I> inputPipe;
  private final OutputPipe<O> outputPipe;
  private final Function<I, O> processor;

  public ProcessStage(
      final InputPipe<I> inputPipe,
      final OutputPipe<O> outputPipe,
      final Function<I, O> processor) {
    this.inputPipe = inputPipe;
    this.outputPipe = outputPipe;
    this.processor = processor;
  }

  @Override
  public void run() {
    while (inputPipe.hasMore()) {
      final I value = inputPipe.get();
      if (value != null) {
        outputPipe.put(processor.apply(value));
      }
    }
    outputPipe.close();
  }
}
