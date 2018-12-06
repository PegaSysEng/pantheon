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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftSignedMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftUnsignedPrePrepareMessageData;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IbftPrePrepareMessageTest {
  @Mock private IbftSignedMessageData<IbftUnsignedPrePrepareMessageData> prePrepareMessageData;
  @Mock private BytesValue messageBytes;
  @Mock private MessageData messageData;
  @Mock private IbftPrePrepareMessage prePrepareMessage;

  @Test
  public void createMessageFromPrePrepareMessageData() {
    when(prePrepareMessageData.encode()).thenReturn(messageBytes);
    IbftPrePrepareMessage prepareMessage = IbftPrePrepareMessage.create(prePrepareMessageData);

    assertThat(prepareMessage.getData()).isEqualTo(messageBytes);
    assertThat(prepareMessage.getCode()).isEqualTo(IbftV2.PRE_PREPARE);
    verify(prePrepareMessageData).encode();
  }

  @Test
  public void createMessageFromPrePrepareMessage() {
    IbftPrePrepareMessage message = IbftPrePrepareMessage.fromMessage(prePrepareMessage);
    assertThat(message).isSameAs(prePrepareMessage);
  }

  @Test
  public void createMessageFromGenericMessageData() {
    when(messageData.getCode()).thenReturn(IbftV2.PRE_PREPARE);
    when(messageData.getData()).thenReturn(messageBytes);
    IbftPrePrepareMessage prePrepareMessage = IbftPrePrepareMessage.fromMessage(messageData);

    assertThat(prePrepareMessage.getData()).isEqualTo(messageData.getData());
    assertThat(prePrepareMessage.getCode()).isEqualTo(IbftV2.PRE_PREPARE);
  }

  @Test
  public void createMessageFailsWhenIncorrectMessageCode() {
    when(messageData.getCode()).thenReturn(42);
    assertThatThrownBy(() -> IbftPrePrepareMessage.fromMessage(messageData))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Message has code 42 and thus is not a IbftPrePrepareMessage");
  }
}
