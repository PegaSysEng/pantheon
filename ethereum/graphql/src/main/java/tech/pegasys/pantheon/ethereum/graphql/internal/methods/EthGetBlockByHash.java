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
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.BlockResult;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.BlockResultFactory;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLQuery;

public class EthGetBlockByHash implements GraphQLMethod {

  //  private static final Logger LOG = getLogger();

  private final BlockResultFactory blockResult;
  private final BlockchainQueries blockchain;

  public EthGetBlockByHash(
      final BlockchainQueries blockchain, final BlockResultFactory blockResult) {
    this.blockchain = blockchain;
    this.blockResult = blockResult;
  }

  @Override
  public String getType() {
    return "eth_getBlockByHash";
  }

  //  @Override
  //  public GraphQLResponse response(final GraphQLRequest request) {
  //    return new GraphQLSuccessResponse(transactionHash(request));
  //  }

  //  private BlockResult blockResult(final JsonRpcRequest request) {
  //    final Hash hash = parameters.required(request.getParams(), 0, Hash.class);
  //
  //    if (isCompleteTransactions(request)) {
  //      return transactionComplete(hash);
  //    }
  //
  //    return transactionHash(hash);
  //  }

  @GraphQLQuery(
      name = "block",
      description =
          "Block fetches an Ethereum block by number or by hash. "
              + "If neither is supplied, the most recent known block is returned.")
  public BlockResult response(@GraphQLArgument(name = "hash") final String hash) {
    return blockchain
        .blockByHashWithTxHashes(Hash.fromHexString(hash))
        .map(tx -> blockResult.transactionHash(tx))
        .orElse(null);
  }
}
