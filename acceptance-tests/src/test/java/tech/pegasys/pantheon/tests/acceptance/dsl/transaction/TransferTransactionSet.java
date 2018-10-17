package tech.pegasys.pantheon.tests.acceptance.dsl.transaction;

import tech.pegasys.pantheon.ethereum.core.Hash;

import java.util.ArrayList;
import java.util.List;

import org.web3j.protocol.Web3j;

public class TransferTransactionSet implements Transaction<List<Hash>> {

  private final List<TransferTransaction> transactions;

  public TransferTransactionSet(final List<TransferTransaction> transactions) {
    this.transactions = transactions;
  }

  @Override
  public List<Hash> execute(final Web3j node) {
    final List<Hash> hashes = new ArrayList<>();

    for (final TransferTransaction transaction : transactions) {
      hashes.add(transaction.execute(node));
    }

    return hashes;
  }
}
