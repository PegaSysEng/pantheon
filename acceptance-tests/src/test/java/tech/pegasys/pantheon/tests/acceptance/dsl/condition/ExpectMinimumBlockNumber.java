/*
 * Copyright 2018 ConsenSys AG.
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
package tech.pegasys.pantheon.tests.acceptance.dsl.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;

import java.io.IOException;
import java.math.BigInteger;

public class ExpectMinimumBlockNumber implements Condition {

  private final BigInteger blockNumber;

  public ExpectMinimumBlockNumber(final BigInteger blockNumber) {
    this.blockNumber = blockNumber;
  }

  @Override
  public void verify(final Node node) {

    try {
      assertThat(node.eth().blockNumber()).isGreaterThanOrEqualTo(blockNumber);
    } catch (final IOException e) {
      fail("Failed to assert block number was greater then or equal to %s", blockNumber, e);
    }
  }
}
