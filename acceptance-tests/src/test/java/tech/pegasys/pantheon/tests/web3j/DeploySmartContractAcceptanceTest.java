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

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;
import tech.pegasys.pantheon.tests.web3j.generated.SimpleStorage;

import java.math.BigInteger;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;

public class DeploySmartContractAcceptanceTest extends AcceptanceTestBase {

  private PantheonNode minerNode;

  @Before
  public void setUp() throws Exception {
    minerNode = pantheon.createMinerNode("miner-node");
    cluster.start(minerNode);
  }

  @Test
  public void deployingMustGiveValidReceipt() {
    final SimpleStorage contract =
        minerNode.execute(transactions.createSmartContract(SimpleStorage.class));

    final TransactionReceipt transactionReceipt = expectingPresentReceipt(contract);

    verifyReceipt(transactionReceipt);
  }

  // TODO move this into the DSL?
  private void verifyReceipt(final TransactionReceipt transactionReceipt) {

    // Contract transaction has no 'to' address or contract address
    assertThat(transactionReceipt.getTo()).isNull();

    assertThat(transactionReceipt.getRoot()).isNull();

    // Changeable - just check existence
    assertThat(transactionReceipt.getBlockHash()).isNotBlank();
    assertThat(transactionReceipt.getTransactionHash()).isNotBlank();
    assertThat(transactionReceipt.getTransactionIndex()).isNotNull();
    assertThat(transactionReceipt.getTransactionHash()).isNotNull();

    // Block zero is the genesis, must be after that
    assertThat(transactionReceipt.getBlockNumber()).isGreaterThanOrEqualTo(BigInteger.ONE);

    // Address generation is deterministic, based on the sender address, nonce and data
    assertThat(transactionReceipt.getContractAddress())
        .isEqualTo("0x42699a7612a82f1d9c36148af9c77354759b210b");

    // Success code is '0x1'
    assertThat(transactionReceipt.getStatus()).isEqualTo("0x1");

    // TODO where is the from address obtained? shouldn't it be the sender address?
    assertThat(transactionReceipt.getFrom())
        .isEqualTo("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73");

    // No logs from expected from the contract deployment
    assertThat(transactionReceipt.getLogs()).isNotNull();
    assertThat(transactionReceipt.getLogs().size()).isEqualTo(0);
    assertThat(transactionReceipt.getLogsBloom())
        .isEqualTo(
            "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
  }

  // TODO wrap this up? (or rename)
  private TransactionReceipt expectingPresentReceipt(final Contract contract) {
    assertThat(contract).isNotNull();
    final Optional<TransactionReceipt> receipt = contract.getTransactionReceipt();
    assertThat(receipt).isNotNull();
    return receipt.get();
  }
}
