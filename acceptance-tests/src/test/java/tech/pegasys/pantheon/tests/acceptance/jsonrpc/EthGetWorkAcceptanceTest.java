package tech.pegasys.pantheon.tests.acceptance.jsonrpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNodeConfig.pantheonMinerNode;
import static tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNodeConfig.pantheonNode;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import org.junit.Before;
import org.junit.Test;
import org.web3j.protocol.exceptions.ClientConnectionException;

public class EthGetWorkAcceptanceTest extends AcceptanceTestBase {

  private PantheonNode minerNode;
  private PantheonNode fullNode;

  @Before
  public void setUp() throws Exception {
    minerNode = cluster.create(pantheonMinerNode("node1"));
    fullNode = cluster.create(pantheonNode("node2"));
    cluster.start(minerNode, fullNode);
  }

  @Test
  public void shouldReturnSuccessResponseWhenMining() throws Exception {
    String[] response = minerNode.eth().getWork();
    assertThat(response).hasSize(3);
    assertThat(response).doesNotContainNull();
  }

  @Test
  public void shouldReturnErrorResponseWhenNotMining() {
    Throwable thrown = catchThrowable(() -> fullNode.eth().getWork());
    assertThat(thrown).isInstanceOf(ClientConnectionException.class);
    assertThat(thrown.getMessage())
        .contains(
            "\"error\" : {\n"
                + "    \"code\" : -32000,\n"
                + "    \"message\" : \"no mining work available yet\"\n"
                + "  }");
  }
}
