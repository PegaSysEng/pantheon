package tech.pegasys.pantheon.tests.acceptance.clique;

import static org.web3j.utils.Convert.Unit.ETHER;
import static tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNodeConfig.pantheonCliqueNode;

import org.junit.Before;
import org.junit.Test;
import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

public class CliqueMiningAcceptanceTest extends AcceptanceTestBase {
  private PantheonNode minerNode;

  @Before
  public void setUp() throws Exception {
    minerNode = cluster.create(pantheonCliqueNode("miner1", "clique/one_node_poa.json",
        "clique/node0_key"));
    cluster.start(minerNode);
  }

  @Test
  public void shouldMineTransactions() {
    final Account fromAccount = accounts.createAccount("account1", "50", ETHER, minerNode);
    final Account toAccount = accounts.createAccount("account2", "0", ETHER, minerNode);
    accounts.waitForAccountBalance(fromAccount, 50, minerNode);

    accounts.incrementalTransfer(fromAccount, toAccount, 1, minerNode);
    accounts.waitForAccountBalance(toAccount, 1, minerNode);

    accounts.incrementalTransfer(fromAccount, toAccount, 2, minerNode);
    accounts.waitForAccountBalance(toAccount, 3, minerNode);

    accounts.incrementalTransfer(fromAccount, toAccount, 3, minerNode);
    accounts.waitForAccountBalance(toAccount, 6, minerNode);

    accounts.incrementalTransfer(fromAccount, toAccount, 4, minerNode);
    accounts.waitForAccountBalance(toAccount, 10, minerNode);

    accounts.incrementalTransfer(fromAccount, toAccount, 5, minerNode);
    accounts.waitForAccountBalance(toAccount, 15, minerNode);
  }
}
