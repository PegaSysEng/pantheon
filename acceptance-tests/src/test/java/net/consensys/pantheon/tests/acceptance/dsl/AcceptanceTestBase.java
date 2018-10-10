package net.consensys.pantheon.tests.acceptance.dsl;

import net.consensys.pantheon.tests.acceptance.dsl.account.Accounts;
import net.consensys.pantheon.tests.acceptance.dsl.node.Cluster;
import net.consensys.pantheon.tests.acceptance.dsl.transaction.Transactions;

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
