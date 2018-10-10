package net.consensys.pantheon.tests.acceptance.dsl.transaction;

import static org.web3j.utils.Numeric.toHexString;

import net.consensys.pantheon.ethereum.core.Hash;
import net.consensys.pantheon.tests.acceptance.dsl.account.Account;

import java.io.IOException;
import java.math.BigInteger;

import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

public class TransferTransaction implements Transaction<Hash> {

  private static final BigInteger MINIMUM_GAS_PRICE = BigInteger.valueOf(1000);
  private static final BigInteger TRANSFER_GAS_COST = BigInteger.valueOf(21000);

  private final Account sender;
  private final Account recipient;
  private final String amount;
  private final Unit unit;

  public TransferTransaction(
      final Account sender, final Account recipient, final String amount, final Unit unit) {
    this.sender = sender;
    this.recipient = recipient;
    this.amount = amount;
    this.unit = unit;
  }

  @Override
  public Hash execute(final Web3j node) {
    final RawTransaction transaction =
        RawTransaction.createEtherTransaction(
            sender.getNextNonce(),
            MINIMUM_GAS_PRICE,
            TRANSFER_GAS_COST,
            recipient.getAddress(),
            Convert.toWei(amount, unit).toBigIntegerExact());
    final String signedTransactionData =
        toHexString(TransactionEncoder.signMessage(transaction, sender.web3jCredentials()));
    try {
      return Hash.fromHexString(
          node.ethSendRawTransaction(signedTransactionData).send().getTransactionHash());
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
