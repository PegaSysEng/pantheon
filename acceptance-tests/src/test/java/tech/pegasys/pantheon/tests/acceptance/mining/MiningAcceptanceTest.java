package tech.pegasys.pantheon.tests.acceptance.mining;

import static org.web3j.utils.Convert.Unit.ETHER;
import static tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNodeConfig.pantheonMinerNode;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import org.junit.Before;
import org.junit.Test;

public class MiningAcceptanceTest extends AcceptanceTestBase {

  private PantheonNode minerNode;

  @Before
  public void setUp() throws Exception {
    minerNode = cluster.create(pantheonMinerNode("miner1"));
    cluster.start(minerNode);
  }

  @Test
  public void shouldMineTransactions() {
    final Account sender = accounts.createAccount("account1", "50", ETHER, minerNode);
    final Account receiver = accounts.createAccount("account2", "0", ETHER, minerNode);
    cluster.awaitPropagation(sender, 50);

    minerNode.execute(transactions.createIncrementalTransfers(sender, receiver, 1));
    cluster.awaitPropagation(receiver, 1);

    minerNode.execute(transactions.createIncrementalTransfers(sender, receiver, 2));
    cluster.awaitPropagation(receiver, 3);

    minerNode.execute(transactions.createIncrementalTransfers(sender, receiver, 3));
    cluster.awaitPropagation(receiver, 6);

    minerNode.execute(transactions.createIncrementalTransfers(sender, receiver, 4));
    cluster.awaitPropagation(receiver, 10);

    minerNode.execute(transactions.createIncrementalTransfers(sender, receiver, 5));
    cluster.awaitPropagation(receiver, 15);
  }
}
