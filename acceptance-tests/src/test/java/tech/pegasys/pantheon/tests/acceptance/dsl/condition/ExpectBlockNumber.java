package tech.pegasys.pantheon.tests.acceptance.dsl.condition;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static tech.pegasys.pantheon.tests.acceptance.dsl.WaitUtils.waitFor;

import java.math.BigInteger;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;

public class ExpectBlockNumber implements Condition {
  private BigInteger blockNumber;

  public ExpectBlockNumber(final BigInteger blockNumber) {
    this.blockNumber = blockNumber;
  }

  @Override
  public void verify(final Node node) {
    waitFor(
        () -> assertThat(node.eth().blockNumber()).isEqualTo(blockNumber)
    );
  }
}
