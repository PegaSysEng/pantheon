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
package tech.pegasys.pantheon.ethereum.worldstate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.chain.BlockchainStorage;
import tech.pegasys.pantheon.ethereum.chain.DefaultMutableBlockchain;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockDataGenerator;
import tech.pegasys.pantheon.ethereum.core.BlockDataGenerator.BlockOptions;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.TransactionReceipt;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockHeaderFunctions;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.KeyValueStoragePrefixedKeyBlockchainStorage;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.services.kvstore.InMemoryKeyValueStorage;
import tech.pegasys.pantheon.testutil.MockExecutorService;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PrunerTest {

  private final NoOpMetricsSystem metricsSystem = new NoOpMetricsSystem();

  private final BlockDataGenerator gen = new BlockDataGenerator();

  @Mock private MarkSweepPruner markSweepPruner;
  private final ExecutorServiceBuilder mockExecutorServiceBuilder = MockExecutorService::new;

  @Test
  public void shouldMarkCorrectBlockAndSweep() throws InterruptedException {
    final Block genesisBlock = gen.genesisBlock();
    final BlockchainStorage blockchainStorage =
        new KeyValueStoragePrefixedKeyBlockchainStorage(
            new InMemoryKeyValueStorage(), new MainnetBlockHeaderFunctions());
    final DefaultMutableBlockchain blockchain =
        new DefaultMutableBlockchain(genesisBlock, blockchainStorage, metricsSystem);

    final Pruner pruner = new Pruner(markSweepPruner, blockchain, mockExecutorServiceBuilder, 0, 0);
    pruner.start();

    BlockOptions options =
        new BlockOptions().setBlockNumber(1L).setParentHash(genesisBlock.getHash());
    final Block newBlock = gen.block(options);
    List<TransactionReceipt> receipts = gen.receipts(newBlock);
    blockchain.appendBlock(newBlock, receipts);
    verify(markSweepPruner).mark(newBlock.getHeader().getStateRoot());
    verify(markSweepPruner).sweep();
    pruner.stop();
  }

  @Test
  public void shouldOnlySweepAfterTransientForkPeriodAndRetentionPeriodEnds()
      throws InterruptedException {
    final Block genesisBlock = gen.genesisBlock();
    final BlockchainStorage blockchainStorage =
        new KeyValueStoragePrefixedKeyBlockchainStorage(
            new InMemoryKeyValueStorage(), new MainnetBlockHeaderFunctions());
    final DefaultMutableBlockchain blockchain =
        new DefaultMutableBlockchain(genesisBlock, blockchainStorage, metricsSystem);

    final Pruner pruner = new Pruner(markSweepPruner, blockchain, mockExecutorServiceBuilder, 1, 2);
    pruner.start();

    BlockOptions options =
        new BlockOptions().setBlockNumber(1L).setParentHash(genesisBlock.getHash());
    Block newBlock = gen.block(options);
    List<TransactionReceipt> receipts = gen.receipts(newBlock);
    blockchain.appendBlock(newBlock, receipts);
    final Hash markBlockStateRootHash = newBlock.getHeader().getStateRoot();
    verify(markSweepPruner, never()).mark(markBlockStateRootHash);
    verify(markSweepPruner, never()).sweep();

    options = options.setBlockNumber(2L).setParentHash(newBlock.getHash());
    newBlock = gen.block(options);
    receipts = gen.receipts(newBlock);
    blockchain.appendBlock(newBlock, receipts);
    verify(markSweepPruner).mark(markBlockStateRootHash);
    verify(markSweepPruner, never()).sweep();

    options = options.setBlockNumber(3L).setParentHash(newBlock.getHash());
    newBlock = gen.block(options);
    receipts = gen.receipts(newBlock);
    blockchain.appendBlock(newBlock, receipts);
    verify(markSweepPruner).sweep();
    pruner.stop();
  }

  @Test
  public void abortsPruningWhenFullyMarkedBlockNoLongerOnCanonicalChain()
      throws InterruptedException {
    final Block genesisBlock = gen.genesisBlock();
    final BlockchainStorage blockchainStorage =
        new KeyValueStoragePrefixedKeyBlockchainStorage(
            new InMemoryKeyValueStorage(), new MainnetBlockHeaderFunctions());
    final DefaultMutableBlockchain blockchain =
        new DefaultMutableBlockchain(genesisBlock, blockchainStorage, metricsSystem);

    // start pruner so it can start handling block added events
    final Pruner pruner = new Pruner(markSweepPruner, blockchain, mockExecutorServiceBuilder, 0, 1);
    pruner.start();

    /*
     Set up pre-marking state:
      O <---- this is a fork as of now (non-canonical)
      |  O <- marking of the full state trie will begin at this block
       \/
       O <--- the common ancestor when the reorg happens
    */
    BlockOptions options =
        new BlockOptions().setBlockNumber(1L).setParentHash(genesisBlock.getHash());
    final Block canonicalHead = gen.block(options);
    List<TransactionReceipt> receipts = gen.receipts(canonicalHead);
    blockchain.appendBlock(canonicalHead, receipts);

    options = new BlockOptions().setBlockNumber(1L).setParentHash(genesisBlock.getHash());
    final Block forkBlock = gen.block(options);
    receipts = gen.receipts(forkBlock);
    blockchain.appendBlock(forkBlock, receipts);
    /*
      Cause reorg:
      O <---- this block causes a reorg; this branch becomes canonical
      |
      O <---- which means that state here is referring to nodes from the common ancestor block,
      |       which is not fully marked
      |  O <- because this was block at which marking began
       \/
       O
    */
    options = new BlockOptions().setBlockNumber(2L).setParentHash(forkBlock.getHash());
    final Block reorgCausingBlock = gen.block(options);
    receipts = gen.receipts(reorgCausingBlock);
    blockchain.appendBlock(reorgCausingBlock, receipts);
    verify(markSweepPruner).mark(canonicalHead.getHeader().getStateRoot());
    verify(markSweepPruner, never()).sweep();
    pruner.stop();
  }

  @Test
  public void shouldRejectInvalidArguments() {
    final Blockchain mockchain = mock(Blockchain.class);
    assertThatThrownBy(
            () -> new Pruner(markSweepPruner, mockchain, mockExecutorServiceBuilder, -1, -2))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> new Pruner(markSweepPruner, mockchain, mockExecutorServiceBuilder, 0, -1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> new Pruner(markSweepPruner, mockchain, mockExecutorServiceBuilder, 1, 0))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
