package tech.pegasys.pantheon.tests.acceptance.dsl;

import tech.pegasys.pantheon.tests.acceptance.dsl.account.Accounts;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Cluster;

import org.junit.After;

public class AcceptanceTestBase {

  protected final Accounts accounts;
  protected final Cluster cluster;
  protected final Transactions transactions;
  protected final JsonRpc jsonRpc;

  protected AcceptanceTestBase() {
    accounts = new Accounts();
    cluster = new Cluster();
    transactions = new Transactions(accounts);
    jsonRpc = new JsonRpc(cluster);
  }

  @After
  public void tearDownAcceptanceTestBase() {
    cluster.close();
  }
}
