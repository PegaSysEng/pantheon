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
  //    private final Bytes32 storage;

  //    public Block(final BlockHeader header,
  //                 final List<TransactionResult> transactions,
  //                 final List<JsonNode> ommers,
  //                 final UInt256 totalDifficulty,
  //                 final int size) {
  //        this(BigInteger.valueOf(header.getNumber()),
  //                header.getHash().toString(),
  //                Quantity.longToPaddedHex(header.getNonce(), 8),
  //                header.getTransactionsRoot().toString(),
  //                transactions.size(),
  //                header.getStateRoot().toString(),
  //                header.getReceiptsRoot().toString(),
  //                header.getExtraData().toString(),
  //                BigInteger.valueOf(header.getGasLimit()),
  //                BigInteger.valueOf(header.getGasUsed()),
  //                BigInteger.valueOf(header.getTimestamp()),
  //                header.getLogsBloom().toString(),
  //                header.getMixHash().toString(),
  //                BigInteger.valueOf(header.getDifficulty().toLong()),
  //                BigInteger.valueOf(totalDifficulty.toLong()),
  //                ommers.size(),
  //                header.getOmmersHash().toString());
  //    }

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
