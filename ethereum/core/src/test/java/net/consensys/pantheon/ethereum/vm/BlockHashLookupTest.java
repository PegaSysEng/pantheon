package net.consensys.pantheon.ethereum.vm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.consensys.pantheon.ethereum.chain.Blockchain;
import net.consensys.pantheon.ethereum.core.BlockHeader;
import net.consensys.pantheon.ethereum.core.BlockHeaderTestFixture;
import net.consensys.pantheon.ethereum.core.Hash;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BlockHashLookupTest {

  private static final int CURRENT_BLOCK_NUMBER = 256;
  private final Blockchain blockchain = mock(Blockchain.class);
  private final BlockHeader[] headers = new BlockHeader[CURRENT_BLOCK_NUMBER];
  private BlockHashLookup cache;

  @Before
  public void setUp() {
    BlockHeader parentHeader = null;
    for (int i = 0; i < headers.length; i++) {
      final BlockHeader header = createHeader(i, parentHeader);
      when(blockchain.getBlockHeader(header.getHash())).thenReturn(Optional.of(header));
      headers[i] = header;
      parentHeader = headers[i];
    }
    cache =
        new BlockHashLookup(
            createHeader(CURRENT_BLOCK_NUMBER, headers[headers.length - 1]), blockchain);
  }

  @After
  public void verifyBlocksNeverLookedUpByNumber() {
    // Looking up the block by number is incorrect because it always uses the canonical chain even
    // if the block being imported is on a fork.
    verify(blockchain, never()).getBlockHeader(anyLong());
  }

  @Test
  public void shouldGetHashOfImmediateParent() {
    assertHashForBlockNumber(CURRENT_BLOCK_NUMBER - 1);
  }

  @Test
  public void shouldGetHashOfGenesisBlock() {
    assertHashForBlockNumber(0);
  }

  @Test
  public void shouldGetHashForRecentBlockAfterOlderBlock() {
    assertHashForBlockNumber(10);
    assertHashForBlockNumber(CURRENT_BLOCK_NUMBER - 1);
  }

  private void assertHashForBlockNumber(final int blockNumber) {
    assertThat(cache.getBlockHash(blockNumber)).isEqualTo(headers[blockNumber].getHash());
  }

  private BlockHeader createHeader(final int blockNumber, final BlockHeader parentHeader) {
    return new BlockHeaderTestFixture()
        .number(blockNumber)
        .parentHash(parentHeader != null ? parentHeader.getHash() : Hash.EMPTY)
        .buildHeader();
  }
}
