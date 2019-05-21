package tech.pegasys.pantheon.ethereum.permissioning.account;

import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.permissioning.AccountLocalConfigPermissioningController;
import tech.pegasys.pantheon.ethereum.permissioning.TransactionSmartContractPermissioningController;

public class AccountPermissioningController {

  private final AccountLocalConfigPermissioningController accountLocalConfigPermissioningController;
  private final TransactionSmartContractPermissioningController transactionSmartContractPermissioningController;

  public AccountPermissioningController(
      final AccountLocalConfigPermissioningController accountLocalConfigPermissioningController,
      final TransactionSmartContractPermissioningController transactionSmartContractPermissioningController) {
    this.accountLocalConfigPermissioningController = accountLocalConfigPermissioningController;
    this.transactionSmartContractPermissioningController = transactionSmartContractPermissioningController;
  }

  public boolean isPermitted(final Transaction transaction, final boolean includeOnChainCheck) {
    if (includeOnChainCheck) {
      return accountLocalConfigPermissioningController.isPermitted(transaction)
          && transactionSmartContractPermissioningController.isPermitted(transaction);
    } else {
      return accountLocalConfigPermissioningController.isPermitted(transaction);
    }
  }

}
