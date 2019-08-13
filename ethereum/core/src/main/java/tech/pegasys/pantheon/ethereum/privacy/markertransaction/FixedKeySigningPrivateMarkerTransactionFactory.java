package tech.pegasys.pantheon.ethereum.privacy.markertransaction;

import java.util.OptionalLong;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.privacy.PrivateTransaction;
import tech.pegasys.pantheon.util.bytes.BytesValues;

public class FixedKeySigningPrivateMarkerTransactionFactory extends PrivateMarkerTransactionFactory {

  @FunctionalInterface
  public interface NonceProvider {
    long getNonce(final Address address);
  }

  private final NonceProvider nonceProvider;
  private final KeyPair signingKey;
  private final Address sender;

  public FixedKeySigningPrivateMarkerTransactionFactory(
      final Address privacyPrecompileAddress,
      final NonceProvider nonceProvider,
      final KeyPair signingKey) {
    super(privacyPrecompileAddress);
    this.nonceProvider = nonceProvider;
    this.signingKey = signingKey;
    this.sender = Util.publicKeyToAddress(signingKey.getPublicKey());
  }

  @Override
  public Transaction create(final String transactionEnclaveKey,
      final PrivateTransaction privateTransaction) {
    return Transaction.builder()
        .nonce(nonceProvider.getNonce(sender))
        .gasPrice(privateTransaction.getGasPrice())
        .gasLimit(privateTransaction.getGasLimit())
        .to(getPrivacyPrecompileAddress())
        .value(privateTransaction.getValue())
        .payload(BytesValues.fromBase64(transactionEnclaveKey))
        .signAndBuild(signingKey);
  }
}
