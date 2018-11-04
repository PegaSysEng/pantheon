package tech.pegasys.pantheon.tests.acceptance.jsonrpc;

import static tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNodeConfig.pantheonMinerNode;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import tech.pegasys.pantheon.ethereum.core.Hash;

import org.junit.Before;
import org.junit.Test;
import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

public class EthGetTransactionReceiptAcceptanceTest extends AcceptanceTestBase {

  private PantheonNode minerNode;
  private Account sender;
  private Account recipient;

  @Before
  public void setUp() throws Exception {
    sender = accounts.createAccount("sender");
    recipient = accounts.createAccount("recipient");
    minerNode = cluster.create(pantheonMinerNode("node"));
    cluster.start(minerNode);
    minerNode.execute(transactions.createTransfer(sender, 10));
    cluster.verify(sender.balanceEquals(10));
  }

  @Test
  public void mustCorrectlyQueryTransactionReceiptFromContractCreationTransaction() {
    final Hash transactionHash
        = minerNode.execute(transactions.createTransfer(recipient, 5));
    cluster.verify(recipient.balanceEquals(5));
    final Optional<TransactionReceipt> response
        = minerNode.execute(eth.getTransactionReceipt(transactionHash.toString()));
    assertThat(response.isPresent()).isTrue();
  }

  @Test
  public void mustCorrectlyQueryTransactionReceiptFromNormalTransaction() {
    final Hash transactionHash
        = minerNode.execute(transactions.createTransfer(sender, recipient, 5));
    cluster.verify(recipient.balanceEquals(10));
    cluster.verify(sender.balanceEquals(5));
    final Optional<TransactionReceipt> response
        = minerNode.execute(eth.getTransactionReceipt(transactionHash.toString()));
    assertThat(response.isPresent()).isTrue();
  }

  @Test
  public void mustReturnNullWhenTransactionDoesNotExist() {
    final Optional<TransactionReceipt> response
        = minerNode.execute(eth.getTransactionReceipt(
            "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238"));
    assertThat(response.isPresent()).isFalse();
  }

}
