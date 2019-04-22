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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tech.pegasys.pantheon.ethereum.graphql.internal.dao.BlockResult;

import java.math.BigInteger;
public class Block implements GraphQLResult {

  private final Long number;
  private final String hash;
  private final String parentHash;
  private final String nonce;
  private final String transactionsRoot;
  private final Integer transactionCount;
  private final String stateRoot;
  private final String receiptsRoot;
  private final String extraData;
  private final Long gasLimit;
  private final Long gasUsed;
  private final BigInteger timestamp;
  private final String logsBloom;
  //    private final String mixHash;
  private final BigInteger difficulty;
  private final BigInteger totalDifficulty;
  private final Integer ommerCount;
  //    private final List<Block> ommers;
      private final String ommerHash;
  //    private final List<Transaction> transactions;

  //    public Account miner(Long block) {
  //
  //    }

  public static Block createFromBlockResult(final BlockResult blockResult) {
    return new Block(
            blockResult.getNumber(),
            blockResult.getHash(),
            blockResult.getParentHash(),
            blockResult.getNonce(),
            blockResult.getTransactionsRoot(),
            blockResult.getTransactions() == null? null : String.valueOf(blockResult.getTransactions().size()),
            blockResult.getStateRoot(),
            blockResult.getReceiptsRoot(),
            blockResult.getExtraData(),
            blockResult.getGasLimit(),
            blockResult.getGasUsed(),
            blockResult.getTimestamp(),
            blockResult.getLogsBloom(),
            blockResult.getDifficulty(),
            blockResult.getTotalDifficulty(),
            blockResult.getOmmers() == null? null : String.valueOf(blockResult.getOmmers().size()),
            blockResult.getSha3Uncles());
  }

  private Block(
          final String number,
          final String hash,
          final String parentHash,
          final String nonce,
          final String transactionsRoot,
          final String transactionCount,
          final String stateRoot,
          final String receiptsRoot,
          final String extraData,
          final String gasLimit,
          final String gasUsed,
          final String timestamp,
          final String logsBloom,
          final String difficulty,
          final String totalDifficulty,
          final String ommerCount,
          final String ommerHash) {
    this.number = convertToLong(number);
    this.hash = hash;
    this.parentHash = parentHash;
    this.nonce = nonce;
    this.transactionsRoot = transactionsRoot;
    this.transactionCount = convertToInteger(transactionCount);
    this.stateRoot = stateRoot;
    this.receiptsRoot = receiptsRoot;
    this.extraData = extraData;
    this.gasLimit = convertToLong(gasLimit);
    this.gasUsed = convertToLong(gasUsed);
    this.timestamp = convertToBigInt(timestamp);
    this.logsBloom = logsBloom;
    this.difficulty = convertToBigInt(difficulty);
    this.totalDifficulty = convertToBigInt(totalDifficulty);
    this.ommerCount = convertToInteger(ommerCount);
    this.ommerHash = ommerHash;
  }

  private Integer convertToInteger(final String i) {
    if (i == null || i.isEmpty()) {
      return null;
    }
    if (i.length() > 2 && i.substring(0, 2).equals("0x")) {
      return new BigInteger(i.substring(2), 16).intValue();
    }
    return Integer.parseInt(i);
  }

  private BigInteger convertToBigInt(final String s) {
    if (s == null || s.isEmpty()) {
      return null;
    }
    if (s.substring(0, 2).equals("0x")) {
      return new BigInteger(s.substring(2), 16);
    }
    return null;
  }

  private Long convertToLong(final String n) {
    if (n == null || n.isEmpty()) {
      return null;
    }
    if (n.substring(0, 2).equals("0x")) {
      return new BigInteger(n.substring(2), 16).longValue();
    }
    return null;
  }

  @JsonGetter(value = "number")
  public Long getNumber() {
    return number;
  }

  @JsonGetter(value = "hash")
  public String getHash() {
    return hash;
  }

  @JsonGetter(value = "parentHash")
  public String getParentHash() {
    return parentHash;
  }

  @JsonGetter(value = "nonce")
  public String getNonce() {
    return nonce;
  }

  @JsonGetter(value = "transactionRoot")
  public String getTransactionsRoot() {
    return transactionsRoot;
  }

  @JsonGetter(value = "transactionCount")
  public Integer getTransactionCount() {
    return transactionCount;
  }

  @JsonGetter(value = "stateRoot")
  public String getStateRoot() {
    return stateRoot;
  }
  @JsonGetter(value = "receiptRoot")
  public String getReceiptsRoot() {
    return receiptsRoot;
  }
  @JsonGetter(value = "extraData")
  public String getExtraData() {
    return extraData;
  }

  @JsonGetter(value = "gasLimit")
  public Long getGasLimit() {
    return gasLimit;
  }

  @JsonGetter(value = "gasUsed")
  public Long getGasUsed() {
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

  @JsonGetter(value = "difficulty")
  public BigInteger getDifficulty() {
    return difficulty;
  }

  @JsonGetter(value = "totalDifficulty")
  public BigInteger getTotalDifficulty() {
    return totalDifficulty;
  }

  @JsonGetter(value = "ommerCount")
  public Integer getOmmerCount() {
    return ommerCount;
  }

  @JsonGetter(value = "ommerHash")
  public String getOmmerHash() {
    return ommerHash;
  }
}

