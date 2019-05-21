package tech.pegasys.pantheon.ethereum.mainnet;

public class TransactionValidationParams {

  private final boolean allowFutureNonce;
  private final boolean stateChange;

  private TransactionValidationParams(final boolean allowFutureNonce,
      final boolean stateChange) {
    this.allowFutureNonce = allowFutureNonce;
    this.stateChange = stateChange;
  }

  public boolean isAllowFutureNonce() {
    return allowFutureNonce;
  }

  public boolean isStateChange() {
    return stateChange;
  }

  public static class Builder {

    private boolean allowFutureNonce = false;
    private boolean stateChange = false;

    public Builder  allowFutureNonce(final boolean allowFutureNonce) {
      this.allowFutureNonce = allowFutureNonce;
      return this;
    }

    public Builder stateChange(final boolean stateChange) {
      this.stateChange = stateChange;
      return this;
    }

    public TransactionValidationParams build() {
      return new TransactionValidationParams(allowFutureNonce, stateChange);
    }
  }
}
