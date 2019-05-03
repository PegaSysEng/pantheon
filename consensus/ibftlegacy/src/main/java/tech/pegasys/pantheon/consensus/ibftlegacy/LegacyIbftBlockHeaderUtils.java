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
package tech.pegasys.pantheon.consensus.ibftlegacy;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderUtils;
import tech.pegasys.pantheon.ethereum.core.Hash;

import java.util.function.Function;

public class LegacyIbftBlockHeaderUtils implements BlockHeaderUtils {

  private static final BlockHeaderUtils ON_CHAIN =
      new LegacyIbftBlockHeaderUtils(IbftBlockHashing::calculateHashOfIbftBlockOnChain);

  private final Function<BlockHeader, Hash> hashFunction;

  private LegacyIbftBlockHeaderUtils(final Function<BlockHeader, Hash> hashFunction) {
    this.hashFunction = hashFunction;
  }

  public static BlockHeaderUtils forOnChainBlock() {
    return ON_CHAIN;
  }

  @Override
  public Hash hash(final BlockHeader header) {
    return hashFunction.apply(header);
  }

  @Override
  public Object parseExtraData(final BlockHeader header) {
    // Legacy IBFT always has an invalid genesis block so don't try to parse it.
    if (header.getNumber() == BlockHeader.GENESIS_BLOCK_NUMBER) {
      return header.getExtraData();
    }
    return IbftExtraData.decodeRaw(header.getExtraData());
  }
}
