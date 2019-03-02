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
package tech.pegasys.pantheon.tests.acceptance;

import static org.web3j.utils.Convert.Unit.ETHER;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;
import tech.pegasys.pantheon.tests.acceptance.dsl.blockchain.Amount;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.account.TransferTransaction;

import org.junit.Before;
import org.junit.Test;

public class CreateAccountAcceptanceTest extends AcceptanceTestBase {

  private Node minerNode;

  @Before
  public void setUp() throws Exception {
    minerNode = pantheon.createMinerNode("minerNode");
    cluster.start(minerNode, pantheon.createArchiveNode("archiveNode"));
  }

  @Test
  public void shouldCreateAnAccount() {
    final Account account = accounts.createAccount("account-one");
    minerNode.execute(transactions.createTransfer(account, 20));
    cluster.verify(account.balanceEquals("20", ETHER));
  }

  @Test
  public void transitiveTransfer() {

    // Create accounts: one & two
    final Account accountOne = accounts.createAccount("account-one");
    final Account accountTwo = accounts.createAccount("account-two");

    // Create transfer of 20 ETH from fund pool to account one
    final Amount accountOneStartingBalance = new Amount(20, ETHER);

    // Execute the transfer order
    minerNode.execute(transactions.createTransfer(accountOne, accountOneStartingBalance));

    // Verify account one balance == 20 ETH
    cluster.verify(accountOne.balanceEquals(accountOneStartingBalance));

    // Create transfer of 15 ETH from one to two (using default gas price)
    final Amount transferAmount = new Amount(15, ETHER);
    final TransferTransaction transitiveTransfer =
        transactions.createTransfer(accountOne, accountTwo, transferAmount);

    // Execute the transfer order
    minerNode.execute(transitiveTransfer);

    // Verify account one balance == 20 ETH - 15 ETH - execution cost
    final Amount balanceAfter =
        accountOneStartingBalance
            .subtract(transitiveTransfer.executionCost())
            .subtract(transferAmount);
    cluster.verify(accountOne.balanceEquals(balanceAfter));

    // Verify account two balance == 15 ETH
    cluster.verify(accountTwo.balanceEquals("15", ETHER));
  }
}
