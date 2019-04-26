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

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.graphql.internal.dao.Quantity;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Ommer implements GraphQLResult {

  private final BigInteger number;
  private final String hash;
  //  private Ommer parent;
  private final String nonce;
  private final String transactionsRoot;
  private final String stateRoot;
  private final String receiptsRoot;
  private final String extraData;
  private final BigInteger gasLimit;
  private final BigInteger gasUsed;
  private final BigInteger timestamp;
  private final String logsBloom;
  private final String mixHash;
  private final BigInteger difficulty;
  //  private final BigInteger totalDifficulty;
  //  private final String ommerHash;
  //  private final List<Log> logs;
  //  private final Account account;
  //  private final CallResult call;
  //  private final Long estimateGas;

  public Ommer(final BlockHeader header) {
    this(
        BigInteger.valueOf(header.getNumber()),
        header.getHash().toString(),
        Quantity.longToPaddedHex(header.getNonce(), 8),
        header.getTransactionsRoot().toString(),
        header.getStateRoot().toString(),
        header.getReceiptsRoot().toString(),
        header.getExtraData().toString(),
        BigInteger.valueOf(header.getGasLimit()),
        BigInteger.valueOf(header.getGasUsed()),
        BigInteger.valueOf(header.getTimestamp()),
        header.getLogsBloom().toString(),
        header.getMixHash().toString(),
        BigInteger.valueOf(header.getDifficulty().toLong()));
  }

  @JsonCreator
  public Ommer(
      @JsonProperty("number") final BigInteger number,
      @JsonProperty("hash") final String hash,
      @JsonProperty("nonce") final String nonce,
      @JsonProperty("transactionsRoot") final String transactionsRoot,
      @JsonProperty("stateRoot") final String stateRoot,
      @JsonProperty("receiptsRoot") final String receiptsRoot,
      @JsonProperty("extraData") final String extraData,
      @JsonProperty("gasLimit") final BigInteger gasLimit,
      @JsonProperty("gasUsed") final BigInteger gasUsed,
      @JsonProperty("timestamp") final BigInteger timestamp,
      @JsonProperty("logsBloom") final String logsBloom,
      @JsonProperty("mixHash") final String mixHash,
      @JsonProperty("difficulty") final BigInteger difficulty) {
    this.number = number;
    this.hash = hash;
    this.nonce = nonce;
    this.transactionsRoot = transactionsRoot;
    this.stateRoot = stateRoot;
    this.receiptsRoot = receiptsRoot;
    this.extraData = extraData;
    this.gasLimit = gasLimit;
    this.gasUsed = gasUsed;
    this.timestamp = timestamp;
    this.logsBloom = logsBloom;
    this.mixHash = mixHash;
    this.difficulty = difficulty;
  }

  @JsonGetter(value = "number")
  public BigInteger getNumber() {
    return number;
  }

  @JsonGetter(value = "hash")
  public String getHash() {
    return hash;
  }

  @JsonGetter(value = "nonce")
  public String getNonce() {
    return nonce;
  }

  @JsonGetter(value = "transactionsRoot")
  public String getTransactionsRoot() {
    return transactionsRoot;
  }

  @JsonGetter(value = "stateRoot")
  public String getStateRoot() {
    return stateRoot;
  }

  @JsonGetter(value = "receiptsRoot")
  public String getReceiptsRoot() {
    return receiptsRoot;
  }

  @JsonGetter(value = "extraData")
  public String getExtraData() {
    return extraData;
  }

  @JsonGetter(value = "gasLimit")
  public BigInteger getGasLimit() {
    return gasLimit;
  }

  @JsonGetter(value = "gasUsed")
  public BigInteger getGasUsed() {
    return gasUsed;
  }

  @JsonGetter(value = "timestamp")
  public BigInteger getTimestamp() {
    return timestamp;
  }

  @JsonGetter(value = "logsBloom")
  public String getLogsBloom() {
    return logsBloom;
  }

  @JsonGetter(value = "mixHash")
  public String getMixHash() {
    return mixHash;
  }

  @JsonGetter(value = "difficulty")
  public BigInteger getDifficulty() {
    return difficulty;
  }
}
