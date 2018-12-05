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
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftUnsignedCommitMessageData;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IbftCommitMessageTest {
  @Mock private IbftSignedMessageData<IbftUnsignedCommitMessageData> commitMessageData;
  @Mock private BytesValue messageBytes;
  @Mock private MessageData messageData;
  @Mock private IbftCommitMessage commitMessage;
  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void createMessageFromCommitMessageData() {
    when(commitMessageData.encode()).thenReturn(messageBytes);
    IbftCommitMessage commitMessage = IbftCommitMessage.create(commitMessageData);

    assertThat(commitMessage.getData()).isEqualTo(messageBytes);
    assertThat(commitMessage.getCode()).isEqualTo(IbftV2.COMMIT);
    verify(commitMessageData).encode();
  }

  @Test
  public void createMessageFromCommitMessage() {
    IbftCommitMessage message = IbftCommitMessage.fromMessage(commitMessage);

    assertThat(message).isEqualTo(commitMessage);
  }

  @Test
  public void createMessageFromGenericMessageData() {
    when(messageData.getData()).thenReturn(messageBytes);
    when(messageData.getCode()).thenReturn(IbftV2.COMMIT);
    IbftCommitMessage commitMessage = IbftCommitMessage.fromMessage(messageData);

    assertThat(messageData.getData()).isEqualTo(commitMessage.getData());
    assertThat(commitMessage.getCode()).isEqualTo(IbftV2.COMMIT);
  }

  @Test
  public void createMessageFailsWhenIncorrectMessageType() {
    when(messageData.getCode()).thenReturn(42);
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Message has code 42 and thus is not a IbftCommitMessage");
    IbftCommitMessage.fromMessage(messageData);
  }
}
