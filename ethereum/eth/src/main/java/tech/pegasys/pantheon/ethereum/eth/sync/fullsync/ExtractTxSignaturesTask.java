package tech.pegasys.pantheon.ethereum.eth.sync.fullsync;

import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Transaction;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class ExtractTxSignaturesTask implements Function<List<Block>, Stream<Block>> {

  @Override
  public Stream<Block> apply(final List<Block> blocks) {
    return blocks.stream()
        .map(this::extractSignatures);
  }

  private Block extractSignatures(final Block block) {
    block.getBody().getTransactions().forEach(Transaction::getSender);
    return block;
  }
}
