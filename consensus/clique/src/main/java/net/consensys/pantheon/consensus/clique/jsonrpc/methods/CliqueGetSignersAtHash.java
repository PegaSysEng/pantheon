package net.consensys.pantheon.consensus.clique.jsonrpc.methods;

import net.consensys.pantheon.consensus.clique.VoteTallyCache;
import net.consensys.pantheon.ethereum.core.BlockHeader;
import net.consensys.pantheon.ethereum.core.Hash;
import net.consensys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import net.consensys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethod;
import net.consensys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import net.consensys.pantheon.ethereum.jsonrpc.internal.queries.BlockWithMetadata;
import net.consensys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CliqueGetSignersAtHash implements JsonRpcMethod {
  public static final String CLIQUE_GET_SIGNERS_AT_HASH = "clique_getSignersAtHash";
  private final BlockchainQueries blockchainQueries;
  private final VoteTallyCache voteTallyCache;
  private final JsonRpcParameter parameters;

  public CliqueGetSignersAtHash(
      final BlockchainQueries blockchainQueries,
      final VoteTallyCache voteTallyCache,
      final JsonRpcParameter parameter) {
    this.blockchainQueries = blockchainQueries;
    this.voteTallyCache = voteTallyCache;
    this.parameters = parameter;
  }

  @Override
  public String getName() {
    return CLIQUE_GET_SIGNERS_AT_HASH;
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    final Optional<BlockHeader> blockHeader = blockNumber(request);
    return blockHeader
        .map(bh -> voteTallyCache.getVoteTallyAtBlock(bh).getCurrentValidators())
        .map(addresses -> addresses.stream().map(Objects::toString).collect(Collectors.toList()))
        .<JsonRpcResponse>map(addresses -> new JsonRpcSuccessResponse(request.getId(), addresses))
        .orElse(new JsonRpcErrorResponse(request.getId(), JsonRpcError.INTERNAL_ERROR));
  }

  private Optional<BlockHeader> blockNumber(final JsonRpcRequest request) {
    final Hash hash = parameters.required(request.getParams(), 0, Hash.class);
    return blockchainQueries.blockByHash(hash).map(BlockWithMetadata::getHeader);
  }
}
