package tech.pegasys.pantheon.ethereum.jsonrpc.internal.filter;

import java.util.ArrayList;
import java.util.List;
import tech.pegasys.pantheon.ethereum.core.Hash;

/** Tracks new pending transactions that have arrived in the transaction pool */
class PendingTransactionFilter {

  private final List<Hash> transactionHashes = new ArrayList<>();

  PendingTransactionFilter() {}

  void addTransactionHash(final Hash hash) {
    transactionHashes.add(hash);
  }

  List<Hash> transactionHashes() {
    return transactionHashes;
  }

  void clearTransactionHashes() {
    transactionHashes.clear();
  }
}
