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

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Base64;

import net.consensys.cava.crypto.sodium.Box;
import org.hyperledger.besu.ethereum.jsonrpc.internal.response.JsonRpcError;
import org.hyperledger.besu.tests.acceptance.dsl.privacy.PrivacyAcceptanceTestBase;
import org.hyperledger.besu.tests.acceptance.dsl.privacy.PrivacyNode;
import org.hyperledger.besu.tests.web3j.generated.EventEmitter;
import org.junit.Before;
import org.junit.Test;

public class EnclaveErrorAcceptanceTest extends PrivacyAcceptanceTestBase {

  private static final long IBFT2_CHAIN_ID = 4;

  private PrivacyNode alice;
  private PrivacyNode bob;
  private String wrongPublicKey;

  @Before
  public void setUp() throws Exception {
    alice = privacyBesu.createIbft2NodePrivacyEnabled("node1", privacyAccountResolver.resolve(0));
    bob = privacyBesu.createIbft2NodePrivacyEnabled("node2", privacyAccountResolver.resolve(1));
    privacyCluster.start(alice, bob);

    wrongPublicKey =
        Base64.getEncoder().encodeToString(Box.KeyPair.random().publicKey().bytesArray());
  }

  @Test
  public void aliceCannotSendTransactionFromBobNode() {
    final Throwable throwable =
        catchThrowable(
            () ->
                alice.execute(
                    privateContractTransactions.createSmartContract(
                        EventEmitter.class,
                        alice.getTransactionSigningKey(),
                        IBFT2_CHAIN_ID,
                        wrongPublicKey,
                        bob.getEnclaveKey())));

    assertThat(throwable)
        .hasMessageContaining(JsonRpcError.ENCLAVE_NO_MATCHING_PRIVATE_KEY.getMessage());
  }

  @Test
  public void enclaveNoPeerUrlError() {
    final Throwable throwable =
        catchThrowable(
            () ->
                alice.execute(
                    privateContractTransactions.createSmartContract(
                        EventEmitter.class,
                        alice.getTransactionSigningKey(),
                        IBFT2_CHAIN_ID,
                        alice.getEnclaveKey(),
                        wrongPublicKey)));

    assertThat(throwable).hasMessageContaining(JsonRpcError.NODE_MISSING_PEER_URL.getMessage());
  }
}
