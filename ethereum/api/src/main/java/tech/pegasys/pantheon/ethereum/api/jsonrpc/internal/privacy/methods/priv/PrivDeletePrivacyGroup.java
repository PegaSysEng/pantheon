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
package tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.privacy.methods.priv;

import static org.apache.logging.log4j.LogManager.getLogger;

import tech.pegasys.pantheon.enclave.Enclave;
import tech.pegasys.pantheon.enclave.types.DeletePrivacyGroupRequest;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.RpcMethod;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;

import org.apache.logging.log4j.Logger;

public class PrivDeletePrivacyGroup implements JsonRpcMethod {

  private static final Logger LOG = getLogger();
  private final Enclave enclave;
  private PrivacyParameters privacyParameters;
  private final JsonRpcParameter parameters;

  public PrivDeletePrivacyGroup(
      final Enclave enclave,
      final PrivacyParameters privacyParameters,
      final JsonRpcParameter parameters) {
    this.enclave = enclave;
    this.privacyParameters = privacyParameters;
    this.parameters = parameters;
  }

  @Override
  public String getName() {
    return RpcMethod.PRIV_DELETE_PRIVACY_GROUP.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    LOG.trace("Executing {}", RpcMethod.PRIV_DELETE_PRIVACY_GROUP.getMethodName());

    final String privacyGroupId = parameters.required(request.getParams(), 0, String.class);

    LOG.trace(
        "Deleting a privacy group with privacyGroupId {} and from {}",
        privacyGroupId,
        privacyParameters.getEnclavePublicKey());

    DeletePrivacyGroupRequest deletePrivacyGroupRequest =
        new DeletePrivacyGroupRequest(privacyGroupId, privacyParameters.getEnclavePublicKey());
    String response;
    try {
      response = enclave.deletePrivacyGroup(deletePrivacyGroupRequest);
    } catch (Exception e) {
      LOG.error("Failed to fetch transaction from Enclave with error " + e.getMessage());
      LOG.error(e);
      return new JsonRpcSuccessResponse(request.getId(), JsonRpcError.DELETE_PRIVACY_GROUP_ERROR);
    }
    return new JsonRpcSuccessResponse(request.getId(), response);
  }
}
