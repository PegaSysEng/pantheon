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
import tech.pegasys.pantheon.util.FutureUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class EnclaveHandler {
  private Map<EnclaveUser, Enclave> enclaves = new HashMap<>();

  public EnclaveHandler(final Map<EnclaveUser, Enclave> enclaves) {
    this.enclaves.putAll(enclaves);
  }

  public SendResponse sendData(final EnclaveUser enclaveUser, final SendRequest sendRequest)
      throws Exception {
    return enclave(enclaveUser).send(sendRequest);
  }

  public ReceiveResponse retrieveData(final ReceiveRequest receiveRequest) {
    final List<CompletableFuture<ReceiveResponse>> retrieveFutures =
        enclaves.values().stream()
            .map(enclave -> asyncReceive(enclave, receiveRequest))
            .collect(Collectors.toList());
    return FutureUtils.anyOfMatching(retrieveFutures, Objects::nonNull).join();
  }

  public String retrievePrivacyGroupId(
      final EnclaveUser enclaveUser, final ReceiveRequest receiveRequest) throws Exception {
    return enclave(enclaveUser).receive(receiveRequest).getPrivacyGroupId();
  }

  public PrivacyGroup createPrivacyGroup(
      final EnclaveUser enclaveUser, final CreatePrivacyGroupRequest createPrivacyGroupRequest)
      throws Exception {
    return enclave(enclaveUser).createPrivacyGroup(createPrivacyGroupRequest);
  }

  public String deletePrivacyGroup(
      final EnclaveUser enclaveUser, final DeletePrivacyGroupRequest deletePrivacyGroupRequest)
      throws Exception {
    return enclave(enclaveUser).deletePrivacyGroup(deletePrivacyGroupRequest);
  }

  public PrivacyGroup[] findPrivacyGroup(
      final EnclaveUser enclaveUser, final FindPrivacyGroupRequest findPrivacyGroupRequest)
      throws Exception {
    return enclave(enclaveUser).findPrivacyGroup(findPrivacyGroupRequest);
  }

  private Enclave enclave(final EnclaveUser enclaveUser) {
    final Enclave enclave = enclaves.get(enclaveUser);
    if (enclave == null) {
      throw new EnclaveException("Unknown enclave user " + enclaveUser);
    }
    return enclave;
  }

  private CompletableFuture<ReceiveResponse> asyncReceive(
      final Enclave enclave, final ReceiveRequest receiveRequest) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return enclave.receive(receiveRequest);
          } catch (final Exception e) {
            return null;
          }
        });
  }
}
