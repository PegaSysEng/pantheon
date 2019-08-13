package tech.pegasys.pantheon.ethereum.privacy.markertransaction;

import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.privacy.PrivateTransaction;

public abstract class PrivateMarkerTransactionFactory {

  private final Address privacyPrecompileAddress;

  public PrivateMarkerTransactionFactory(
      Address privacyPrecompileAddress) {
    this.privacyPrecompileAddress = privacyPrecompileAddress;
  }

  protected Address getPrivacyPrecompileAddress() {
    return privacyPrecompileAddress;
  }

  public abstract Transaction create(final String transactionEnclaveKey,
      final PrivateTransaction privateTransaction);


}
