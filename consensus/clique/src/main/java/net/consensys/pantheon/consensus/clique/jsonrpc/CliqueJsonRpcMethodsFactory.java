package net.consensys.pantheon.consensus.clique.jsonrpc;

import net.consensys.pantheon.consensus.clique.CliqueContext;
import net.consensys.pantheon.consensus.clique.jsonrpc.methods.CliqueGetSigners;
import net.consensys.pantheon.consensus.clique.jsonrpc.methods.CliqueGetSignersAtHash;
import net.consensys.pantheon.consensus.clique.jsonrpc.methods.Discard;
import net.consensys.pantheon.consensus.clique.jsonrpc.methods.Propose;
import net.consensys.pantheon.consensus.common.EpochManager;
import net.consensys.pantheon.consensus.common.VoteProposer;
import net.consensys.pantheon.ethereum.ProtocolContext;
import net.consensys.pantheon.ethereum.chain.MutableBlockchain;
import net.consensys.pantheon.ethereum.db.WorldStateArchive;
import net.consensys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethod;
import net.consensys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import net.consensys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;

import java.util.HashMap;
import java.util.Map;

public class CliqueJsonRpcMethodsFactory {

  public Map<String, JsonRpcMethod> methods(final ProtocolContext<CliqueContext> context) {
    final MutableBlockchain blockchain = context.getBlockchain();
    final WorldStateArchive worldStateArchive = context.getWorldStateArchive();
    final BlockchainQueries blockchainQueries =
        new BlockchainQueries(blockchain, worldStateArchive);
    final JsonRpcParameter jsonRpcParameter = new JsonRpcParameter();
    final VoteProposer voteProposer = context.getConsensusState().getVoteProposer();
    final EpochManager epochManager = context.getConsensusState().getEpochManager();

    final CliqueGetSigners cliqueGetSigners =
        new CliqueGetSigners(blockchainQueries, epochManager, jsonRpcParameter);
    final CliqueGetSignersAtHash cliqueGetSignersAtHash =
        new CliqueGetSignersAtHash(blockchainQueries, epochManager, jsonRpcParameter);
    final Propose proposeRpc = new Propose(voteProposer, jsonRpcParameter);
    final Discard discardRpc = new Discard(voteProposer, jsonRpcParameter);

    final Map<String, JsonRpcMethod> rpcMethods = new HashMap<>();
    rpcMethods.put(cliqueGetSigners.getName(), cliqueGetSigners);
    rpcMethods.put(cliqueGetSignersAtHash.getName(), cliqueGetSignersAtHash);
    rpcMethods.put(proposeRpc.getName(), proposeRpc);
    rpcMethods.put(discardRpc.getName(), discardRpc);
    return rpcMethods;
  }
}
