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
package tech.pegasys.pantheon.services.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class FlatFileTaskCollectionTest
    extends AbstractTaskQueueTest<FlatFileTaskCollection<BytesValue>> {

  @Rule public final TemporaryFolder folder = new TemporaryFolder();

  @Override
  protected FlatFileTaskCollection<BytesValue> createQueue() throws IOException {
    final Path dataDir = folder.newFolder().toPath();
    return createQueue(dataDir);
  }

  private FlatFileTaskCollection<BytesValue> createQueue(final Path dataDir) {
    return new FlatFileTaskCollection<>(dataDir, Function.identity(), Function.identity(), 10);
  }

  @After
  public void checkFilesCleanedUp() {
    final String[] files = folder.getRoot().list((dir, name) -> name.startsWith("tasks"));
    assertThat(files.length).isEqualTo(0);
  }
}
