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
package tech.pegasys.pantheon.ethereum.graphql.internal.methods;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.graphql.internal.dao.TransactionCompleteResult;
import tech.pegasys.pantheon.ethereum.graphql.internal.dao.TransactionResult;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockWithMetadata;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.TransactionWithMetadata;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Block;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.List;
import java.util.stream.Collectors;

public class BlockParentFetcher implements DataFetcher<Block>, GraphQLMethod {

  //    private static final Logger LOG = LogManager.getLogger();

  private final BlockchainQueries blockchain;

  public BlockParentFetcher(final BlockchainQueries blockchain) {
    this.blockchain = blockchain;
  }

  @Override
  public String getType() {
    return "parent";
  }

  @Override
  public Block get(final DataFetchingEnvironment environment) {
    final long childNumber = ((Block) environment.getSource()).getNumber().longValue();
    return childNumber == 0 ? null : getParentByNumber(childNumber - 1);
  }
    private Block getParentByNumber(final Long number) {
      try {
        return blockchain.blockByNumber(number).map(this::transactionComplete).orElse(null);
      } catch (NullPointerException e) {
        return null;
      }
    }

    private Block transactionComplete(
            final BlockWithMetadata<TransactionWithMetadata, Hash> blockWithMetadata) {
      final List<TransactionResult> txs =
              blockWithMetadata.getTransactions().stream()
                      .map(TransactionCompleteResult::new)
                      .collect(Collectors.toList());
      final List<JsonNode> ommers =
              blockWithMetadata.getOmmers().stream()
                      .map(Hash::toString)
                      .map(TextNode::new)
                      .collect(Collectors.toList());
      return new Block(
              blockWithMetadata.getHeader(),
              txs,
              ommers,
              blockWithMetadata.getTotalDifficulty(),
              blockWithMetadata.getSize());
    }
}
