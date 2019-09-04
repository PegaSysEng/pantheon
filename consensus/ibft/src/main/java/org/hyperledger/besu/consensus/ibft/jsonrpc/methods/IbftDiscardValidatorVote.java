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
package org.hyperledger.besu.consensus.ibft.jsonrpc.methods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.consensus.common.VoteProposer;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.jsonrpc.internal.JsonRpcRequest;
import org.hyperledger.besu.ethereum.jsonrpc.internal.methods.JsonRpcMethod;
import org.hyperledger.besu.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import org.hyperledger.besu.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import org.hyperledger.besu.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;

public class IbftDiscardValidatorVote implements JsonRpcMethod {
  private static final Logger LOG = LogManager.getLogger();
  private final VoteProposer voteProposer;
  private final JsonRpcParameter parameters;

  public IbftDiscardValidatorVote(
      final VoteProposer voteProposer, final JsonRpcParameter parameters) {
    this.voteProposer = voteProposer;
    this.parameters = parameters;
  }

  @Override
  public String getName() {
    return RpcMethod.IBFT_DISCARD_VALIDATOR_VOTE.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest req) {
    final Address validatorAddress = parameters.required(req.getParams(), 0, Address.class);
    LOG.trace("Received RPC rpcName={} address={}", getName(), validatorAddress);
    voteProposer.discard(validatorAddress);

    return new JsonRpcSuccessResponse(req.getId(), true);
  }
}
