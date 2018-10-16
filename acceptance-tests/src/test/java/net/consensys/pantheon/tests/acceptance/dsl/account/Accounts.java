package net.consensys.pantheon.tests.acceptance.dsl.account;

import static net.consensys.pantheon.tests.acceptance.dsl.WaitUtils.waitFor;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.web3j.utils.Convert.toWei;

import net.consensys.pantheon.tests.acceptance.dsl.node.PantheonNode;
import org.apache.logging.log4j.Logger;
import org.web3j.utils.Convert.Unit;

public class Accounts {

  private static final Logger LOG = getLogger();

  private final Account richBenefactorOne;
  private final Account richBenefactorTwo;

  public Accounts() {
    richBenefactorOne =
        Account.fromPrivateKey(
            "Rich Benefactor One",
            "8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63");
    richBenefactorTwo =
        Account.fromPrivateKey(
            "Rich Benefactor Two",
            "c87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3");
  }

  public Account getSecondaryBenefactor() {
    return richBenefactorTwo;
  }

  public Account getPrimaryBenefactor() {
    return richBenefactorOne;
  }

  public Account createAccount(
      final String accountName,
      final String initialBalance,
      final Unit initialBalanceUnit,
      final PantheonNode createOnNode) {
    final Account account = Account.create(accountName);
    createOnNode.transferFunds(richBenefactorOne, account, initialBalance, initialBalanceUnit);

    return account;
  }

  public Account createAccount(final String accountName) {
    return Account.create(accountName);
  }

  public void waitForAccountBalance(
      final Account account, final int etherAmount, final PantheonNode node) {
    waitForAccountBalance(account, String.valueOf(etherAmount), Unit.ETHER, node);
  }

  public void waitForAccountBalance(
      final Account account,
      final String expectedBalance,
      final Unit balanceUnit,
      final PantheonNode node) {
    LOG.info(
        "Waiting for {} to have a balance of {} {} on node {}",
        account.getName(),
        expectedBalance,
        balanceUnit,
        node.getName());

    waitFor(
        () ->
            assertThat(node.getAccountBalance(account))
                .isEqualTo(toWei(expectedBalance, balanceUnit).toBigIntegerExact()));
  }
}
