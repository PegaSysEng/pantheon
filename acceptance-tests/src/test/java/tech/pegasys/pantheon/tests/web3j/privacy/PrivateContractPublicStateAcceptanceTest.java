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
package tech.pegasys.pantheon.tests.web3j.privacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import tech.pegasys.pantheon.tests.acceptance.dsl.privacy.PrivacyAcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.privacy.PrivacyNode;
import tech.pegasys.pantheon.tests.web3j.generated.CrossContractReader;
import tech.pegasys.pantheon.tests.web3j.generated.EventEmitter;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.eea.response.PrivateTransactionReceipt;
import org.web3j.tx.exceptions.ContractCallException;

public class PrivateContractPublicStateAcceptanceTest extends PrivacyAcceptanceTestBase {
  private static final long POW_CHAIN_ID = 2018;

  private PrivacyNode minerNode;

  @Before
  public void setUp() throws Exception {
    minerNode =
        privacyPantheon.createPrivateTransactionEnabledMinerNode(
            "miner-node", privacyAccountResolver.resolve(0));
    privacyCluster.start(minerNode);
  }

  @Test
  public void mustAllowAccessToPublicStateFromPrivateTx() throws Exception {
    final EventEmitter publicEventEmitter =
        minerNode
            .getPantheon()
            .execute((contractTransactions.createSmartContract(EventEmitter.class)));

    final TransactionReceipt receipt = publicEventEmitter.store(BigInteger.valueOf(12)).send();
    assertNotNull(receipt);

    final CrossContractReader reader =
        minerNode
            .getPantheon()
            .execute(
                privateContractTransactions.createSmartContract(
                    CrossContractReader.class,
                    minerNode.getTransactionSigningKey(),
                    POW_CHAIN_ID,
                    minerNode.getEnclaveKey()));
    Assert.assertEquals(
        reader.read(publicEventEmitter.getContractAddress()).send(), BigInteger.valueOf(12));
  }

  @Test(expected = ContractCallException.class)
  public void mustNotAllowAccessToPrivateStateFromPublicTx() throws Exception {
    final EventEmitter privateEventEmitter =
        minerNode
            .getPantheon()
            .execute(
                (privateContractTransactions.createSmartContract(
                    EventEmitter.class,
                    minerNode.getTransactionSigningKey(),
                    POW_CHAIN_ID,
                    minerNode.getEnclaveKey())));

    final TransactionReceipt receipt = privateEventEmitter.store(BigInteger.valueOf(12)).send();
    assertNotNull(receipt);

    final CrossContractReader publicReader =
        minerNode
            .getPantheon()
            .execute(contractTransactions.createSmartContract(CrossContractReader.class));

    publicReader.read(privateEventEmitter.getContractAddress()).send();
  }

  @Test
  public void privateContractMustNotBeAbleToCallPublicContractWhichChangesState() throws Exception {
    final CrossContractReader privateReader =
        minerNode
            .getPantheon()
            .execute(
                privateContractTransactions.createSmartContract(
                    CrossContractReader.class,
                    minerNode.getTransactionSigningKey(),
                    POW_CHAIN_ID,
                    minerNode.getEnclaveKey()));

    final CrossContractReader publicReader =
        minerNode
            .getPantheon()
            .execute(contractTransactions.createSmartContract(CrossContractReader.class));

    final PrivateTransactionReceipt transactionReceipt =
        (PrivateTransactionReceipt)
            privateReader.incrementRemote(publicReader.getContractAddress()).send();

    assertEquals("0x", transactionReceipt.getOutput());
  }

  @Test
  public void privateContractMustNotBeAbleToCallPublicContractWhichInstantiatesContract()
      throws Exception {
    final CrossContractReader privateReader =
        minerNode
            .getPantheon()
            .execute(
                privateContractTransactions.createSmartContract(
                    CrossContractReader.class,
                    minerNode.getTransactionSigningKey(),
                    POW_CHAIN_ID,
                    minerNode.getEnclaveKey()));

    final CrossContractReader publicReader =
        minerNode
            .getPantheon()
            .execute(contractTransactions.createSmartContract(CrossContractReader.class));

    final PrivateTransactionReceipt transactionReceipt =
        (PrivateTransactionReceipt)
            privateReader.deployRemote(publicReader.getContractAddress()).send();

    assertEquals(0, transactionReceipt.getLogs().size());
  }

  @Test
  public void privateContractMustNotBeAbleToCallSelfDetructOfPublicContract() throws Exception {
    final CrossContractReader privateReader =
        minerNode
            .getPantheon()
            .execute(
                privateContractTransactions.createSmartContract(
                    CrossContractReader.class,
                    minerNode.getTransactionSigningKey(),
                    POW_CHAIN_ID,
                    minerNode.getEnclaveKey()));

    final CrossContractReader publicReader =
        minerNode
            .getPantheon()
            .execute(contractTransactions.createSmartContract(CrossContractReader.class));

    final PrivateTransactionReceipt transactionReceipt =
        (PrivateTransactionReceipt)
            privateReader.remoteDestroy(publicReader.getContractAddress()).send();

    assertEquals("0x", transactionReceipt.getOutput());
  }
}
