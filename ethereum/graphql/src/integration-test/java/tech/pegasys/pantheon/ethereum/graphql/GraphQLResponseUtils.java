/*
 * Copyright 2018 ConsenSys AG.
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
package tech.pegasys.pantheon.ethereum.graphql;

import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.COINBASE;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.DIFFICULTY;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.EXTRA_DATA;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.GAS_LIMIT;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.GAS_USED;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.LOGS_BLOOM;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.MIX_HASH;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.NONCE;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.NUMBER;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.OMMERS_HASH;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.PARENT_HASH;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.RECEIPTS_ROOT;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.STATE_ROOT;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.TIMESTAMP;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.TOTAL_DIFFICULTY;
import static tech.pegasys.pantheon.ethereum.graphql.GraphQLResponseKey.TRANSACTION_ROOT;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHashFunction;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.LogsBloomFilter;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLSuccessResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Block;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Transaction;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockHashFunction;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.core.json.Json;

public class GraphQLResponseUtils {

  /** Hex is base 16 */
  private static final int HEX_RADIX = 16;

  /** @param values hex encoded values. */
  public GraphQLResponse response(final Map<GraphQLResponseKey, String> values) throws IOException {
    return response(values, new ArrayList<>());
  }

  /** @param values hex encoded values. */
  public GraphQLResponse response(
      final Map<GraphQLResponseKey, String> values, final List<Transaction> transactions)
      throws IOException {

    final Hash mixHash = hash(values.get(MIX_HASH));
    final Hash parentHash = hash(values.get(PARENT_HASH));
    final Hash ommersHash = hash(values.get(OMMERS_HASH));
    final Address coinbase = address(values.get(COINBASE));
    final Hash stateRoot = hash(values.get(STATE_ROOT));
    final Hash transactionsRoot = hash(values.get(TRANSACTION_ROOT));
    final Hash receiptsRoot = hash(values.get(RECEIPTS_ROOT));
    final LogsBloomFilter logsBloom = logsBloom(values.get(LOGS_BLOOM));
    final UInt256 difficulty = unsignedInt256(values.get(DIFFICULTY));
    final BytesValue extraData = bytes(values.get(EXTRA_DATA));
    final BlockHashFunction hashFunction = MainnetBlockHashFunction::createHash;
    final long number = unsignedLong(values.get(NUMBER));
    final long gasLimit = unsignedLong(values.get(GAS_LIMIT));
    final long gasUsed = unsignedLong(values.get(GAS_USED));
    final long timestamp = unsignedLong(values.get(TIMESTAMP));
    final long nonce = unsignedLong(values.get(NONCE));
    final UInt256 totalDifficulty = unsignedInt256(values.get(TOTAL_DIFFICULTY));
    //    final int size = unsignedInt(values.get(SIZE));

    final List<JsonNode> ommers = new ArrayList<>();

    final BlockHeader header =
        new BlockHeader(
            parentHash,
            ommersHash,
            coinbase,
            stateRoot,
            transactionsRoot,
            receiptsRoot,
            logsBloom,
            difficulty,
            number,
            gasLimit,
            gasUsed,
            timestamp,
            extraData,
            mixHash,
            nonce,
            hashFunction);

    Block block = new Block(header, 1, ommers.size(), totalDifficulty);

    return Json.mapper
        .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
        .readValue(
            "{ \"data\": {\"block\": " + embedTransactions(block, transactions) + " } }",
            GraphQLSuccessResponse.class);
  }

  private String embedTransactions(final Block block, final List<Transaction> transactions) {
    String blockString = Json.encodePrettily(block);
    return blockString
        .substring(0, blockString.lastIndexOf("}") - 1)
        .concat(
            ", \"transactions\": ["
                + transactions.stream().map(Json::encodePrettily).collect(Collectors.joining(","))
                + "]}");
  }

  public List<Transaction> transactions(final Transaction... transactions) {
    final List<Transaction> list = new ArrayList<>(transactions.length);

    for (final Transaction transaction : transactions) {
      list.add(transaction);
    }

    return list;
  }

  public Transaction transaction(
      final String hash,
      final BigInteger nonce,
      final Integer index,
      final BigInteger value,
      final BigInteger gasPrice,
      final BigInteger gas,
      final String inputData,
      final BigInteger status,
      final BigInteger gasUsed,
      final BigInteger cumulativeGasUsed) {

    return new Transaction(
        hash, nonce, index, value, gasPrice, gas, inputData, status, gasUsed, cumulativeGasUsed);
  }

  private long unsignedLong(final String value) {
    final String hex = removeHexPrefix(value);
    return new BigInteger(hex, HEX_RADIX).longValue();
  }

  private Hash hash(final String hex) {
    return Hash.fromHexString(hex);
  }

  private String removeHexPrefix(final String prefixedHex) {
    return prefixedHex.startsWith("0x") ? prefixedHex.substring(2) : prefixedHex;
  }

  private Address address(final String hex) {
    return Address.fromHexString(hex);
  }

  private LogsBloomFilter logsBloom(final String hex) {
    return LogsBloomFilter.fromHexString(hex);
  }

  private UInt256 unsignedInt256(final String hex) {
    return UInt256.fromHexString(hex);
  }

  private BytesValue bytes(final String hex) {
    return BytesValue.fromHexString(hex);
  }
}
