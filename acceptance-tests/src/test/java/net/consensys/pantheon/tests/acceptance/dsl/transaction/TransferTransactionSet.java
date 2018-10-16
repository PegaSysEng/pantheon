package net.consensys.pantheon.tests.acceptance.dsl.transaction;

import java.util.ArrayList;
import java.util.List;

import net.consensys.pantheon.ethereum.core.Hash;
import org.web3j.protocol.Web3j;

public class TransferTransactionSet implements Transaction<List<Hash>> {

  private final List<TransferTransaction> transactions;

  public TransferTransactionSet(final List<TransferTransaction> transactions) {
    this.transactions = transactions;
  }

  /**
   * Transfer funds in separate transactions (1 eth increments). This is a strategy to increase the
   * total of transactions.
   *
   * @param node where the transactions will be executed.
   * @return always <code>null</code>.
   */
  @Override
  public List<Hash> execute(final Web3j node) {
    final List<Hash> hashes = new ArrayList<>();

    for (final TransferTransaction transaction : transactions) {
      hashes.add(transaction.execute(node));
    }

    return hashes;
  }
}
