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
package tech.pegasys.pantheon.tests.acceptance.dsl.condition.blockchain;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Java6Assertions.assertThat;

import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eth.EthTransactions;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.awaitility.Duration;

public class ExpectBlockNotCreated implements Condition {
  private static final Duration WAIT_TIME = new Duration(20, SECONDS);
  private final EthTransactions eth;

  public ExpectBlockNotCreated(final EthTransactions eth) {
    this.eth = eth;
  }

  @Override
  public void verify(final Node node) {
    final BigInteger lastBlock = node.execute(eth.blockNumber());
    Awaitility.await()
        .ignoreExceptions()
        .pollInterval(WAIT_TIME)
        .atMost(30, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(node.execute(eth.blockNumber())).isEqualTo(lastBlock));
  }
}
