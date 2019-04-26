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

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction implements GraphQLResult {

  private final String hash;
  private final BigInteger nonce;
  private final Integer index;
  private final Account from;
  private final Account to;
  private final BigInteger value;
  private final BigInteger gasPrice;
  private final BigInteger gas;
  private final String inputData;
  private final Block block;
  private final BigInteger status;
  private final BigInteger gasUsed;
  private final BigInteger cumulativeGasUsed;
  private final Account createdContract;
  //    private final List<Log> logs;

  @JsonCreator
  public Transaction(
      @JsonProperty("hash") final String hash,
      @JsonProperty("nonce") final BigInteger nonce,
      @JsonProperty("index") final Integer index,
      @JsonProperty("from") final Account from,
      @JsonProperty("to;") final Account to,
      @JsonProperty("value") final BigInteger value,
      @JsonProperty("gasPrice") final BigInteger gasPrice,
      @JsonProperty("gas") final BigInteger gas,
      @JsonProperty("inputData") final String inputData,
      @JsonProperty("block") final Block block,
      @JsonProperty("status") final BigInteger status,
      @JsonProperty("gasUsed") final BigInteger gasUsed,
      @JsonProperty("cumulativeGasUsed") final BigInteger cumulativeGasUsed,
      @JsonProperty("createdContract") final Account createdContract) {
    this.hash = hash;
    this.nonce = nonce;
    this.index = index;
    this.from = from;
    this.to = to;
    this.value = value;
    this.gasPrice = gasPrice;
    this.gas = gas;
    this.inputData = inputData;
    this.block = block;
    this.status = status;
    this.gasUsed = gasUsed;
    this.cumulativeGasUsed = cumulativeGasUsed;
    this.createdContract = createdContract;
  }

  @JsonGetter(value = "hash")
  public String getHash() {
    return hash;
  }

  @JsonGetter(value = "nonce")
  public BigInteger getNonce() {
    return nonce;
  }

  @JsonGetter(value = "index")
  public Integer getIndex() {
    return index;
  }

  @JsonGetter(value = "from")
  public Account getFrom() {
    return from;
  }

  @JsonGetter(value = "to")
  public Account getTo() {
    return to;
  }

  @JsonGetter(value = "value")
  public BigInteger getValue() {
    return value;
  }

  @JsonGetter(value = "gasPrice")
  public BigInteger getGasPrice() {
    return gasPrice;
  }

  @JsonGetter(value = "gas")
  public BigInteger getGas() {
    return gas;
  }

  @JsonGetter(value = "inputData")
  public String getInputData() {
    return inputData;
  }

  @JsonGetter(value = "block")
  public Block getBlock() {
    return block;
  }

  @JsonGetter(value = "status")
  public BigInteger getStatus() {
    return status;
  }

  @JsonGetter(value = "gasUsed")
  public BigInteger getGasUsed() {
    return gasUsed;
  }

  @JsonGetter(value = "cumulativeGasUsed")
  public BigInteger getCumulativeGasUsed() {
    return cumulativeGasUsed;
  }

  @JsonGetter(value = "createdContract")
  public Account getCreatedContract() {
    return createdContract;
  }
}
