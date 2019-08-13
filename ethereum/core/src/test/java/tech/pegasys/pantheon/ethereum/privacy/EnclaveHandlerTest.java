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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.enclave.Enclave;
import tech.pegasys.pantheon.enclave.EnclaveException;
import tech.pegasys.pantheon.enclave.types.CreatePrivacyGroupRequest;
import tech.pegasys.pantheon.enclave.types.DeletePrivacyGroupRequest;
import tech.pegasys.pantheon.enclave.types.FindPrivacyGroupRequest;
import tech.pegasys.pantheon.enclave.types.PrivacyGroup;
import tech.pegasys.pantheon.enclave.types.ReceiveRequest;
import tech.pegasys.pantheon.enclave.types.ReceiveResponse;
import tech.pegasys.pantheon.enclave.types.SendRequest;
import tech.pegasys.pantheon.enclave.types.SendRequestPantheon;
import tech.pegasys.pantheon.enclave.types.SendResponse;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnclaveHandlerTest {
  @Mock private EnclaveUser enclaveUser1;
  @Mock private EnclaveUser enclaveUser2;
  @Mock private EnclaveUser enclaveUser3;
  @Mock private EnclaveUser unknownEnclaveUser;
  @Mock private Enclave enclave1;
  @Mock private Enclave enclave2;
  @Mock private Enclave enclave3;
  private EnclaveHandler enclaveHandler;

  @Before
  public void setup() {
    final Map<EnclaveUser, Enclave> enclaves =
        ImmutableMap.of(enclaveUser1, enclave1, enclaveUser2, enclave2, enclaveUser3, enclave3);
    enclaveHandler = new EnclaveHandler(enclaves);
  }

  @Test
  public void sendsDataToUsersOrion() throws Exception {
    final SendRequest sendRequest = new SendRequestPantheon("payload", "from", "privacyGroup");
    when(enclave1.send(sendRequest)).thenReturn(new SendResponse("abc123"));

    assertThat(enclaveHandler.sendData(enclaveUser1, sendRequest))
        .isEqualToComparingFieldByField(new SendResponse("abc123"));
    verify(enclave1).send(sendRequest);
    verifyZeroInteractions(enclave2);
  }

  @Test
  public void sendDataShouldFailIfNoEnclaveUserConfiguredForAnEnclave() {
    final SendRequest sendRequest = new SendRequestPantheon("payload", "from", "privacyGroup");
    assertThatThrownBy(() -> enclaveHandler.sendData(unknownEnclaveUser, sendRequest))
        .isInstanceOf(EnclaveException.class);
    verifyZeroInteractions(enclave1, enclave2, enclave3);
  }

  @Test
  public void retrievesDataForEnclaveUser() throws Exception {
    final ReceiveRequest receiveRequest = new ReceiveRequest("abc123", "user2PublicKey");

    when(enclave1.receive(any(ReceiveRequest.class))).thenThrow(EnclaveException.class);
    when(enclave2.receive(any(ReceiveRequest.class))).thenThrow(EnclaveException.class);
    when(enclave3.receive(receiveRequest)).thenReturn(new ReceiveResponse(null, "privacyGroup"));

    assertThat(enclaveHandler.retrieveData(receiveRequest).getPrivacyGroupId())
        .isEqualTo("privacyGroup");
    verify(enclave1).receive(any());
    verify(enclave2).receive(any());
    verify(enclave3).receive(any());
  }

  @Test
  public void retrievesDataEvenWhenSomeEnclavesFail() throws Exception {
    when(enclave1.receive(any())).thenThrow(IOException.class);
    when(enclave2.receive(any(ReceiveRequest.class)))
        .thenReturn(new ReceiveResponse(null, "privacyGroup"));
    when(enclave3.receive(any(ReceiveRequest.class)))
        .thenReturn(new ReceiveResponse(null, "privacyGroup"));

    final ReceiveResponse receiveResponse =
        enclaveHandler.retrieveData(new ReceiveRequest("abc123", "user2PublicKey"));
    assertThat(receiveResponse.getPrivacyGroupId()).isEqualTo("privacyGroup");
    verify(enclave1).receive(any());
    verify(enclave2).receive(any());
    verify(enclave3).receive(any());
  }

  @Test
  public void retrieveDataShouldFailWhenNoEnclaveContainsData() throws Exception {
    when(enclave1.receive(any())).thenThrow(EnclaveException.class);
    when(enclave2.receive(any())).thenThrow(EnclaveException.class);
    when(enclave3.receive(any())).thenThrow(IOException.class);

    assertThatThrownBy(
            () -> enclaveHandler.retrieveData(new ReceiveRequest("abc123", "user2PublicKey")))
        .isInstanceOf(EnclaveException.class);
    verify(enclave1).receive(any());
    verify(enclave2).receive(any());
    verify(enclave3).receive(any());
  }

  @Test
  public void retrievesPrivacyGroupIdOnlyForUsersEnclave() throws Exception {
    final ReceiveRequest receiveRequest = new ReceiveRequest("abc123", "user2PublicKey");

    when(enclave1.receive(receiveRequest)).thenReturn(new ReceiveResponse(null, "privacyGroup"));

    assertThat(enclaveHandler.retrievePrivacyGroupId(enclaveUser1, receiveRequest))
        .isEqualTo("privacyGroup");
    verify(enclave1).receive(receiveRequest);
    verifyZeroInteractions(enclave2, enclave3);
  }

  @Test
  public void retrievesPrivacyGroupShouldFailIfNoEnclaveUserConfiguredForAnEnclave() {
    final ReceiveRequest receiveRequest = new ReceiveRequest("abc123", "user2PublicKey");

    assertThatThrownBy(
            () -> enclaveHandler.retrievePrivacyGroupId(unknownEnclaveUser, receiveRequest))
        .isInstanceOf(EnclaveException.class);
    verifyZeroInteractions(enclave1, enclave2, enclave3);
  }

  @Test
  public void createPrivacyGroupIdOnlyForUsersEnclave() throws Exception {
    final CreatePrivacyGroupRequest createPrivacyGroupRequest =
        new CreatePrivacyGroupRequest(
            new String[] {"enclave2"}, "enclave1", "privacyGroup", "privacy group");
    final PrivacyGroup privacyGroup = new PrivacyGroup();

    when(enclave1.createPrivacyGroup(createPrivacyGroupRequest)).thenReturn(privacyGroup);
    assertThat(enclaveHandler.createPrivacyGroup(enclaveUser1, createPrivacyGroupRequest))
        .isEqualTo(privacyGroup);
    verify(enclave1).createPrivacyGroup(createPrivacyGroupRequest);
    verifyZeroInteractions(enclave2, enclave3);
  }

  @Test
  public void createPrivacyGroupIdShouldFailIfNoEnclaveUserConfiguredForAnEnclave() {
    final CreatePrivacyGroupRequest createPrivacyGroupRequest =
        new CreatePrivacyGroupRequest(
            new String[] {"enclave2"}, "enclave1", "privacyGroup", "privacy group");

    assertThatThrownBy(
            () -> enclaveHandler.createPrivacyGroup(unknownEnclaveUser, createPrivacyGroupRequest))
        .isInstanceOf(EnclaveException.class);
    verifyZeroInteractions(enclave1, enclave2, enclave3);
  }

  @Test
  public void deletePrivacyGroupIdOnlyForUsersEnclave() throws Exception {
    final DeletePrivacyGroupRequest deletePrivacyGroupRequest =
        new DeletePrivacyGroupRequest("abc123", "user1PublicKey");

    when(enclave1.deletePrivacyGroup(deletePrivacyGroupRequest)).thenReturn("privacyGroup");

    assertThat(enclaveHandler.deletePrivacyGroup(enclaveUser1, deletePrivacyGroupRequest))
        .isEqualTo("privacyGroup");
    verify(enclave1).deletePrivacyGroup(deletePrivacyGroupRequest);
    verifyZeroInteractions(enclave2, enclave3);
  }

  @Test
  public void deletePrivacyGroupIdShouldFailIfNoEnclaveUserConfiguredForAnEnclave() {
    final DeletePrivacyGroupRequest deletePrivacyGroupRequest =
        new DeletePrivacyGroupRequest("abc123", "user1PublicKey");
    assertThatThrownBy(
            () -> enclaveHandler.deletePrivacyGroup(unknownEnclaveUser, deletePrivacyGroupRequest))
        .isInstanceOf(EnclaveException.class);
    verifyZeroInteractions(enclave1, enclave2, enclave3);
  }

  @Test
  public void findPrivacyGroupIdOnlyForUsersEnclave() throws Exception {
    final FindPrivacyGroupRequest findPrivacyGroupRequest =
        new FindPrivacyGroupRequest(new String[] {"user2PublicKey"});
    final PrivacyGroup privacyGroup = new PrivacyGroup();

    when(enclave1.findPrivacyGroup(findPrivacyGroupRequest))
        .thenReturn(new PrivacyGroup[] {privacyGroup});

    assertThat(enclaveHandler.findPrivacyGroup(enclaveUser1, findPrivacyGroupRequest))
        .isEqualTo(new PrivacyGroup[] {privacyGroup});
    verify(enclave1).findPrivacyGroup(findPrivacyGroupRequest);
    verifyZeroInteractions(enclave2, enclave3);
  }

  @Test
  public void findPrivacyGroupIdShouldFailIfNoEnclaveUserConfiguredForAnEnclave() throws Exception {
    final FindPrivacyGroupRequest findPrivacyGroupRequest =
        new FindPrivacyGroupRequest(new String[] {"user2PublicKey"});
    assertThatThrownBy(
            () -> enclaveHandler.findPrivacyGroup(unknownEnclaveUser, findPrivacyGroupRequest))
        .isInstanceOf(EnclaveException.class);
    verifyZeroInteractions(enclave1, enclave2, enclave3);
  }
}
