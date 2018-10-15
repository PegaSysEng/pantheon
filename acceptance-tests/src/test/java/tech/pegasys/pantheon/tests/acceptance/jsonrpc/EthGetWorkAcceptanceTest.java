package tech.pegasys.pantheon.tests.acceptance.jsonrpc;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNodeConfig.pantheonMinerNode;
import static tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNodeConfig.pantheonNode;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import org.junit.Before;
import org.junit.Test;
import org.web3j.protocol.core.methods.response.EthGetWork;

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
    EthGetWork response = minerNode.web3j().ethGetWork().send();
    assertThat(response.getSeedHashForDag()).isNotBlank();
  }

  @Test
  public void shouldReturnErrorResponseWhenNotMining() throws Exception {
    EthGetWork response = fullNode.web3j().ethGetWork().send();
    assertThat(response.hasError()).isTrue();
  }
}
