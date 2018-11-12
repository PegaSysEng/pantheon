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
package tech.pegasys.pantheon.consensus.clique;

import tech.pegasys.pantheon.config.CliqueConfigOptions;
import tech.pegasys.pantheon.config.GenesisConfigOptions;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolScheduleBuilder;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolScheduleBuilder.DefaultMilestoneBlocks;

/**
 * Defines the protocol behaviours for a blockchain using Clique.
 */
public class CliqueProtocolSchedule {

  private static final int DEFAULT_CHAIN_ID = 4;

  public static ProtocolSchedule<CliqueContext> create(
      final GenesisConfigOptions config, final KeyPair nodeKeys) {

    final CliqueConfigOptions cliqueConfig = config.getCliqueConfigOptions();

    // TODO(tmm) replace address with passed in node data (coming later)
    final CliqueProtocolSpecs specs =
        new CliqueProtocolSpecs(
            (long) cliqueConfig.getBlockPeriodSeconds(),
            cliqueConfig.getEpochLength(),
            Util.publicKeyToAddress(nodeKeys.getPublicKey()));

    final ProtocolScheduleBuilder<CliqueContext> builder = new ProtocolScheduleBuilder<>(
        config,
        DefaultMilestoneBlocks.NO_DEFAULTS,
        specs::applyCliqueSpecificModifications);
    return builder.createProtocolSchedule(DEFAULT_CHAIN_ID);
  }
}
