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

import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.plugin.data.Address;
import tech.pegasys.pantheon.plugin.data.Quantity;
import tech.pegasys.pantheon.plugin.data.UnformattedData;

import java.math.BigInteger;
import java.util.Optional;

public class TransactionAdapter implements tech.pegasys.pantheon.plugin.data.Transaction {
  private final Transaction transaction;

  public TransactionAdapter(final Transaction transaction) {
    this.transaction = transaction;
  }

  @Override
  public long getNonce() {
    return transaction.getNonce();
  }

  @Override
  public Quantity getGasPrice() {
    return new QuantityAdapter(transaction.getGasPrice().asUInt256());
  }

  @Override
  public long getGasLimit() {
    return transaction.getGasLimit();
  }

  @Override
  public Optional<? extends Address> getTo() {
    return transaction.getTo().isPresent()
        ? Optional.of(new AddressAdapter(transaction.getTo().get()))
        : Optional.empty();
  }

  @Override
  public Quantity getValue() {
    return new QuantityAdapter(transaction.getValue().asUInt256());
  }

  @Override
  public BigInteger getV() {
    return transaction.getV();
  }

  @Override
  public BigInteger getR() {
    return transaction.getR();
  }

  @Override
  public BigInteger getS() {
    return transaction.getS();
  }

  @Override
  public Address getSender() {
    return new AddressAdapter(transaction.getSender());
  }

  @Override
  public Optional<BigInteger> getChainId() {
    return transaction.getChainId();
  }

  @Override
  public Optional<? extends UnformattedData> getInit() {
    return transaction.getTo().isPresent()
        ? Optional.empty()
        : Optional.of(new UnformattedDataAdapter(transaction.getPayload()));
  }

  @Override
  public Optional<? extends UnformattedData> getData() {
    return Optional.of(new UnformattedDataAdapter(transaction.getPayload()));
  }
}
