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
package tech.pegasys.pantheon.consensus.clique.blockcreation;

import tech.pegasys.pantheon.consensus.clique.CliqueContext;
import tech.pegasys.pantheon.ethereum.blockcreation.AbstractMiningCoordinator;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncState;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.Optional;

public class CliqueMiningCoordinator
    extends AbstractMiningCoordinator<CliqueContext, CliqueBlockMiner> {

  public CliqueMiningCoordinator(
      final Blockchain blockchain, final CliqueMinerExecutor executor, final SyncState syncState) {
    super(blockchain, executor, syncState);
  }

  @Override
  public boolean newBlockExceedsCurrentOperation(final Block newBlock) {
    final Optional<UInt256> totalDifficulty =
        blockchain.getTotalDifficultyByHash(newBlock.getHash());

    return totalDifficulty
        .map(td -> td.compareTo(currentRunningMiner.get().getTotalDifficulty()) >= 0)
        .orElse(false);
  }
}
