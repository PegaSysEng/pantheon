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
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.privacy.eea;

import static org.apache.logging.log4j.LogManager.getLogger;

import tech.pegasys.pantheon.enclave.Enclave;
import tech.pegasys.pantheon.enclave.types.FindPrivacyGroupRequest;
import tech.pegasys.pantheon.enclave.types.PrivacyGroup;
import tech.pegasys.pantheon.enclave.types.PrivacyGroup.Type;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.jsonrpc.RpcMethod;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethod;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.results.Quantity;
import tech.pegasys.pantheon.ethereum.privacy.PrivateTransactionHandler;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.Arrays;

public class EeaGetTransactionCount implements JsonRpcMethod {

  public static class EeaGetTransactionCountJsonBody {

    final String privateFrom;
    final String[] privateFor;
    final Address address;

    @JsonCreator
    public EeaGetTransactionCountJsonBody(
        @JsonProperty("privateFrom") final String privateFrom,
        @JsonProperty("privateFor") final String[] privateFor,
        @JsonProperty("address") final Address address) {
      this.privateFrom = privateFrom;
      this.privateFor = privateFor;
      this.address = address;
    }

    public String getPrivateFrom() {
      return privateFrom;
    }

    public String[] getPrivateFor() {
      return privateFor;
    }

    public Address getAddress() {
      return address;
    }
  }

  private static final Logger LOG = getLogger();

  private final JsonRpcParameter parameters;
  private final Enclave enclave;
  private final PrivateTransactionHandler privateTransactionHandler;

  public EeaGetTransactionCount(
      final JsonRpcParameter parameters,
      final Enclave enclave,
      final PrivateTransactionHandler privateTransactionHandler) {
    this.parameters = parameters;
    this.enclave = enclave;
    this.privateTransactionHandler = privateTransactionHandler;
  }

  @Override
  public String getName() {
    return RpcMethod.PRIV_GET_LEGACY_TRANSACTION_COUNT.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    if (request.getParamLength() != 1) {
      return new JsonRpcErrorResponse(request.getId(), JsonRpcError.INVALID_PARAMS);
    }
    final EeaGetTransactionCountJsonBody eeaGetTransactionCount =
        parameters.required(request.getParams(), 0, EeaGetTransactionCountJsonBody.class);

    try {
      final long nonce = determineNonce(eeaGetTransactionCount);
      return new JsonRpcSuccessResponse(request.getId(), Quantity.create(nonce));
    } catch (final Exception e) {
      LOG.error("Failed to fetch group from Enclave with error " + e.getMessage());
      LOG.error(e);
      return new JsonRpcErrorResponse(request.getId(), JsonRpcError.FIND_PRIVACY_GROUP_ERROR);
    }
  }

  private long determineNonce(final EeaGetTransactionCountJsonBody getTransactionCount)
      throws Exception {

    final String[] groupMembers =
        Arrays.append(getTransactionCount.getPrivateFor(), getTransactionCount.getPrivateFrom());

    final FindPrivacyGroupRequest request = new FindPrivacyGroupRequest(groupMembers);
    final List<PrivacyGroup> matchingGroups = Lists.newArrayList(enclave.findPrivacyGroup(request));

    final List<PrivacyGroup> legacyGroups =
        matchingGroups.stream()
            .filter(group -> group.getType() == Type.LEGACY)
            .collect(Collectors.toList());

    if (legacyGroups.size() != 1) {
      final String errorMessage =
          String.format(
              "Found invalid number of privacy groups (%d), expected 1.", legacyGroups.size());
      LOG.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }

    final String privacyGroupId = legacyGroups.get(0).getPrivacyGroupId();

    return privateTransactionHandler.getSenderNonce(
        getTransactionCount.getAddress(), privacyGroupId);
  }
}
