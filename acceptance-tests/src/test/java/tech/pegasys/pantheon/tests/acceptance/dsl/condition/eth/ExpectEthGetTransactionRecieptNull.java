package tech.pegasys.pantheon.tests.acceptance.dsl.condition.eth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eth.EthGetTransactionReceiptTransaction;

public class ExpectEthGetTransactionRecieptNull implements Condition {

  private final EthGetTransactionReceiptTransaction transaction;

  public ExpectEthGetTransactionRecieptNull(final EthGetTransactionReceiptTransaction transaction) {
    this.transaction = transaction;
  }

  @Override
  public void verify(final Node node) {
    final Optional<TransactionReceipt> response = node.execute(transaction);
    assertThat(response.isPresent()).isFalse();
  }

}
