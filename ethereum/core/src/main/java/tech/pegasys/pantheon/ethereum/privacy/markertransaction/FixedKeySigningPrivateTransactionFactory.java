package tech.pegasys.pantheon.ethereum.privacy.markertransaction;

import java.util.OptionalLong;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.privacy.PrivateTransaction;
import tech.pegasys.pantheon.util.bytes.BytesValues;

public class FixedKeySigningPrivateTransactionFactory extends PrivateTransactionFactory {

  private final Blockchain blockchain;
  private final PendingTransactions pendingTransactions;
  private final KeyPair signingKey;
  private final Address sender;

  public FixedKeySigningPrivateTransactionFactory(
      final Blockchain blockchain, final Address privacyPrecompileAddress,
      final KeyPair signingKey,
      final Address sender) {
    super(privacyPrecompileAddress);
    this.signingKey = signingKey;
    this.sender = sender;
    this.blockchain = blockchain;
  }


  @Override
  public Transaction create(String transactionEnclaveKey,
      PrivateTransaction privateTransaction) {
    return Transaction.builder()
        .nonce(0)
        .gasPrice(privateTransaction.getGasPrice())
        .gasLimit(privateTransaction.getGasLimit())
        .to(getPrivacyPrecompileAddress())
        .value(privateTransaction.getValue())
        .payload(BytesValues.fromBase64(transactionEnclaveKey))
        .signAndBuild(signingKey);
  }

  protected long getNonce(final Address address) {
    final OptionalLong pendingNonce = pendingTransactions.getNextNonceForSender(address);
    return pendingNonce.orElseGet(
        () -> blockchain.getTransactionCount(address, blockchain.headBlockNumber()));
  }
}
