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
package org.hyperledger.besu.tests.acceptance.dsl.condition.eea;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.hyperledger.besu.tests.acceptance.dsl.node.PantheonNode;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.eea.EeaRequestFactory;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.eea.EeaTransactions;
import org.hyperledger.besu.util.bytes.BytesValue;
import org.web3j.utils.Numeric;

public class ExpectValidPrivateContractValuesReturned extends GetValidPrivateTransactionReceipt {

  private final String returnValue;

  public ExpectValidPrivateContractValuesReturned(
      final String returnValue, final EeaConditions eea, final EeaTransactions transactions) {
    super(eea, transactions);
    this.returnValue = returnValue;
  }

  @Override
  public void verify(final PantheonNode node, final String transactionHash) {
    final EeaRequestFactory.PrivateTransactionReceipt privateTxReceipt =
        getPrivateTransactionReceipt(node, transactionHash);

    BytesValue output = BytesValue.fromHexString(privateTxReceipt.getOutput());
    assertEquals(new BigInteger(returnValue), Numeric.decodeQuantity(output.toString()));
  }
}
