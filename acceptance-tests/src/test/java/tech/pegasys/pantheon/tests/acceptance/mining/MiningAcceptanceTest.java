package tech.pegasys.pantheon.tests.acceptance.mining;

import static tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNodeConfig.pantheonMinerNode;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class MiningAcceptanceTest extends AcceptanceTestBase {

  private PantheonNode minerNode;

  @Before
  public void setUp() throws Exception {
    minerNode = cluster.create(pantheonMinerNode("miner1"));
    cluster.start(minerNode);
  }

  @Test
  public void shouldMineTransactions() {
    final Account sender = accounts.createAccount("account1");
    final Account receiver = accounts.createAccount("account2");
    minerNode.execute(transactions.createTransfer(sender, 50));
    cluster.verify(sender.balanceEquals(50));

    minerNode.execute(transactions.createIncrementalTransfers(sender, receiver, 1));
    cluster.verify(receiver.balanceEquals(1));

    minerNode.execute(transactions.createIncrementalTransfers(sender, receiver, 2));
    cluster.verify(receiver.balanceEquals(3));

    minerNode.execute(transactions.createIncrementalTransfers(sender, receiver, 3));
    cluster.verify(receiver.balanceEquals(6));

    minerNode.execute(transactions.createIncrementalTransfers(sender, receiver, 4));
    cluster.verify(receiver.balanceEquals(10));

    minerNode.execute(transactions.createIncrementalTransfers(sender, receiver, 5));
    cluster.verify(receiver.balanceEquals(15));
  }
}
