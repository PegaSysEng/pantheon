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
package tech.pegasys.pantheon.ethereum.eth.sync.fastsync;

import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.ethereum.eth.sync.SyncTargetManager;
import tech.pegasys.pantheon.ethereum.eth.sync.SynchronizerConfiguration;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncState;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncTarget;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.metrics.LabelledMetric;
import tech.pegasys.pantheon.metrics.OperationTimer;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class FastSyncTargetManager<C> extends SyncTargetManager<C> {
  private static final Logger LOG = LogManager.getLogger();

  private final EthContext ethContext;
  private final BlockHeader pivotBlockHeader;

  public FastSyncTargetManager(
      final SynchronizerConfiguration config,
      final ProtocolSchedule<C> protocolSchedule,
      final ProtocolContext<C> protocolContext,
      final EthContext ethContext,
      final SyncState syncState,
      final LabelledMetric<OperationTimer> ethTasksTimer,
      final BlockHeader pivotBlockHeader) {
    super(config, protocolSchedule, protocolContext, ethContext, syncState, ethTasksTimer);
    this.ethContext = ethContext;
    this.pivotBlockHeader = pivotBlockHeader;
  }

  @Override
  protected Optional<EthPeer> selectBestAvailableSyncTarget() {
    final Optional<EthPeer> maybeBestPeer = ethContext.getEthPeers().bestPeer();
    if (!maybeBestPeer.isPresent()) {
      LOG.info("No sync target, wait for peers.");
      return Optional.empty();
    } else {
      final EthPeer bestPeer = maybeBestPeer.get();
      if (bestPeer.chainState().getEstimatedHeight() < pivotBlockHeader.getNumber()) {
        LOG.info("No sync target with sufficient chain height, wait for peers.");
        return Optional.empty();
      }
      return maybeBestPeer;
    }
  }

  @Override
  public boolean shouldSwitchSyncTarget(final SyncTarget currentTarget) {
    return false;
  }
}
