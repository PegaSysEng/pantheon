package net.consensys.pantheon.consensus.clique.jsonrpc.methods;

import static net.consensys.pantheon.consensus.clique.CliqueHelpers.getValidatorsForLastEpochBlock;

import net.consensys.pantheon.consensus.common.EpochManager;
import net.consensys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import net.consensys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethod;
import net.consensys.pantheon.ethereum.jsonrpc.internal.parameters.BlockParameter;
import net.consensys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import net.consensys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CliqueGetSigners implements JsonRpcMethod {
  public static final String CLIQUE_GET_SIGNERS = "clique_getSigners";
  private final BlockchainQueries blockchainQueries;
  private final EpochManager epochManager;
  private final JsonRpcParameter parameters;

  public CliqueGetSigners(
      final BlockchainQueries blockchainQueries,
      final EpochManager epochManager,
      final JsonRpcParameter parameter) {
    this.blockchainQueries = blockchainQueries;
    this.epochManager = epochManager;
    this.parameters = parameter;
  }

  @Override
  public String getName() {
    return CLIQUE_GET_SIGNERS;
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    final long blockNumber = blockNumber(request);
    return getValidatorsForLastEpochBlock(blockNumber, epochManager, blockchainQueries)
        .map(addresses -> addresses.stream().map(Objects::toString).collect(Collectors.toList()))
        .<JsonRpcResponse>map(addresses -> new JsonRpcSuccessResponse(request.getId(), addresses))
        .orElse(new JsonRpcErrorResponse(request.getId(), JsonRpcError.INTERNAL_ERROR));
  }

  private long blockNumber(final JsonRpcRequest request) {
    final Optional<BlockParameter> blockParameter =
        parameters.optional(request.getParams(), 0, BlockParameter.class);
    final long latest = blockchainQueries.headBlockNumber();
    return blockParameter.map(b -> b.getNumber().orElse(latest)).orElse(latest);
  }
}
