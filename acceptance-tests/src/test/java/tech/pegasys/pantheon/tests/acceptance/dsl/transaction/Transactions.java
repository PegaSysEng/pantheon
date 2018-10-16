package tech.pegasys.pantheon.tests.acceptance.dsl.transaction;

import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;
import tech.pegasys.pantheon.tests.acceptance.dsl.account.Accounts;

import java.util.ArrayList;
import java.util.List;

import org.web3j.utils.Convert.Unit;

public class Transactions {

  private final Accounts accounts;

  public Transactions(final Accounts accounts) {
    this.accounts = accounts;
  }

  public TransferTransaction createTransfer(final Account recipient, final int amount) {
    return createTransfer(accounts.getPrimaryBenefactor(), recipient, amount);
  }

  public TransferTransaction createTransfer(
      final Account sender, final Account recipient, final int amount) {
    return new TransferTransaction(sender, recipient, String.valueOf(amount), Unit.ETHER);
  }

  public TransferTransactionSet createIncrementalTransfers(
      final Account sender, final Account recipient, final int etherAmount) {
    final List<TransferTransaction> transfers = new ArrayList<>();

    for (int i = 1; i <= etherAmount; i++) {
      transfers.add(new TransferTransaction(sender, recipient, "1", Unit.ETHER));
    }

    return new TransferTransactionSet(transfers);
  }
}
