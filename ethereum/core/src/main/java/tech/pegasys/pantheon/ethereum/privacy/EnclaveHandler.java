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
package tech.pegasys.pantheon.ethereum.privacy;

import tech.pegasys.pantheon.enclave.Enclave;
import tech.pegasys.pantheon.enclave.EnclaveException;
import tech.pegasys.pantheon.enclave.types.CreatePrivacyGroupRequest;
import tech.pegasys.pantheon.enclave.types.DeletePrivacyGroupRequest;
import tech.pegasys.pantheon.enclave.types.FindPrivacyGroupRequest;
import tech.pegasys.pantheon.enclave.types.PrivacyGroup;
import tech.pegasys.pantheon.enclave.types.ReceiveRequest;
import tech.pegasys.pantheon.enclave.types.ReceiveResponse;
import tech.pegasys.pantheon.enclave.types.SendRequest;
import tech.pegasys.pantheon.enclave.types.SendResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class EnclaveHandler {
  private Map<EnclaveUser, EnclaveUserDetails> enclaves = new HashMap<>();

  public EnclaveHandler(final Map<EnclaveUser, EnclaveUserDetails> enclaves) {
    this.enclaves.putAll(enclaves);
  }

  public SendResponse sendData(final EnclaveUser enclaveUser, final SendRequest sendRequest)
      throws Exception {
    final Enclave enclave = findEnclave(enclaveUser);
    return enclave.send(sendRequest);
  }

  public ReceiveResponse retrieveData(final ReceiveRequest receiveRequest) {
    final List<ReceiveResponse> receiveResponses =
        enclaves.values().stream()
            .map(
                enclaveUserDetails -> asyncReceive(enclaveUserDetails.getEnclave(), receiveRequest))
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    return receiveResponses.stream()
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow(
            () ->
                new EnclaveException(
                    "No enclave found containing data for key " + receiveRequest.getKey()));
  }

  public String retrievePrivacyGroupId(
      final EnclaveUser enclaveUser, final ReceiveRequest receiveRequest) throws Exception {
    final Enclave enclave = findEnclave(enclaveUser);
    return enclave.receive(receiveRequest).getPrivacyGroupId();
  }

  public PrivacyGroup createPrivacyGroup(
      final EnclaveUser enclaveUser, final CreatePrivacyGroupRequest createPrivacyGroupRequest)
      throws Exception {
    final Enclave enclave = findEnclave(enclaveUser);
    return enclave.createPrivacyGroup(createPrivacyGroupRequest);
  }

  public String deletePrivacyGroup(
      final EnclaveUser enclaveUser, final DeletePrivacyGroupRequest deletePrivacyGroupRequest)
      throws Exception {
    final Enclave enclave = findEnclave(enclaveUser);
    return enclave.deletePrivacyGroup(deletePrivacyGroupRequest);
  }

  public PrivacyGroup[] findPrivacyGroup(
      final EnclaveUser enclaveUser, final FindPrivacyGroupRequest findPrivacyGroupRequest)
      throws Exception {
    final Enclave enclave = findEnclave(enclaveUser);
    return enclave.findPrivacyGroup(findPrivacyGroupRequest);
  }

  private Enclave findEnclave(final EnclaveUser enclaveUser) {
    final EnclaveUserDetails enclaveUserDetails = enclaves.get(enclaveUser);
    if (enclaveUserDetails == null) {
      throw new EnclaveException("Unknown enclave user " + enclaveUser);
    }
    return enclaveUserDetails.getEnclave();
  }

  private CompletableFuture<ReceiveResponse> asyncReceive(
      final Enclave enclave, final ReceiveRequest receiveRequest) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return enclave.receive(receiveRequest);
          } catch (Exception e) {
            return null;
          }
        });
  }
}
