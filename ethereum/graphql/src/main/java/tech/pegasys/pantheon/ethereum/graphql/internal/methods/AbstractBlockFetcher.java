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
package tech.pegasys.pantheon.ethereum.graphql.internal.methods;

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.graphql.internal.dao.TransactionCompleteResult;
import tech.pegasys.pantheon.ethereum.graphql.internal.dao.TransactionResult;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockWithMetadata;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.TransactionWithMetadata;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Block;
import tech.pegasys.pantheon.util.bytes.Bytes32;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

abstract class AbstractBlockFetcher implements GraphQLMethod {

  final BlockchainQueries blockchain;

  AbstractBlockFetcher(final BlockchainQueries blockchain) {
    this.blockchain = blockchain;
  }

  Block getBlockByHash(final Bytes32 hash) {
    try {
      return blockchain.blockByHashWithTxHashes(Hash.wrap(hash)).map(this::transactionComplete).orElse(null);
    } catch (NullPointerException e) {
      return null;
    }
  }

  Block getBlockByNumber(final Long number) {
    try {
      return blockchain.blockByNumberWithTxHashes(number).map(this::transactionComplete).orElse(null);
    } catch (NullPointerException e) {
      return null;
    }
  }

  Block getLatestBlock() {
    return blockchain.latestBlockWithTxHashes().map(this::transactionComplete).orElse(null);
  }

  Block transactionComplete(
      final BlockWithMetadata<Hash, Hash> blockWithMetadata) {
    return new Block(
        blockWithMetadata.getHeader(),
        blockWithMetadata.getTransactions().size(),
        blockWithMetadata.getOmmers().size(),
        blockWithMetadata.getTotalDifficulty());
  }
}
