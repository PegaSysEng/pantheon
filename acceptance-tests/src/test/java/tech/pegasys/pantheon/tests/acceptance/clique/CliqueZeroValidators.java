package tech.pegasys.pantheon.tests.acceptance.clique;

import java.io.IOException;
import org.junit.Test;
import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

public class CliqueZeroValidators extends AcceptanceTestBase {

  @Test
  public void startingWithZeroValidatorsProducesValidCluster() throws IOException {
    final String[] validators = {""};
    final PantheonNode node1 = pantheon.createCliqueNodeWithValidators("node1", validators);
    final PantheonNode node2 = pantheon.createCliqueNodeWithValidators("node2", validators);
    cluster.start(node1, node2);

    node1.verify(net.awaitPeerCount(1));
  }

}
