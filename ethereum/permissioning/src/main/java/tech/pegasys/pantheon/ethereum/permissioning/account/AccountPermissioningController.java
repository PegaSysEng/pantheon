package tech.pegasys.pantheon.ethereum.permissioning.account;

import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.permissioning.AccountLocalConfigPermissioningController;
import tech.pegasys.pantheon.ethereum.permissioning.TransactionSmartContractPermissioningController;

public class AccountPermissioningController {

  //TODO should we rename the classes?
  private final AccountLocalConfigPermissioningController accountLocalConfigPermissioningController;
  private final TransactionSmartContractPermissioningController transactionSmartContractPermissioningController;

  public AccountPermissioningController(
      final AccountLocalConfigPermissioningController accountLocalConfigPermissioningController,
      final TransactionSmartContractPermissioningController transactionSmartContractPermissioningController) {
    this.accountLocalConfigPermissioningController = accountLocalConfigPermissioningController;
    this.transactionSmartContractPermissioningController = transactionSmartContractPermissioningController;
  }

  boolean isPermittedGlobally(final Transaction transaction) {
    return accountLocalConfigPermissioningController.isPermitted(transaction)
        && transactionSmartContractPermissioningController.isPermitted(transaction);
  }

  boolean isPermittedOnChainOnly(final Transaction transaction) {
    return transactionSmartContractPermissioningController.isPermitted(transaction);
  }

}
