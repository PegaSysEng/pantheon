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

class ExternalPipelineSource<T> implements PipelineSource<T> {

  private final Pipe<T> pipe;

  public ExternalPipelineSource(final Pipe<T> pipe) {
    this.pipe = pipe;
  }

  @Override
  public InputPipe<T> getPipe() {
    return pipe;
  }

  @Override
  public void abort() {
    pipe.abort();
  }
}
