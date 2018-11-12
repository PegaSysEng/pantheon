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
package tech.pegasys.pantheon.ethereum.mainnet;

import tech.pegasys.pantheon.config.GenesisConfigOptions;

import java.util.function.Function;

public class ProtocolScheduleBuilder<C> {
  private final GenesisConfigOptions config;
  private final DefaultMilestoneBlocks defaultMilestoneBlocks;
  private final Function<ProtocolSpecBuilder<Void>, ProtocolSpecBuilder<C>> protocolSpecAdapter;

  public ProtocolScheduleBuilder(
      final GenesisConfigOptions config,
      final DefaultMilestoneBlocks defaultMilestoneBlocks,
      final Function<ProtocolSpecBuilder<Void>, ProtocolSpecBuilder<C>> protocolSpecAdapter) {
    this.config = config;
    this.defaultMilestoneBlocks = defaultMilestoneBlocks;
    this.protocolSpecAdapter = protocolSpecAdapter;
  }

  public ProtocolSchedule<C> createProtocolSchedule(final int defaultChainId) {
    final int chainId = config.getChainId().orElse(defaultChainId);
    final MutableProtocolSchedule<C> protocolSchedule = new MutableProtocolSchedule<>(chainId);

    addProtocolSpec(protocolSchedule, 0, MainnetProtocolSpecs.frontierDefinition());
    addProtocolSpec(
        protocolSchedule,
        config.getHomesteadBlockNumber().orElse(defaultMilestoneBlocks.getHomesteadBlockNumber()),
        MainnetProtocolSpecs.homesteadDefinition());

    final long daoBlockNumber = config.getDaoForkBlock().orElse(defaultMilestoneBlocks.getDaoBlockNumber());
    if (daoBlockNumber > 0) {
      final ProtocolSpec<C> originalProtocolSpec = protocolSchedule.getByBlockNumber(daoBlockNumber);
      addProtocolSpec(
          protocolSchedule, daoBlockNumber, MainnetProtocolSpecs.daoRecoveryInitDefinition());
      addProtocolSpec(
          protocolSchedule, daoBlockNumber + 1, MainnetProtocolSpecs.daoRecoveryTransitionDefinition());

      // Return to the previous protocol spec after the dao fork has completed.
      protocolSchedule.putMilestone(daoBlockNumber + 10, originalProtocolSpec);
    }

    addProtocolSpec(
        protocolSchedule,
        config
            .getTangerineWhistleBlockNumber()
            .orElse(defaultMilestoneBlocks.getTangerineWhistleBlockNumber()),
        MainnetProtocolSpecs.tangerineWhistleDefinition());
    addProtocolSpec(
        protocolSchedule,
        config
            .getSpuriousDragonBlockNumber()
            .orElse(defaultMilestoneBlocks.getSpuriousDragonBlockNumber()),
        MainnetProtocolSpecs.spuriousDragonDefinition(chainId));
    addProtocolSpec(
        protocolSchedule,
        config.getByzantiumBlockNumber().orElse(defaultMilestoneBlocks.getByzantiumBlockNumber()),
        MainnetProtocolSpecs.byzantiumDefinition(chainId));
    addProtocolSpec(
        protocolSchedule,
        config
            .getConstantinopleBlockNumber()
            .orElse(defaultMilestoneBlocks.getConstantinopleBlockNumber()),
        MainnetProtocolSpecs.constantinopleDefinition(chainId));

    return protocolSchedule;
  }

  private void addProtocolSpec(
      final MutableProtocolSchedule<C> protocolSchedule,
      final long blockNumber,
      final ProtocolSpecBuilder<Void> definition) {
    if (blockNumber >= 0) {
      protocolSchedule.putMilestone(
          blockNumber, protocolSpecAdapter.apply(definition).build(protocolSchedule));
    }
  }

  public static class DefaultMilestoneBlocks {

    public static final DefaultMilestoneBlocks NO_DEFAULTS =
        new DefaultMilestoneBlocks(-1, -1, -1, -1, -1, -1);

    private final long homesteadBlockNumber;
    private final long daoBlockNumber;
    private final long tangerineWhistleBlockNumber;
    private final long spuriousDragonBlockNumber;
    private final long byzantiumBlockNumber;
    private final long constantinopleBlockNumber;

    public DefaultMilestoneBlocks(
        final long homesteadBlockNumber,
        final long daoBlockNumber,
        final long tangerineWhistleBlockNumber,
        final long spuriousDragonBlockNumber,
        final long byzantiumBlockNumber,
        final long constantinopleBlockNumber) {
      this.homesteadBlockNumber = homesteadBlockNumber;
      this.daoBlockNumber = daoBlockNumber;
      this.tangerineWhistleBlockNumber = tangerineWhistleBlockNumber;
      this.spuriousDragonBlockNumber = spuriousDragonBlockNumber;
      this.byzantiumBlockNumber = byzantiumBlockNumber;
      this.constantinopleBlockNumber = constantinopleBlockNumber;
    }

    public long getHomesteadBlockNumber() {
      return homesteadBlockNumber;
    }

    public long getDaoBlockNumber() {
      return daoBlockNumber;
    }

    public long getTangerineWhistleBlockNumber() {
      return tangerineWhistleBlockNumber;
    }

    public long getSpuriousDragonBlockNumber() {
      return spuriousDragonBlockNumber;
    }

    public long getByzantiumBlockNumber() {
      return byzantiumBlockNumber;
    }

    public long getConstantinopleBlockNumber() {
      return constantinopleBlockNumber;
    }
  }
}
