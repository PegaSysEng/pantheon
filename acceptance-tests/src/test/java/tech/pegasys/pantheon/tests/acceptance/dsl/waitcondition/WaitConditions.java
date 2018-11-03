package tech.pegasys.pantheon.tests.acceptance.dsl.waitcondition;

import java.math.BigInteger;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.blockchain.ExpectBlockNumber;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eth.EthTransactions;

public class WaitConditions {
  private EthTransactions eth;

  public WaitConditions(final EthTransactions eth) {
    this.eth = eth;
  }

  public WaitCondition chainHeadHasProgressed(final PantheonNode node, final int blocksAheadOfLatest) {
    final BigInteger futureBlock =
        node.execute(eth.blockNumber()).add(BigInteger.valueOf(blocksAheadOfLatest));
    return new ExpectBlockNumber(eth, futureBlock)::verify;
  }
}
