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
package tech.pegasys.pantheon.tests.acceptance.jsonrpc;

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import org.junit.Before;
import org.junit.Test;

public class EthGetTransactionReceiptAcceptanceTest extends AcceptanceTestBase {

  private PantheonNode minerNode;
  private Account sender;
  private Account recipient;

  @Before
  public void setUp() throws Exception {
    sender = accounts.createAccount("sender");
    recipient = accounts.createAccount("recipient");
    minerNode = pantheon.createMinerNode("node");
    cluster.start(minerNode);
    minerNode.execute(transactions.createTransfer(sender, 10));
    cluster.verify(sender.balanceEquals(10));
  }

  @Test
  public void mustCorrectlyQueryTransactionReceiptFromContractCreationTransaction() {
    final Hash transactionHash = minerNode.execute(transactions.createTransfer(recipient, 5));
    cluster.verify(recipient.balanceEquals(5));
    minerNode.verify(eth.getTransactionReceipt(transactionHash));
  }

  @Test
  public void mustCorrectlyQueryTransactionReceiptFromNormalTransaction() {
    final Hash transactionHash =
        minerNode.execute(transactions.createTransfer(sender, recipient, 5));
    cluster.verify(recipient.balanceEquals(5));
    minerNode.verify(eth.getTransactionReceipt(transactionHash));
  }

  @Test
  public void mustReturnNullWhenTransactionDoesNotExist() {
    minerNode.verify(
        eth.getTransactionReceiptNotAvailable(
            "0x0000000000000000000000000000000000000000000000000000000000000000"));
  }
}
