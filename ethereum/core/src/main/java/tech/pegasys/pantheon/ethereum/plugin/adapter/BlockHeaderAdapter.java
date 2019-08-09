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
package tech.pegasys.pantheon.ethereum.plugin.adapter;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.plugin.data.Address;
import tech.pegasys.pantheon.plugin.data.Hash;
import tech.pegasys.pantheon.plugin.data.Quantity;
import tech.pegasys.pantheon.plugin.data.UnformattedData;

public class BlockHeaderAdapter implements tech.pegasys.pantheon.plugin.data.BlockHeader {

  private final BlockHeader blockHeader;

  public BlockHeaderAdapter(final BlockHeader blockHeader) {
    this.blockHeader = blockHeader;
  }

  @Override
  public Hash getParentHash() {
    return new HashAdapter(blockHeader.getParentHash());
  }

  @Override
  public Hash getOmmersHash() {
    return new HashAdapter(blockHeader.getOmmersHash());
  }

  @Override
  public Address getCoinbase() {
    return new AddressAdapter(blockHeader.getCoinbase());
  }

  @Override
  public Hash getStateRoot() {
    return new HashAdapter(blockHeader.getStateRoot());
  }

  @Override
  public Hash getTransactionsRoot() {
    return new HashAdapter(blockHeader.getTransactionsRoot());
  }

  @Override
  public Hash getReceiptsRoot() {
    return new HashAdapter(blockHeader.getReceiptsRoot());
  }

  @Override
  public UnformattedData getLogsBloom() {
    return new UnformattedDataAdapter(blockHeader.getLogsBloom().getBytes());
  }

  @Override
  public Quantity getDifficulty() {
    return new QuantityAdapter(blockHeader.getDifficulty());
  }

  @Override
  public long getNumber() {
    return blockHeader.getNumber();
  }

  @Override
  public long getGasLimit() {
    return blockHeader.getGasLimit();
  }

  @Override
  public long getGasUsed() {
    return blockHeader.getGasUsed();
  }

  @Override
  public long getTimestamp() {
    return blockHeader.getTimestamp();
  }

  @Override
  public UnformattedData getExtraData() {
    return new UnformattedDataAdapter(blockHeader.getExtraData());
  }

  @Override
  public Hash getMixHash() {
    return new HashAdapter(blockHeader.getMixHash());
  }

  @Override
  public long getNonce() {
    return blockHeader.getNonce();
  }

  @Override
  public Hash getBlockHash() {
    return new HashAdapter(blockHeader.getHash());
  }
}
