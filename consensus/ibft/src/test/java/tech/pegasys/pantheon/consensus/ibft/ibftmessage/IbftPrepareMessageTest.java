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
package tech.pegasys.pantheon.consensus.ibft.ibftmessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftSignedMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftUnsignedPrepareMessageData;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IbftPrepareMessageTest {
  @Mock private IbftSignedMessageData<IbftUnsignedPrepareMessageData> prepareMessageData;
  @Mock private BytesValue messageBytes;
  @Mock private MessageData messageData;
  @Mock private IbftPrepareMessage prepareMessage;
  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void createMessageFromPrepareMessageData() {
    when(prepareMessageData.encode()).thenReturn(messageBytes);
    IbftPrepareMessage prepareMessage = IbftPrepareMessage.create(prepareMessageData);

    assertThat(prepareMessage.getData()).isEqualTo(messageBytes);
    assertThat(prepareMessage.getCode()).isEqualTo(IbftV2.PREPARE);
    verify(prepareMessageData).encode();
  }

  @Test
  public void createMessageFromPrepareMessage() {
    IbftPrepareMessage message = IbftPrepareMessage.fromMessage(prepareMessage);
    assertThat(message).isEqualTo(prepareMessage);
  }

  @Test
  public void createMessageFromGenericMessageData() {
    when(messageData.getData()).thenReturn(messageBytes);
    when(messageData.getCode()).thenReturn(IbftV2.PREPARE);
    IbftPrepareMessage prepareMessage = IbftPrepareMessage.fromMessage(messageData);

    assertThat(prepareMessage.getData()).isEqualTo(messageData.getData());
    assertThat(prepareMessage.getCode()).isEqualTo(IbftV2.PREPARE);
  }

  @Test
  public void createMessageFailsWhenIncorrectMessageCode() {
    when(messageData.getCode()).thenReturn(42);
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Message has code 42 and thus is not a IbftPrepareMessage");
    IbftPrepareMessage.fromMessage(messageData);
  }
}
