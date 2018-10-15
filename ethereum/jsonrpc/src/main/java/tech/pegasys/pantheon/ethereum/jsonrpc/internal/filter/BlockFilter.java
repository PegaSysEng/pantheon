package tech.pegasys.pantheon.ethereum.jsonrpc.internal.filter;

import java.util.ArrayList;
import java.util.List;
import tech.pegasys.pantheon.ethereum.core.Hash;

/** Tracks new blocks being added to the blockchain. */
class BlockFilter {

  private final List<Hash> blockHashes = new ArrayList<>();

  BlockFilter() {}

  void addBlockHash(final Hash hash) {
    blockHashes.add(hash);
  }

  List<Hash> blockHashes() {
    return blockHashes;
  }

  void clearBlockHashes() {
    blockHashes.clear();
  }
}
