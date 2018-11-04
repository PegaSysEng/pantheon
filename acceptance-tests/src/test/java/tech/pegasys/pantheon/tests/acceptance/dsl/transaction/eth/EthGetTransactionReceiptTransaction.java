package tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eth;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Optional;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;

public class EthGetTransactionReceiptTransaction implements
    Transaction<Optional<TransactionReceipt>> {

  private final String input;

  EthGetTransactionReceiptTransaction(final String input) {this.input = input; }

  @Override public Optional<TransactionReceipt> execute(final Web3j node) {
    try {
      final EthGetTransactionReceipt result = node.ethGetTransactionReceipt(input).send();
      assertThat(result.hasError()).isFalse();
      return result.getTransactionReceipt();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

}
