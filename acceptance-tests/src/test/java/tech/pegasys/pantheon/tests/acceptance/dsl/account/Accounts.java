package tech.pegasys.pantheon.tests.acceptance.dsl.account;

public class Accounts {

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

  public Account createAccount(final String accountName) {
    return Account.create(accountName);
  }
}
