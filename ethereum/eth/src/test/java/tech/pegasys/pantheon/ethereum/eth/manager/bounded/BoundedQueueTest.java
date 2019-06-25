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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;

import org.junit.Test;

public class BoundedQueueTest {

  @Test
  public void offerShouldAcceptNewElements() {
    int size = 10;
    final BoundedQueue queue = new BoundedQueue(size, "test", new NoOpMetricsSystem());
    for (int i = 0; i < size; i++) {
      assertTrue(queue.offer(() -> {}));
      assertEquals(i + 1, queue.size());
    }
  }

  @Test
  public void offerShouldMakeARoomAndAcceptNewElementAtFullCapacity() {
    final BoundedQueue queue = new BoundedQueue(1, "test", new NoOpMetricsSystem());
    assertTrue(queue.offer(() -> {}));
    assertEquals(1, queue.size());
    assertTrue(queue.offer(() -> {}));
    assertEquals(1, queue.size());
  }
}
