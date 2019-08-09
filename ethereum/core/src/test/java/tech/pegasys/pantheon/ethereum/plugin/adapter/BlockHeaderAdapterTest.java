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

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;

import org.junit.Test;

public class BlockHeaderAdapterTest {
  private final BlockHeaderTestFixture blockHeaderTestFixture = new BlockHeaderTestFixture();
  private final BlockHeader blockHeader = blockHeaderTestFixture.buildHeader();
  private final BlockHeaderAdapter blockHeaderAdapter = new BlockHeaderAdapter(blockHeader);

  @Test
  public void getParentHash() {
    assertThat(blockHeaderAdapter.getParentHash().getByteArray())
        .isEqualTo(blockHeader.getParentHash().extractArray());
  }

  @Test
  public void getOmmersHash() {
    assertThat(blockHeaderAdapter.getOmmersHash().getByteArray())
        .isEqualTo(blockHeader.getOmmersHash().extractArray());
  }

  @Test
  public void getCoinbase() {
    assertThat(blockHeaderAdapter.getCoinbase().getByteArray())
        .isEqualTo(blockHeader.getCoinbase().extractArray());
  }

  @Test
  public void getStateRoot() {
    assertThat(blockHeaderAdapter.getStateRoot().getByteArray())
        .isEqualTo(blockHeader.getStateRoot().extractArray());
  }

  @Test
  public void getTransactionsRoot() {
    assertThat(blockHeaderAdapter.getTransactionsRoot().getByteArray())
        .isEqualTo(blockHeader.getTransactionsRoot().extractArray());
  }

  @Test
  public void getReceiptsRoot() {
    assertThat(blockHeaderAdapter.getReceiptsRoot().getByteArray())
        .isEqualTo(blockHeader.getReceiptsRoot().extractArray());
  }

  @Test
  public void getLogsBloom() {
    assertThat(blockHeaderAdapter.getLogsBloom().getByteArray())
        .isEqualTo(blockHeader.getLogsBloom().getBytes().extractArray());
  }

  @Test
  public void getDifficulty() {
    assertThat(blockHeaderAdapter.getDifficulty().getValue())
        .isEqualTo(blockHeader.getDifficulty().toLong());
  }

  @Test
  public void getNumber() {
    assertThat(blockHeaderAdapter.getNumber()).isEqualTo(blockHeader.getNumber());
  }

  @Test
  public void getGasLimit() {
    assertThat(blockHeaderAdapter.getGasLimit()).isEqualTo(blockHeader.getGasLimit());
  }

  @Test
  public void getGasUsed() {
    assertThat(blockHeaderAdapter.getGasUsed()).isEqualTo(blockHeader.getGasUsed());
  }

  @Test
  public void getTimestamp() {
    assertThat(blockHeaderAdapter.getTimestamp()).isEqualTo(blockHeader.getTimestamp());
  }

  @Test
  public void getExtraData() {
    assertThat(blockHeaderAdapter.getExtraData().getByteArray())
        .isEqualTo(blockHeader.getExtraData().extractArray());
  }

  @Test
  public void getMixHash() {
    assertThat(blockHeaderAdapter.getMixHash().getByteArray())
        .isEqualTo(blockHeader.getMixHash().extractArray());
  }

  @Test
  public void getNonce() {
    assertThat(blockHeaderAdapter.getNonce()).isEqualTo(blockHeader.getNonce());
  }

  @Test
  public void getBlockHash() {
    assertThat(blockHeaderAdapter.getBlockHash().getByteArray())
        .isEqualTo(blockHeader.getHash().extractArray());
  }
}
