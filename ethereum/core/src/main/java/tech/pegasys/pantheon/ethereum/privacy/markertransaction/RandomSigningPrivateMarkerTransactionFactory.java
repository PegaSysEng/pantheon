package tech.pegasys.pantheon.ethereum.privacy.markertransaction;

import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.privacy.PrivateTransaction;
import tech.pegasys.pantheon.util.bytes.BytesValues;

public class RandomSigningPrivateMarkerTransactionFactory extends PrivateMarkerTransactionFactory {

  public RandomSigningPrivateMarkerTransactionFactory(Address privacyPrecompileAddress) {
    super(privacyPrecompileAddress);
  }

  public Transaction create(final String transactionEnclaveKey,
      final PrivateTransaction privateTransaction) {
    final KeyPair keyPair = KeyPair.generate();

    return Transaction.builder()
        .nonce(0)
        .gasPrice(privateTransaction.getGasPrice())
        .gasLimit(privateTransaction.getGasLimit())
        .to(getPrivacyPrecompileAddress())
        .value(privateTransaction.getValue())
        .payload(BytesValues.fromBase64(transactionEnclaveKey))
        .signAndBuild(keyPair);
  }
}
