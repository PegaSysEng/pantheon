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
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockWithMetadata;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Block;
import tech.pegasys.pantheon.util.bytes.Bytes32;

abstract class AbstractBlockFetcher implements GraphQLMethod {

  final BlockchainQueries blockchain;

  AbstractBlockFetcher(final BlockchainQueries blockchain) {
    this.blockchain = blockchain;
  }

  Block getBlockByHash(final Bytes32 hash) {
    try {
      return blockchain
          .blockByHashWithTxHashes(Hash.wrap(hash))
          .map(this::blockWithMetadataToBlock)
          .orElse(null);
    } catch (NullPointerException e) {
      return null;
    }
  }

  Block getBlockByNumber(final Long number) {
    try {
      return blockchain
          .blockByNumberWithTxHashes(number)
          .map(this::blockWithMetadataToBlock)
          .orElse(null);
    } catch (NullPointerException e) {
      return null;
    }
  }

  Block getLatestBlock() {
    return blockchain.latestBlockWithTxHashes().map(this::blockWithMetadataToBlock).orElse(null);
  }

  Block blockWithMetadataToBlock(final BlockWithMetadata<Hash, Hash> blockWithMetadata) {
    return new Block(
        blockWithMetadata.getHeader(),
        blockWithMetadata.getTransactions().size(),
        blockWithMetadata.getOmmers().size(),
        blockWithMetadata.getTotalDifficulty());
  }
}
