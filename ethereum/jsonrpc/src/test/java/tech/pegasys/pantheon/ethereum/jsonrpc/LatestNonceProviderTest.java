package tech.pegasys.pantheon.ethereum.jsonrpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.OptionalLong;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.eth.transactions.PendingTransactions;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;

@RunWith(MockitoJUnitRunner.class)
public class LatestNonceProviderTest {

  private final Address senderAdress = Address.fromHexString("1");

  private PendingTransactions pendingTransactions = mock(PendingTransactions.class);
  private BlockchainQueries blockchainQueries = mock(BlockchainQueries.class);
  private LatestNonceProvider nonceProvider =
      new LatestNonceProvider(blockchainQueries, pendingTransactions);

  @Test
  public void nextNonceUsesTxPool() {
    final long highestNonceInPendingTransactions = 123;
    when(pendingTransactions.getNextNonceForSender(senderAdress))
        .thenReturn(OptionalLong.of(highestNonceInPendingTransactions));
    assertThat(nonceProvider.getNonce(senderAdress)).isEqualTo(highestNonceInPendingTransactions);
  }

  @Test
  public void nextNonceIsTakenFromBlockchainIfNoPendingTransactionResponse() {
    final long headBlockNumber = 8;
    final long nonceInBLockchain = 56;
    when(pendingTransactions.getNextNonceForSender(senderAdress)).thenReturn(OptionalLong.empty());
    when(blockchainQueries.headBlockNumber()).thenReturn(headBlockNumber);
    when(blockchainQueries.getTransactionCount(senderAdress, headBlockNumber))
        .thenReturn(nonceInBLockchain);
    assertThat(nonceProvider.getNonce(senderAdress)).isEqualTo(nonceInBLockchain);
  }

}