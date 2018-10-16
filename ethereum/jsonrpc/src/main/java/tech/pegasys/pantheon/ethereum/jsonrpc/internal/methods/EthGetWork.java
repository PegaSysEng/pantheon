package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods;

import static org.apache.logging.log4j.LogManager.getLogger;

import tech.pegasys.pantheon.ethereum.blockcreation.AbstractMiningCoordinator;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.mainnet.DirectAcyclicGraphSeed;
import tech.pegasys.pantheon.ethereum.mainnet.EthHashSolverInputs;

import java.util.Optional;
import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.Logger;

public class EthGetWork implements JsonRpcMethod {

  private final AbstractMiningCoordinator miner;
  private final BlockchainQueries blockchain;
  private static final Logger LOG = getLogger();

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
      byte[] dagSeed = DirectAcyclicGraphSeed.dagSeed(rawResult.getBlockNumber());
      String[] result = {
        "0x" + DatatypeConverter.printHexBinary(rawResult.getPrePowHash()).toLowerCase(),
        "0x" + DatatypeConverter.printHexBinary(dagSeed).toLowerCase(),
        rawResult.getTarget().toHexString()
      };
      return new JsonRpcSuccessResponse(req.getId(), result);
    } else {
      return new JsonRpcErrorResponse(req.getId(), JsonRpcError.NO_MINING_WORK_FOUND);
    }
  }
}
