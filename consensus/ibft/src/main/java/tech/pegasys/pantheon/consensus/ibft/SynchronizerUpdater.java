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
package tech.pegasys.pantheon.consensus.ibft;

import static org.apache.logging.log4j.LogManager.getLogger;

import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeers;
import tech.pegasys.pantheon.ethereum.p2p.api.PeerConnection;

import org.apache.logging.log4j.Logger;

public class SynchronizerUpdater implements BaseSynchronizerUpdater {

  private static final Logger LOG = getLogger();
  private final EthPeers ethPeers;

  public SynchronizerUpdater(final EthPeers ethPeers) {
    this.ethPeers = ethPeers;
  }

  @Override
  public void updatePeerChainState(
      final long knownBlockNumber, final PeerConnection peerConnection) {
    final EthPeer ethPeer = ethPeers.peer(peerConnection);
    if (ethPeer == null) {
      LOG.warn("Received message from a peer with no corresponding EthPeer.");
      return;
    }

    if (ethPeer.chainState().getEstimatedHeight() < knownBlockNumber) {
      ethPeer.chainState().updateHeightEstimate(knownBlockNumber);
    }
  }
}
