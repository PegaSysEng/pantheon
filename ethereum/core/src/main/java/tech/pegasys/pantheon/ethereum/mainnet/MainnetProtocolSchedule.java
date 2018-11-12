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

import tech.pegasys.pantheon.config.GenesisConfigFile;
import tech.pegasys.pantheon.config.GenesisConfigOptions;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolScheduleBuilder.DefaultMilestoneBlocks;

import java.util.Collections;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import io.vertx.core.json.JsonObject;

/** Provides {@link ProtocolSpec} lookups for mainnet hard forks. */
public class MainnetProtocolSchedule {

  private static final long DEFAULT_HOMESTEAD_BLOCK_NUMBER = 1_150_000L;
  private static final long DEFAULT_DAO_BLOCK_NUMBER = 1_920_000L;
  private static final long DEFAULT_TANGERINE_WHISTLE_BLOCK_NUMBER = 2_463_000L;
  private static final long DEFAULT_SPURIOUS_DRAGON_BLOCK_NUMBER = 2_675_000L;
  private static final long DEFAULT_BYZANTIUM_BLOCK_NUMBER = 4_730_000L;
  // Start of Constantinople has not yet been set.
  private static final long DEFAULT_CONSTANTINOPLE_BLOCK_NUMBER = -1L;
  public static final int DEFAULT_CHAIN_ID = 1;
  private static final DefaultMilestoneBlocks DEFAULT_MILESTONE_BLOCKS =
      new DefaultMilestoneBlocks(
          DEFAULT_HOMESTEAD_BLOCK_NUMBER,
          DEFAULT_DAO_BLOCK_NUMBER,
          DEFAULT_TANGERINE_WHISTLE_BLOCK_NUMBER,
          DEFAULT_SPURIOUS_DRAGON_BLOCK_NUMBER,
          DEFAULT_BYZANTIUM_BLOCK_NUMBER,
          DEFAULT_CONSTANTINOPLE_BLOCK_NUMBER);

  /**
   * Creates a mainnet protocol schedule with milestones starting at the specified block numbers
   *
   * @param homesteadBlockNumber Block number at which to start the homestead fork
   * @param daoBlockNumber Block number at which to start the dao fork
   * @param tangerineWhistleBlockNumber Block number at which to start the tangerine whistle fork
   * @param spuriousDragonBlockNumber Block number at which to start the spurious dragon fork
   * @param byzantiumBlockNumber Block number at which to start the byzantium fork
   * @param constantinopleBlockNumber Block number at which to start the constantinople fork
   * @param chainId ID of the blockchain
   * @return MainnetProtocolSchedule return newly instantiated protocol schedule
   */
  public static ProtocolSchedule<Void> create(
      final long homesteadBlockNumber,
      final long daoBlockNumber,
      final long tangerineWhistleBlockNumber,
      final long spuriousDragonBlockNumber,
      final long byzantiumBlockNumber,
      final long constantinopleBlockNumber,
      final int chainId) {

    return fromConfig(
        new GenesisConfigOptions(
            new JsonObject(
                ImmutableMap.<String, Object>builder()
                    .put("ethash", Collections.emptyMap())
                    .put("homesteadblock", homesteadBlockNumber)
                    .put("daoforkblock", daoBlockNumber)
                    .put("eip150block", tangerineWhistleBlockNumber)
                    .put("eip158block", spuriousDragonBlockNumber)
                    .put("byzantiumblock", byzantiumBlockNumber)
                    .put("constantinopleblock", constantinopleBlockNumber)
                    .put("chainid", chainId)
                    .build())));
  }

  public static ProtocolSchedule<Void> create() {
    return fromConfig(GenesisConfigFile.DEFAULT.getConfigOptions());
  }

  /**
   * Create a Mainnet protocol schedule from a config object
   *
   * @param config {@link GenesisConfigOptions} containing the config options for the milestone
   *     starting points
   * @return A configured mainnet protocol schedule
   */
  public static ProtocolSchedule<Void> fromConfig(final GenesisConfigOptions config) {
    return new ProtocolScheduleBuilder<>(config, DEFAULT_MILESTONE_BLOCKS, Function.identity())
        .createProtocolSchedule(DEFAULT_CHAIN_ID);
  }
}
