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
package tech.pegasys.pantheon.ethereum.eth.sync.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.ethereum.chain.BlockAddedObserver;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SyncStateTest {

  private final Blockchain blockchain = mock(Blockchain.class);
  private final EthPeers ethPeers = mock(EthPeers.class);
  private SyncState syncState;
  private BlockAddedObserver blockAddedObserver;

  @Before
  public void setUp() {
    final ArgumentCaptor<BlockAddedObserver> captor = ArgumentCaptor.forClass(BlockAddedObserver.class);
    when(blockchain.observeBlockAdded(captor.capture())).thenReturn(1L);
    syncState = new SyncState(blockchain, ethPeers);
    blockAddedObserver = captor.getValue();
  }

  @Test
  public void shouldBeInSyncWhenNoSyncTargetHasBeenSet() {
    assertThat(syncState.isInSync()).isTrue();
  }
}
