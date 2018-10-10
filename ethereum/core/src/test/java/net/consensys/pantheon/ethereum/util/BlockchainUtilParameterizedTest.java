package net.consensys.pantheon.ethereum.util;

import static org.assertj.core.api.Assertions.assertThat;

import net.consensys.pantheon.ethereum.core.Block;
import net.consensys.pantheon.ethereum.core.BlockBody;
import net.consensys.pantheon.ethereum.core.BlockHeader;
import net.consensys.pantheon.ethereum.core.TransactionReceipt;
import net.consensys.pantheon.ethereum.db.DefaultMutableBlockchain;
import net.consensys.pantheon.ethereum.mainnet.MainnetBlockHashFunction;
import net.consensys.pantheon.ethereum.testutil.BlockDataGenerator;
import net.consensys.pantheon.services.kvstore.InMemoryKeyValueStorage;
import net.consensys.pantheon.services.kvstore.KeyValueStorage;
import net.consensys.pantheon.util.uint.UInt256;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class BlockchainUtilParameterizedTest {
  private static final BlockDataGenerator blockDataGenerator = new BlockDataGenerator();
  private static final Random random = new Random(666);

  private static final int chainHeight = 50;
  private final int headerRequestSize;
  private final int commonAncestorHeight;
  private static Block genesisBlock;
  private static KeyValueStorage localKvStore;
  private static DefaultMutableBlockchain localBlockchain;

  private KeyValueStorage remoteKvStore;
  private DefaultMutableBlockchain remoteBlockchain;

  public BlockchainUtilParameterizedTest(
      final int headerRequestSize, final int commonAncestorHeight) {
    this.headerRequestSize = headerRequestSize;
    this.commonAncestorHeight = commonAncestorHeight;
  }

  @BeforeClass
  public static void setupClass() {
    genesisBlock = blockDataGenerator.genesisBlock();
    localKvStore = new InMemoryKeyValueStorage();
    localBlockchain =
        new DefaultMutableBlockchain(
            genesisBlock, localKvStore, MainnetBlockHashFunction::createHash);

    // Setup local chain
    for (int i = 1; i <= chainHeight; i++) {
      final BlockDataGenerator.BlockOptions options =
          new BlockDataGenerator.BlockOptions()
              .setBlockNumber(i)
              .setParentHash(localBlockchain.getBlockHashByNumber(i - 1).get());
      final Block block = blockDataGenerator.block(options);
      final List<TransactionReceipt> receipts = blockDataGenerator.receipts(block);
      localBlockchain.appendBlock(block, receipts);
    }
  }

  @Before
  public void setup() {
    remoteKvStore = new InMemoryKeyValueStorage();
    remoteBlockchain =
        new DefaultMutableBlockchain(
            genesisBlock, remoteKvStore, MainnetBlockHashFunction::createHash);
  }

  @Parameterized.Parameters(name = "requestSize={0}, commonAncestor={1}")
  public static Collection<Object[]> parameters() {
    final int[] requestSizes = {5, 12, chainHeight, chainHeight * 2};
    final List<Object[]> params = new ArrayList<>();
    for (final int requestSize : requestSizes) {
      for (int i = 0; i <= chainHeight; i++) {
        if (i == 0) {
          params.add(new Object[] {requestSize, i});
        }
        if (i == random.nextInt(chainHeight - 1) + 1) {
          params.add(new Object[] {requestSize, i});
        }
        if (i == chainHeight) {
          params.add(new Object[] {requestSize, i});
        }
      }
    }
    return params;
  }

  @Test
  public void searchesAscending() {
    BlockHeader commonHeader = genesisBlock.getHeader();
    for (long i = 1; i <= commonAncestorHeight; i++) {
      commonHeader = localBlockchain.getBlockHeader(i).get();
      final List<TransactionReceipt> receipts =
          localBlockchain.getTxReceipts(commonHeader.getHash()).get();
      final BlockBody commonBody = localBlockchain.getBlockBody(commonHeader.getHash()).get();
      remoteBlockchain.appendBlock(new Block(commonHeader, commonBody), receipts);
    }

    // Remaining blocks are disparate...
    for (long i = commonAncestorHeight + 1L; i <= chainHeight; i++) {
      final BlockDataGenerator.BlockOptions localOptions =
          new BlockDataGenerator.BlockOptions()
              .setBlockNumber(i)
              .setParentHash(localBlockchain.getBlockHashByNumber(i - 1).get());
      final Block localBlock = blockDataGenerator.block(localOptions);
      final List<TransactionReceipt> localReceipts = blockDataGenerator.receipts(localBlock);
      localBlockchain.appendBlock(localBlock, localReceipts);

      final BlockDataGenerator.BlockOptions remoteOptions =
          new BlockDataGenerator.BlockOptions()
              .setDifficulty(UInt256.ONE) // differentiator
              .setBlockNumber(i)
              .setParentHash(remoteBlockchain.getBlockHashByNumber(i - 1).get());
      final Block remoteBlock = blockDataGenerator.block(remoteOptions);
      final List<TransactionReceipt> remoteReceipts = blockDataGenerator.receipts(remoteBlock);
      remoteBlockchain.appendBlock(remoteBlock, remoteReceipts);
    }

    // Create a list of headers...
    List<BlockHeader> headers = new ArrayList<>();
    for (long i = 0L; i <= remoteBlockchain.getChainHeadBlockNumber(); i++) {
      headers.add(remoteBlockchain.getBlockHeader(i).get());
    }

    OptionalInt maybeAncestorNumber =
        BlockchainUtil.findHighestKnownBlockIndex(localBlockchain, headers, true);

    assertThat(maybeAncestorNumber.getAsInt()).isEqualTo(Math.toIntExact(commonHeader.getNumber()));
  }

  @Test
  public void searchesDescending() {
    BlockHeader commonHeader = genesisBlock.getHeader();
    for (long i = 1; i <= commonAncestorHeight; i++) {
      commonHeader = localBlockchain.getBlockHeader(i).get();
      final List<TransactionReceipt> receipts =
          localBlockchain.getTxReceipts(commonHeader.getHash()).get();
      final BlockBody commonBody = localBlockchain.getBlockBody(commonHeader.getHash()).get();
      remoteBlockchain.appendBlock(new Block(commonHeader, commonBody), receipts);
    }

    // Remaining blocks are disparate...
    for (long i = commonAncestorHeight + 1L; i <= chainHeight; i++) {
      final BlockDataGenerator.BlockOptions localOptions =
          new BlockDataGenerator.BlockOptions()
              .setBlockNumber(i)
              .setParentHash(localBlockchain.getBlockHashByNumber(i - 1).get());
      final Block localBlock = blockDataGenerator.block(localOptions);
      final List<TransactionReceipt> localReceipts = blockDataGenerator.receipts(localBlock);
      localBlockchain.appendBlock(localBlock, localReceipts);

      final BlockDataGenerator.BlockOptions remoteOptions =
          new BlockDataGenerator.BlockOptions()
              .setDifficulty(UInt256.ONE) // differentiator
              .setBlockNumber(i)
              .setParentHash(remoteBlockchain.getBlockHashByNumber(i - 1).get());
      final Block remoteBlock = blockDataGenerator.block(remoteOptions);
      final List<TransactionReceipt> remoteReceipts = blockDataGenerator.receipts(remoteBlock);
      remoteBlockchain.appendBlock(remoteBlock, remoteReceipts);
    }

    // Create a list of headers...
    List<BlockHeader> headers = new ArrayList<>();
    for (long i = 0L; i <= remoteBlockchain.getChainHeadBlockNumber(); i++) {
      headers.add(remoteBlockchain.getBlockHeader(i).get());
    }
    Collections.reverse(headers);

    OptionalInt maybeAncestorNumber =
        BlockchainUtil.findHighestKnownBlockIndex(localBlockchain, headers, false);

    assertThat(maybeAncestorNumber.getAsInt())
        .isEqualTo(Math.toIntExact(chainHeight - commonHeader.getNumber()));
  }
}
