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
package tech.pegasys.pantheon.ethereum.graphql.internal.request;

import tech.pegasys.pantheon.ethereum.core.Address;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CallData {

  private final Address from;
  private final Address to;
  private final BigInteger gas;
  private final BigInteger gasPrice;
  private final BigInteger value;
  private final String data;

  @JsonCreator
  public CallData(
      @JsonProperty("from") final Address from,
      @JsonProperty("to") final Address to,
      @JsonProperty("gas") final BigInteger gas,
      @JsonProperty("gasPrice") final BigInteger gasPrice,
      @JsonProperty("value") final BigInteger value,
      @JsonProperty("data") final String data) {
    this.from = from;
    this.to = to;
    this.gas = gas;
    this.gasPrice = gasPrice;
    this.value = value;
    this.data = data;
  }

  public Address getFrom() {
    return from;
  }

  public Address getTo() {
    return to;
  }

  public BigInteger getGas() {
    return gas;
  }

  public BigInteger getGasPrice() {
    return gasPrice;
  }

  public BigInteger getValue() {
    return value;
  }

  public String getData() {
    return data;
  }
}
