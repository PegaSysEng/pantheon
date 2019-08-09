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
package tech.pegasys.pantheon.ethereum.plugin.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.core.TransactionTestFixture;

import org.junit.Test;

public class TransactionAdapterTest {

  private final Transaction transaction =
      new TransactionTestFixture().createTransaction(SECP256K1.KeyPair.generate());
  private final TransactionAdapter transactionAdapter = new TransactionAdapter(transaction);

  @Test
  public void getNonce() {
    assertThat(transactionAdapter.getNonce()).isEqualTo(transaction.getNonce());
  }

  @Test
  public void getGasPrice() {
    assertThat(transactionAdapter.getGasPrice().getValue())
        .isEqualTo(transaction.getGasPrice().toLong());
  }

  @Test
  public void getGasLimit() {
    assertThat(transactionAdapter.getGasLimit()).isEqualTo(transaction.getGasLimit());
  }

  @Test
  public void getValue() {
    assertThat(transactionAdapter.getValue().getValue()).isEqualTo(transaction.getValue().toLong());
  }

  @Test
  public void getV() {
    assertThat(transactionAdapter.getV()).isEqualTo(transaction.getV());
  }

  @Test
  public void getR() {
    assertThat(transactionAdapter.getR()).isEqualTo(transaction.getR());
  }

  @Test
  public void getS() {
    assertThat(transactionAdapter.getS()).isEqualTo(transaction.getS());
  }

  @Test
  public void getSender() {
    assertThat(transactionAdapter.getSender().getByteArray())
        .isEqualTo(transaction.getSender().extractArray());
  }

  @Test
  public void getChainId() {
    assertThat(transactionAdapter.getChainId()).isEqualTo(transaction.getChainId());
  }
}
