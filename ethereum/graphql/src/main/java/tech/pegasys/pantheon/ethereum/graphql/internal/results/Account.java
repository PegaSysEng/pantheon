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
package tech.pegasys.pantheon.ethereum.graphql.internal.results;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Account implements GraphQLResult {

  private final Address address;
  private final BigInteger balance;
  private final BigInteger transactionCount;
  private final BytesValue code;

  public Account(Address address) {
    this(address, BigInteger.ZERO, BigInteger.ZERO, BytesValue.EMPTY);
  }

  public Account(tech.pegasys.pantheon.ethereum.core.Account account) {
    this(
        account.getAddress(),
        BigInteger.valueOf(account.getBalance().toLong()),
        BigInteger.valueOf(account.getNonce()),
        account.getCode());
  }

  @JsonCreator
  public Account(
      @JsonProperty("address") final Address address,
      @JsonProperty("balance") final BigInteger balance,
      @JsonProperty("transactionCount") final BigInteger transactionCount,
      @JsonProperty("code") final BytesValue code) {
    this.address = address;
    this.balance = balance;
    this.transactionCount = transactionCount;
    this.code = code;
  }

  @JsonGetter(value = "address")
  public Address getAddress() {
    return address;
  }

  @JsonGetter(value = "balance")
  public BigInteger getBalance() {
    return balance;
  }

  @JsonGetter(value = "transactionCount")
  public BigInteger getTransactionCount() {
    return transactionCount;
  }

  @JsonGetter(value = "code")
  public BytesValue getCode() {
    return code;
  }
}
