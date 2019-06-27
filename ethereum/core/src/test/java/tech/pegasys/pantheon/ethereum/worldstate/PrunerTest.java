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

import tech.pegasys.pantheon.consensus.clique.CliqueBlockHeaderFunctions;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.chain.BlockchainStorage;
import tech.pegasys.pantheon.ethereum.chain.DefaultMutableBlockchain;
import tech.pegasys.pantheon.ethereum.chain.DefaultMutableBlockchainTest;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockDataGenerator;
import tech.pegasys.pantheon.ethereum.core.BlockDataGenerator.BlockOptions;
import tech.pegasys.pantheon.ethereum.core.TransactionReceipt;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockHeaderFunctions;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.KeyValueStoragePrefixedKeyBlockchainStorage;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.WorldStateKeyValueStorage;
import tech.pegasys.pantheon.ethereum.vm.TestBlockchain;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.services.kvstore.InMemoryKeyValueStorage;

import org.junit.Test;
import tech.pegasys.pantheon.services.kvstore.KeyValueStorage;

import java.util.List;

public class PrunerTest {

  private final NoOpMetricsSystem metricsSystem = new NoOpMetricsSystem();

  @Test
  public void doesNotLoseStateDuringReorg() {
    final InMemoryKeyValueStorage markStorage = new InMemoryKeyValueStorage();
    final InMemoryKeyValueStorage stateStorage = new InMemoryKeyValueStorage();
    final WorldStateStorage worldStateStorage = new WorldStateKeyValueStorage(stateStorage);
//    final WorldStateArchive worldStateArchive = new WorldStateArchive(worldStateStorage);

    final BlockDataGenerator gen = new BlockDataGenerator();
    final KeyValueStorage kvStore = new InMemoryKeyValueStorage();
    final Block genesisBlock = gen.genesisBlock();
    final DefaultMutableBlockchain blockchain = createBlockchain(kvStore, genesisBlock);

    /*
     Set up pre-marking state:
      O    <--- this is a fork as of now (non-canonical)
      |  O <--- marking of the full state trie will begin at this block
       \/
       O   <--- the common ancestor when the reorg happens
    */
    BlockOptions options =
            new BlockOptions().setBlockNumber(1L).setParentHash(genesisBlock.getHash());
    Block newBlock = gen.block(options);
    List<TransactionReceipt> receipts = gen.receipts(newBlock);
    blockchain.appendBlock(newBlock, receipts);

    options = new BlockOptions().setBlockNumber(1L).setParentHash(genesisBlock.getHash());
    newBlock = gen.block(options);
    receipts = gen.receipts(newBlock);
    blockchain.appendBlock(newBlock, receipts);
    /*
      Cause reorg:
      O <-- this block causes a reorg, this branch becomes canonical
      |
      O <-- which means that state here is referring to nodes from the common ancestor block,
      |     which is not fully marked
      |  O <--- because this was the last fully marked block
       \/
       O
    */
    final Pruner pruner =
        new Pruner(
            new MarkSweepPruner(worldStateStorage, markStorage, metricsSystem), blockchain, 2);
    pruner.start();
  }

  private DefaultMutableBlockchain createBlockchain(
          final KeyValueStorage kvStore, final Block genesisBlock) {
    return new DefaultMutableBlockchain(
            genesisBlock,
            new KeyValueStoragePrefixedKeyBlockchainStorage(kvStore, new MainnetBlockHeaderFunctions()),
            new NoOpMetricsSystem());
  }
}
