package tech.pegasys.pantheon.tests.acceptance.ibft2;

import java.io.IOException;
import org.junit.Test;
import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

public class Ibft2ZeroValidators extends AcceptanceTestBase {

  @Test
  public void zeroValidatorsFormValidCluster() throws IOException {
    final String[] validators = {};
    final PantheonNode node1 =
        pantheon.createIbft2NodeWithValidators("node1", validators);
    final PantheonNode node2 =
        pantheon.createIbft2NodeWithValidators("node2", validators);

    cluster.start(node1, node2);

    node1.verify(net.awaitPeerCount(1));
  }

}
