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

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.graphql.internal.dao.BlockResult;
import tech.pegasys.pantheon.ethereum.graphql.internal.dao.BlockResultFactory;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Block;
import tech.pegasys.pantheon.util.bytes.Bytes32;

import java.util.Optional;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class EthBlockMethod implements DataFetcher<Block>, GraphQLMethod {

  //    private static final Logger LOG = LogManager.getLogger();

  private final BlockResultFactory blockResult;
  private final BlockchainQueries blockchain;

  public EthBlockMethod(final BlockchainQueries blockchain, final BlockResultFactory blockResult) {
    this.blockchain = blockchain;
    this.blockResult = blockResult;
  }

  @Override
  public String getType() {
    return "block";
  }

  @Override
  public Block get(final DataFetchingEnvironment environment){

    if (environment.containsArgument("hash")) {
      Bytes32 hash = getHash(environment);
      return getBlockByHash(hash);
    } else if (environment.containsArgument("number")) {
      Long number = getNumber(environment);
      return getBlockByNumber(number);
    } else {
      return getLatestBlock();
    }
  }

  private Bytes32 getHash(final DataFetchingEnvironment environment) {

    return (Bytes32) Optional.of(environment.getArgument("hash")).orElse(null);
  }

  private Long getNumber(final DataFetchingEnvironment environment) {
    return (Long) Optional.of(environment.getArgument("number")).orElse(null);
  }

  private Block getBlockByHash(final Bytes32 hash) {
    try {
      BlockResult result =
              blockchain.blockByHash(Hash.wrap(hash)).map(blockResult::transactionComplete).orElse(null);
      return result == null ? null : Block.createFromBlockResult(result);
    } catch (NullPointerException e) {
      return null;
    }



  }

  private Block getBlockByNumber(final Long number) {
    try {
      BlockResult result =
              blockchain.blockByNumber(number).map(blockResult::transactionComplete).orElse(null);

      return result == null ? null : Block.createFromBlockResult(result);
    } catch (NullPointerException e) {
      return null;
    }
  }

  private Block getLatestBlock() {
    BlockResult result =
        blockchain.latestBlock().map(blockResult::transactionComplete).orElse(null);
    return result == null ? null : Block.createFromBlockResult(result);
  }
}
