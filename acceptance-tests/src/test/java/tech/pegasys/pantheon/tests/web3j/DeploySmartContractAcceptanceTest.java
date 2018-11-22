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
package tech.pegasys.pantheon.tests.web3j;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;
import tech.pegasys.pantheon.tests.web3j.generated.SimpleStorage;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class DeploySmartContractAcceptanceTest extends AcceptanceTestBase {

  private PantheonNode minerNode;

  @Before
  public void setUp() throws Exception {
    minerNode = pantheon.createMinerNode("miner-node");
    cluster.start(minerNode);
  }

  // {'id': 1, 'jsonrpc': '2.0', 'result':
  // '0xbba27352c4f655a15fc9d85bc79166b13592528063642b6e95c9a74f2c9bcbcf'}

  @Test
  public void deployContract() throws Exception {
    final SimpleStorage contract =
        minerNode.execute(transactions.createSmartContract(SimpleStorage.class));

    assertNotNull(contract);

    final Optional<TransactionReceipt> receipt = contract.getTransactionReceipt();

    assertNotNull(receipt);
    assertTrue(receipt.isPresent());
    assertNotNull(receipt.get().getTransactionHash());

    // TODO expect the getReceipt to equal the provided receipt?

    final Hash transactionHash = Hash.fromHexString(receipt.get().getTransactionHash());

    minerNode.verify(eth.expectSuccessfulTransactionReceipt(transactionHash));
  }
}
