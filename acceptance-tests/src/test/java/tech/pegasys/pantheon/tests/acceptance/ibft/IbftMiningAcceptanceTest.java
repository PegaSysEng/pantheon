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
package tech.pegasys.pantheon.tests.acceptance.ibft;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import java.io.IOException;
import java.util.ArrayList;

import com.google.common.collect.Lists;
import org.junit.Test;

public class IbftMiningAcceptanceTest extends AcceptanceTestBase {

  @Test
  public void shouldMineOnSingleNode() throws IOException {
    final PantheonNode minerNode = pantheon.createIbftNode("miner1");
    cluster.start(minerNode);

    final Account sender = accounts.createAccount("account1");
    final Account receiver = accounts.createAccount("account2");

    minerNode.execute(transactions.createTransfer(sender, 50));
    cluster.verify(sender.balanceEquals(50));

    minerNode.execute(transactions.createIncrementalTransfers(sender, receiver, 1));
    cluster.verify(receiver.balanceEquals(1));

    minerNode.execute(transactions.createIncrementalTransfers(sender, receiver, 2));
    cluster.verify(receiver.balanceEquals(3));
  }

  @Test
  public void shouldMineOnMultipleNodes() throws IOException {
    final PantheonNode minerNode1 = pantheon.createIbftNode("miner1");
    final PantheonNode minerNode2 = pantheon.createIbftNode("miner2");
    final PantheonNode minerNode3 = pantheon.createIbftNode("miner3");
    final PantheonNode minerNode4 = pantheon.createIbftNode("miner4");
    cluster.start(minerNode1, minerNode2, minerNode3, minerNode4);

    final Account sender = accounts.createAccount("account1");
    final Account receiver = accounts.createAccount("account2");

    minerNode1.execute(transactions.createTransfer(sender, 50));
    cluster.verify(sender.balanceEquals(50));

    minerNode2.execute(transactions.createIncrementalTransfers(sender, receiver, 1));
    cluster.verify(receiver.balanceEquals(1));

    minerNode3.execute(transactions.createIncrementalTransfers(sender, receiver, 2));
    cluster.verify(receiver.balanceEquals(3));

    minerNode4.execute(transactions.createIncrementalTransfers(sender, receiver, 3));
    cluster.verify(receiver.balanceEquals(6));
  }

  @Test
  public void shouldStillMineWhenANodeFailsAndHasSufficientValidators() throws IOException {
    final PantheonNode minerNode1 = pantheon.createIbftNode("miner1");
    final PantheonNode minerNode2 = pantheon.createIbftNode("miner2");
    final PantheonNode minerNode3 = pantheon.createIbftNode("miner3");
    final PantheonNode minerNode4 = pantheon.createIbftNode("miner4");
    final PantheonNode[] nodes = {minerNode1, minerNode2, minerNode3, minerNode4};
    cluster.start(nodes);

    final Account receiver = accounts.createAccount("account2");

    final PantheonNode stoppedNode = ibft.lastValidator(nodes);
    ArrayList<Node> activeNodes = Lists.newArrayList(nodes);
    activeNodes.remove(stoppedNode);

    cluster.stopNode(stoppedNode);
    activeNodes.get(0).execute(transactions.createTransfer(receiver, 80));

    cluster.verify(receiver.balanceEquals(80), activeNodes);
  }
}
