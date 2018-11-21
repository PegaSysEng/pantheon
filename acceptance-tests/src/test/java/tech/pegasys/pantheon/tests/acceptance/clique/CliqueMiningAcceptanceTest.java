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
package tech.pegasys.pantheon.tests.acceptance.clique;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import java.io.IOException;

import org.junit.Test;

public class CliqueMiningAcceptanceTest extends AcceptanceTestBase {

  @Test
  public void shouldMineTransactionsOnSingleNode() throws IOException {
    final PantheonNode minerNode = pantheon.createCliqueMinerNode("miner1");
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
  public void shouldMineTransactionsOnMultipleNodes() throws IOException {
    final PantheonNode minerNode1 = pantheon.createCliqueMinerNode("miner1");
    final PantheonNode minerNode2 = pantheon.createCliqueMinerNode("miner2");
    final PantheonNode minerNode3 = pantheon.createCliqueMinerNode("miner3");
    cluster.start(minerNode1, minerNode2, minerNode3);

    final Account sender = accounts.createAccount("account1");
    final Account receiver = accounts.createAccount("account2");

    minerNode1.execute(transactions.createTransfer(sender, 50));
    cluster.verify(sender.balanceEquals(50));

    minerNode2.execute(transactions.createIncrementalTransfers(sender, receiver, 1));
    cluster.verify(receiver.balanceEquals(1));

    minerNode3.execute(transactions.createIncrementalTransfers(sender, receiver, 2));
    cluster.verify(receiver.balanceEquals(3));
  }

  @Test
  public void shouldStallMiningWhenInsufficientValidators()
      throws IOException, InterruptedException {
    final PantheonNode minerNode1 = pantheon.createCliqueMinerNode("miner1");
    final PantheonNode minerNode2 = pantheon.createCliqueMinerNode("miner2");
    final PantheonNode minerNode3 = pantheon.createCliqueMinerNode("miner3");
    cluster.start(minerNode1, minerNode2, minerNode3);

    cluster.stopNode(minerNode2);
    cluster.stopNode(minerNode3);

    final Condition lastBlockNumber = blockchain.blockNumberMustBeLatest(minerNode1);
    Thread.sleep(15_000);
    minerNode1.verify(lastBlockNumber);
    Thread.sleep(15_000);
    minerNode1.verify(lastBlockNumber);
  }
}
