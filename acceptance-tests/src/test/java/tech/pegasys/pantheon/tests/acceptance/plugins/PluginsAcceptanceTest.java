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
package tech.pegasys.pantheon.tests.acceptance.plugins;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.cluster.Cluster;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.cluster.ClusterConfiguration;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.cluster.ClusterConfigurationBuilder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PluginsAcceptanceTest extends AcceptanceTestBase {
  private PantheonNode node;

  @Before
  public void setUp() throws Exception {
    node = pantheon.createPluginsNode("node1", Collections.singletonList("testPlugin"));
    cluster.start(node);
  }

  @Test
  public void shouldRegister() {
    waitForFile(node.homeDirectory().resolve("plugins/testPlugin.registered"));
  }

  @Test
  public void shouldStart() {
    waitForFile(node.homeDirectory().resolve("plugins/testPlugin.started"));
  }

  @Test
  @Ignore("No way to do a graceful shutdown of Pantheon at the moment.")
  public void shouldStop() {
    cluster.stopNode(node);
    waitForFile(node.homeDirectory().resolve("plugins/testPlugin.stopped"));
  }

  private void waitForFile(final Path path) {
    final File file = path.toFile();
    Awaitility.waitAtMost(30, TimeUnit.SECONDS)
        .until(
            () -> {
              if (file.exists()) {
                try (final Stream<String> s = Files.lines(path)) {
                  return s.count() > 0;
                }
              } else {
                return false;
              }
            });
  }
}
