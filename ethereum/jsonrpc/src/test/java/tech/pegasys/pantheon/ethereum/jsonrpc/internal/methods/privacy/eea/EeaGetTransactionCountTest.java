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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.enclave.Enclave;
import tech.pegasys.pantheon.enclave.types.PrivacyGroup;
import tech.pegasys.pantheon.enclave.types.PrivacyGroup.Type;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.privacy.PrivateTransactionHandler;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class EeaGetTransactionCountTest {
  private final Address address = Address.fromHexString("55");
  private Enclave enclave = mock(Enclave.class);
  private PrivateTransactionHandler privateTransactionHandler =
      mock(PrivateTransactionHandler.class);

  private JsonRpcRequest request;

  @Before
  public void setup() {
    final String[] privateFor = new String[] {"first", "second", "third"};
    final String privateFrom = "thePrivateFromKey";

    final Map<String, Object> jsonMap = new HashMap<>();
    jsonMap.put("address", address.toString());
    jsonMap.put("privateFrom", privateFrom);
    jsonMap.put("privateFor", privateFor);

    request = new JsonRpcRequest("2.0", "eea_getTransactionCount", new Object[] {jsonMap});
  }

  @Test
  public void validRequestProducesExpectedNonce() throws Exception {
    PrivacyGroup[] returnedGroups =
        new PrivacyGroup[] {
          new PrivacyGroup("Group1", Type.LEGACY, "Group1_Name", "Group1_Desc", new String[0]),
        };

    when(enclave.findPrivacyGroup(any())).thenReturn(returnedGroups);
    when(privateTransactionHandler.getSenderNonce(address, "Group1")).thenReturn(8L);

    final EeaGetTransactionCount method =
        new EeaGetTransactionCount(new JsonRpcParameter(), enclave, privateTransactionHandler);

    final JsonRpcResponse response = method.response(request);
    assertThat(response).isInstanceOf(JsonRpcSuccessResponse.class);

    final JsonRpcSuccessResponse successResponse = (JsonRpcSuccessResponse) response;
    assertThat(successResponse.getResult()).isEqualTo("0x8");
  }

  @Test
  public void moreThanOneMatchingLegacyGroupReturnsError() throws Exception {
    PrivacyGroup[] returnedGroups =
        new PrivacyGroup[] {
          new PrivacyGroup("Group1", Type.LEGACY, "Group1_Name", "Group1_Desc", new String[0]),
          new PrivacyGroup("Group2", Type.LEGACY, "Group2_Name", "Group2_Desc", new String[0]),
        };

    when(enclave.findPrivacyGroup(any())).thenReturn(returnedGroups);
    when(privateTransactionHandler.getSenderNonce(address, "Group1")).thenReturn(8L);

    final EeaGetTransactionCount method =
        new EeaGetTransactionCount(new JsonRpcParameter(), enclave, privateTransactionHandler);

    final JsonRpcResponse response = method.response(request);
    assertThat(response).isInstanceOf(JsonRpcErrorResponse.class);

    final JsonRpcErrorResponse errorResponse = (JsonRpcErrorResponse) response;
    assertThat(errorResponse.getError()).isEqualTo(JsonRpcError.FIND_PRIVACY_GROUP_ERROR);
  }

  @Test
  public void noMatchingLegacyGroupsReturnsError() throws Exception {
    PrivacyGroup[] returnedGroups = new PrivacyGroup[] {};

    when(enclave.findPrivacyGroup(any())).thenReturn(returnedGroups);

    final EeaGetTransactionCount method =
        new EeaGetTransactionCount(new JsonRpcParameter(), enclave, privateTransactionHandler);

    final JsonRpcResponse response = method.response(request);
    assertThat(response).isInstanceOf(JsonRpcErrorResponse.class);

    final JsonRpcErrorResponse errorResponse = (JsonRpcErrorResponse) response;
    assertThat(errorResponse.getError()).isEqualTo(JsonRpcError.FIND_PRIVACY_GROUP_ERROR);
  }

  @Test
  public void enclaveThrowingAnExceptionResultsInErrorResponse() throws Exception {
    when(enclave.findPrivacyGroup(any())).thenThrow(new Exception("Enclave Failed"));
    final EeaGetTransactionCount method =
        new EeaGetTransactionCount(new JsonRpcParameter(), enclave, privateTransactionHandler);

    final JsonRpcResponse response = method.response(request);
    assertThat(response).isInstanceOf(JsonRpcErrorResponse.class);

    final JsonRpcErrorResponse errorResponse = (JsonRpcErrorResponse) response;
    assertThat(errorResponse.getError()).isEqualTo(JsonRpcError.FIND_PRIVACY_GROUP_ERROR);
  }
}
