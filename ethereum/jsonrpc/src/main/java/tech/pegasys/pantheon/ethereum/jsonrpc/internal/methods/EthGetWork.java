package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods;

import java.util.Optional;
import javax.xml.bind.DatatypeConverter;
import tech.pegasys.pantheon.ethereum.blockcreation.AbstractMiningCoordinator;
import tech.pegasys.pantheon.ethereum.blockcreation.EthHashMiningCoordinator;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.mainnet.EthHashSolverInputs;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;

public class EthGetWork implements JsonRpcMethod {

  private final AbstractMiningCoordinator miner;
  private final BlockchainQueries blockchain;

  public EthGetWork(AbstractMiningCoordinator miner, BlockchainQueries blockchain) {
    this.miner = miner;
    this.blockchain = blockchain;
  }

  @Override
  public String getName() {
    return "eth_getWork";
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest req) {
    Optional<EthHashSolverInputs> solver = miner.getWorkDefinition();
    if (solver.isPresent()) {
      EthHashSolverInputs rawResult = solver.get();
      String seedHashString = "0x0000000000000000000000000000000000000000000000000000000000000000";
      if (rawResult.getDagSeed() != 0) {
        seedHashString = blockchain.getBlockHashByNumber(rawResult.getDagSeed()).get().toString();
      }
      String [] result = {
          "0x" + DatatypeConverter.printHexBinary(rawResult.getPrePowHash()).toLowerCase(),
          seedHashString,
          rawResult.getTarget().toHexString()
      };
      return new JsonRpcSuccessResponse(req.getId(), result);
    } else {
      return new JsonRpcErrorResponse(req.getId(), JsonRpcError.INTERNAL_ERROR);
    }
  }
}



