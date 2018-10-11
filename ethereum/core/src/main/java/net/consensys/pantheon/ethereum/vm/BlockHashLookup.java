package net.consensys.pantheon.ethereum.vm;

import net.consensys.pantheon.ethereum.chain.Blockchain;
import net.consensys.pantheon.ethereum.core.BlockHeader;
import net.consensys.pantheon.ethereum.core.Hash;
import net.consensys.pantheon.ethereum.core.ProcessableBlockHeader;
import net.consensys.pantheon.ethereum.vm.operations.BlockHashOperation;

import java.util.HashMap;
import java.util.Map;

/**
 * Calculates and caches block hashes by number following the chain for a specific branch. This is
 * used by {@link BlockHashOperation} and ensures that the correct block hash is returned even when
 * the block being imported is on a fork.
 *
 * <p>A new BlockHashCache must be created for each block being processed but should be reused for
 * all transactions within that block.
 */
public class BlockHashLookup {

  private BlockHeader searchStartHeader;
  private final Blockchain blockchain;
  private final Map<Long, Hash> hashByNumber = new HashMap<>();

  public BlockHashLookup(final ProcessableBlockHeader currentBlock, final Blockchain blockchain) {
    this.searchStartHeader =
        blockchain
            .getBlockHeader(currentBlock.getParentHash())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Attempting to process a block header without parent being on chain"));
    this.blockchain = blockchain;
    hashByNumber.put(currentBlock.getNumber() - 1, currentBlock.getParentHash());
  }

  public Hash getBlockHash(final long blockNumber) {
    final Hash cachedHash = hashByNumber.get(blockNumber);
    if (cachedHash != null) {
      return cachedHash;
    }
    while (searchStartHeader != null && searchStartHeader.getNumber() != blockNumber) {
      hashByNumber.put(searchStartHeader.getNumber(), searchStartHeader.getHash());
      hashByNumber.put(searchStartHeader.getNumber() - 1, searchStartHeader.getParentHash());
      searchStartHeader = blockchain.getBlockHeader(searchStartHeader.getParentHash()).orElse(null);
    }
    return hashByNumber.get(blockNumber);
  }
}
