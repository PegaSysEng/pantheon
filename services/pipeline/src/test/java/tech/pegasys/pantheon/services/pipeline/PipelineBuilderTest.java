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

import static com.google.common.primitives.Ints.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.AfterClass;
import org.junit.Test;

public class PipelineBuilderTest {

  private static final ThreadFactory THREAD_FACTORY =
      new ThreadFactoryBuilder()
          .setNameFormat(PipelineBuilderTest.class.getSimpleName() + "-%d")
          .setDaemon(true)
          .build();
  private final Iterator<Integer> tasks =
      asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15).iterator();

  private static final ExecutorService executorService =
      Executors.newCachedThreadPool(THREAD_FACTORY);

  @AfterClass
  public static void afterClass() throws Exception {
    executorService.shutdownNow();
    if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
      fail("Executor service did not shut down cleanly");
    }
  }

  @Test
  public void shouldPipeTasksFromSupplierToCompleter() throws Exception {
    final List<Integer> output = new ArrayList<>();
    final Pipeline pipeline = PipelineBuilder.createPipelineFrom(tasks).andFinishWith(output::add);
    final CompletableFuture<?> result = pipeline.start(executorService);
    result.get(10, TimeUnit.SECONDS);
    assertThat(output).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
  }

  @Test
  public void shouldPassInputThroughIntermediateStage() throws Exception {
    final List<String> output = new ArrayList<>();
    final Pipeline pipeline =
        PipelineBuilder.createPipelineFrom(tasks)
            .thenProcessWith(Object::toString)
            .andFinishWith(output::add);

    final CompletableFuture<?> result = pipeline.start(executorService);
    result.get(10, TimeUnit.SECONDS);
    assertThat(output)
        .containsExactly(
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15");
  }
}
