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
package org.hyperledger.besu.tests.web3j.privacy;

import org.hyperledger.besu.tests.acceptance.dsl.ethsigner.EthSignerClient;
import org.hyperledger.besu.tests.acceptance.dsl.ethsigner.testutil.EthSignerTestHarness;
import org.hyperledger.besu.tests.acceptance.dsl.ethsigner.testutil.EthSignerTestHarnessFactory;
import org.hyperledger.besu.tests.acceptance.dsl.privacy.PrivacyAcceptanceTestBase;
import org.hyperledger.besu.tests.acceptance.dsl.privacy.PrivacyNet;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.eea.PrivateTransactionBuilder;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

public class EthSignerAcceptanceTest extends PrivacyAcceptanceTestBase {
  private PrivacyNet privacyNet;

  private EthSignerClient ethSignerClient;

  @Before
  public void setUp() throws Exception {
    privacyNet =
        PrivacyNet.builder(privacy, privacyPantheon, cluster, false).addMinerNode("Alice").build();
    privacyNet.startPrivacyNet();

    final EthSignerTestHarness ethSigner =
        EthSignerTestHarnessFactory.create(
            privacy.newFolder().toPath(),
            "ethSignerKey--fe3b557e8fb62b89f4916b721be55ceb828dbd73.json",
            privacyNet.getNode("Alice").getJsonRpcSocketPort().orElseThrow(),
            2018);
    ethSignerClient = new EthSignerClient(ethSigner.getHttpListeningUrl());
  }

  @Test
  public void privateSmartContractMustDeploy() throws IOException {
    final BytesValue privacyGroupId = PrivacyGroup.generatePrivacyGroup(privacyNet, "Alice");
    final long nonce = privacyNet.getNode("Alice").nextNonce(privacyGroupId);

    final String transactionHash =
        ethSignerClient.eeaSendTransaction(
            null,
            BigInteger.valueOf(63992),
            BigInteger.valueOf(1000),
            PrivateTransactionBuilder.EVENT_EMITTER_CONSTRUCTOR.toString(),
            BigInteger.valueOf(nonce),
            privacyNet.getEnclave("Alice").getPublicKeys().get(0),
            Collections.emptyList(),
            "restricted");

    privacyNet.getNode("Alice").verify(eea.expectSuccessfulTransactionReceipt(transactionHash));

    final String expectedContractAddress =
        Address.privateContractAddress(
                privacyNet.getNode("Alice").getAddress(), nonce, privacyGroupId)
            .toString();

    privateTransactionVerifier
        .validPrivateContractDeployed(expectedContractAddress)
        .verify(privacyNet.getNode("Alice"), transactionHash);
  }
}
