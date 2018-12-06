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
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftUnsignedRoundChangeMessageData;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IbftRoundChangeMessageTest {
  @Mock private IbftSignedMessageData<IbftUnsignedRoundChangeMessageData> roundChangeMessageData;
  @Mock private BytesValue messageBytes;
  @Mock private MessageData messageData;
  @Mock private IbftRoundChangeMessage roundChangeMessage;

  @Test
  public void createMessageFromRoundChangeMessageData() {
    when(roundChangeMessageData.encode()).thenReturn(messageBytes);
    IbftRoundChangeMessage roundChangeMessage =
        IbftRoundChangeMessage.create(roundChangeMessageData);

    assertThat(roundChangeMessage.getData()).isEqualTo(messageBytes);
    assertThat(roundChangeMessage.getCode()).isEqualTo(IbftV2.ROUND_CHANGE);
    verify(roundChangeMessageData).encode();
  }

  @Test
  public void createMessageFromRoundChangeMessage() {
    IbftRoundChangeMessage message = IbftRoundChangeMessage.fromMessage(roundChangeMessage);
    assertThat(message).isSameAs(roundChangeMessage);
  }

  @Test
  public void createMessageFromGenericMessageData() {
    when(messageData.getData()).thenReturn(messageBytes);
    when(messageData.getCode()).thenReturn(IbftV2.ROUND_CHANGE);
    IbftRoundChangeMessage roundChangeMessage = IbftRoundChangeMessage.fromMessage(messageData);

    assertThat(roundChangeMessage.getData()).isEqualTo(messageData.getData());
    assertThat(roundChangeMessage.getCode()).isEqualTo(IbftV2.ROUND_CHANGE);
  }

  @Test
  public void createMessageFailsWhenIncorrectMessageCode() {
    when(messageData.getCode()).thenReturn(42);
    assertThatThrownBy(() -> IbftRoundChangeMessage.fromMessage(messageData))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Message has code 42 and thus is not a IbftRoundChangeMessage");
  }
}
