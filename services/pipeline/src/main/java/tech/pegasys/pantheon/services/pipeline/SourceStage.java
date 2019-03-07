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

import java.util.Iterator;

class SourceStage<T> implements Runnable {
  private final Iterator<T> source;
  private final OutputPipe<T> output;

  SourceStage(final Iterator<T> source, final OutputPipe<T> output) {
    this.source = source;
    this.output = output;
  }

  @Override
  public void run() {
    while (output.isOpen() && source.hasNext()) {
      final T value = source.next();
      if (value != null) {
        output.put(value);
      }
    }
    output.close();
  }
}
