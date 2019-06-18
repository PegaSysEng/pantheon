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
package tech.pegasys.pantheon.ethereum.eth.manager.task;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static tech.pegasys.pantheon.ethereum.vm.TestBlockchain.generateTestBlockHash;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.ethereum.eth.manager.PendingPeerRequest;
import tech.pegasys.pantheon.ethereum.eth.messages.BlockHeadersMessage;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.p2p.rlpx.wire.MessageData;
import tech.pegasys.pantheon.metrics.LabelledMetric;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.metrics.OperationTimer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*"})
@PrepareForTest(fullyQualifiedNames = "tech.pegasys.pantheon.ethereum.eth.*")
public class AbstractGetHeadersFromPeerTaskTest {

  @Mock private EthPeer peerMock;
  @Mock private MessageData messageData;
  @Mock private ProtocolSchedule protocolScheduleMock;
  @Mock private EthContext ethContextMock;
  @Mock private MetricsSystem metricsSystemMock;
  @Mock private LabelledMetric<OperationTimer> labelledMetricMock;
  @Mock private BlockHeadersMessage blockHeadersMessageMock;
  @Mock private Matcher matcherMock;
  private AbstractGetHeadersFromPeerTask getHeadersFromPeerTask;

  @Before
  public void setUp() {

    when(metricsSystemMock.createLabelledTimer(any(), any(), any(), any()))
        .thenReturn(labelledMetricMock);
    getHeadersFromPeerTask =
        new AbstractGetHeadersFromPeerTask(
            protocolScheduleMock, ethContextMock, 2, 0, false, metricsSystemMock) {
          @Override
          protected boolean matchesFirstHeader(BlockHeader firstHeader) {
            return matcherMock.match();
          }

          @Override
          protected PendingPeerRequest sendRequest() {
            return null;
          }
        };
  }

  @Test
  public void checkThatSequentialHeadersFormingAChainWorks() {
    mockStatic(BlockHeadersMessage.class);
    when(matcherMock.match()).thenReturn(true);
    final BlockHeader block1 =
        new BlockHeaderTestFixture().number(1).parentHash(generateTestBlockHash(0)).buildHeader();
    final BlockHeader block2 =
        new BlockHeaderTestFixture().number(2).parentHash(block1.getHash()).buildHeader();
    final List<BlockHeader> headers = Arrays.asList(block1, block2);
    when(blockHeadersMessageMock.getHeaders(any())).thenReturn(headers);
    when(BlockHeadersMessage.readFrom(any())).thenReturn(blockHeadersMessageMock);
    Optional<List<BlockHeader>> optionalBlockHeaders =
        getHeadersFromPeerTask.processResponse(false, messageData, peerMock);
    assertNotNull(optionalBlockHeaders);
    assertTrue(optionalBlockHeaders.isPresent());
    List<BlockHeader> blockHeaders = optionalBlockHeaders.get();
    assertThat(blockHeaders, hasSize(2));
  }

  @Test
  public void checkThatSequentialHeadersNotFormingAChainFails() {
    mockStatic(BlockHeadersMessage.class);
    when(matcherMock.match()).thenReturn(true);
    final BlockHeader block1 =
        new BlockHeaderTestFixture().number(1).parentHash(generateTestBlockHash(0)).buildHeader();
    final BlockHeader block2 =
        new BlockHeaderTestFixture().number(2).parentHash(generateTestBlockHash(1)).buildHeader();
    final List<BlockHeader> headers = Arrays.asList(block1, block2);
    when(blockHeadersMessageMock.getHeaders(any())).thenReturn(headers);
    when(BlockHeadersMessage.readFrom(any())).thenReturn(blockHeadersMessageMock);
    Optional<List<BlockHeader>> optionalBlockHeaders =
        getHeadersFromPeerTask.processResponse(false, messageData, peerMock);
    assertNotNull(optionalBlockHeaders);
    assertFalse(optionalBlockHeaders.isPresent());
  }

  @FunctionalInterface
  private interface Matcher {
    boolean match();
  }
}
