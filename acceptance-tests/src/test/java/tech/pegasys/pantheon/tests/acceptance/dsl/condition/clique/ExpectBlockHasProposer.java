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
package tech.pegasys.pantheon.tests.acceptance.dsl.condition.clique;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static tech.pegasys.pantheon.ethereum.core.Hash.fromHexString;
import static tech.pegasys.pantheon.tests.acceptance.dsl.WaitUtils.waitFor;

import tech.pegasys.pantheon.consensus.clique.CliqueBlockHeaderFunctions;
import tech.pegasys.pantheon.consensus.clique.CliqueExtraData;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.LogsBloomFilter;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eth.EthTransactions;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.uint.UInt256;

import org.web3j.protocol.core.methods.response.EthBlock.Block;

public class ExpectBlockHasProposer implements Condition {
  private final EthTransactions eth;
  private Address proposer;

  public ExpectBlockHasProposer(final EthTransactions eth, final Address proposer) {
    this.eth = eth;
    this.proposer = proposer;
  }

  @Override
  public void verify(final Node node) {
    waitFor(() -> assertThat(proposerAddress(node)).isEqualTo(proposer));
  }

  private Address proposerAddress(final Node node) {
    final Block block = node.execute(eth.block());
    final BlockHeader blockHeader = createBlockHeader(block);
    final CliqueExtraData cliqueExtraData = CliqueExtraData.decode(blockHeader);
    return cliqueExtraData.getProposerAddress();
  }

  private BlockHeader createBlockHeader(final Block block) {
    final Hash mixHash =
        block.getMixHash() == null
            ? Hash.fromHexStringLenient("0x0")
            : fromHexString(block.getMixHash());
    return new BlockHeader(
        fromHexString(block.getParentHash()),
        fromHexString(block.getSha3Uncles()),
        Address.fromHexString(block.getMiner()),
        fromHexString(block.getStateRoot()),
        fromHexString(block.getTransactionsRoot()),
        fromHexString(block.getReceiptsRoot()),
        LogsBloomFilter.fromHexString(block.getLogsBloom()),
        UInt256.fromHexString(block.getDifficultyRaw()),
        block.getNumber().longValue(),
        block.getGasLimit().longValue(),
        block.getGasUsed().longValue(),
        block.getTimestamp().longValue(),
        BytesValue.fromHexString(block.getExtraData()),
        mixHash,
        block.getNonce().longValue(),
        new CliqueBlockHeaderFunctions());
  }
}
