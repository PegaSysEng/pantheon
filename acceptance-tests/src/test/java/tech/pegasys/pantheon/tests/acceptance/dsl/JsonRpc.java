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
package tech.pegasys.pantheon.tests.acceptance.dsl;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.ExpectSha3;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Cluster;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Web3Transactions;

public class JsonRpc {

  private final Cluster nodes;
  private final Web3Transactions web3;

  public JsonRpc(final Cluster nodes, final Web3Transactions web3) {
    this.nodes = nodes;
    this.web3 = web3;
  }

  public void waitForPeersConnected(final PantheonNode node, final int expectedNumberOfPeers) {
    WaitUtils.waitFor(() -> assertThat(node.getPeerCount()).isEqualTo(expectedNumberOfPeers));
  }

  public Condition web3sha3(final String input, final String expectedHash) {
    return new ExpectSha3(web3.sha3(input), expectedHash);
  }
}
