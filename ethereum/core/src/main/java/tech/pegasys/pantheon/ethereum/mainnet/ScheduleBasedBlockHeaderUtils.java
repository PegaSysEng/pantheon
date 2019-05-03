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

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderUtils;
import tech.pegasys.pantheon.ethereum.core.ExtraData;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.SealableBlockHeader;

/**
 * Looks up the correct {@link BlockHeaderUtils} to use based on a {@link ProtocolSchedule} to
 * ensure that the correct hash is created given the block number.
 */
public class ScheduleBasedBlockHeaderUtils<C> implements BlockHeaderUtils {

  private final ProtocolSchedule<C> protocolSchedule;

  private ScheduleBasedBlockHeaderUtils(final ProtocolSchedule<C> protocolSchedule) {
    this.protocolSchedule = protocolSchedule;
  }

  public static <C> BlockHeaderUtils create(final ProtocolSchedule<C> protocolSchedule) {
    return new ScheduleBasedBlockHeaderUtils<>(protocolSchedule);
  }

  @Override
  public Hash hash(final BlockHeader header) {
    return getBlockHeaderUtils(header).hash(header);
  }

  @Override
  public ExtraData parseExtraData(final BlockHeader header) {
    return getBlockHeaderUtils(header).parseExtraData(header);
  }

  private BlockHeaderUtils getBlockHeaderUtils(final SealableBlockHeader header) {
    return protocolSchedule.getByBlockNumber(header.getNumber()).getBlockHeaderUtils();
  }
}
