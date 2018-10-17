package tech.pegasys.pantheon.ethereum.core;

/** Generic interface for a view over the accounts of the world state. */
public interface WorldView {
  WorldView EMPTY =
      new WorldView() {
        @Override
        public Account get(final Address address) {
          return null;
        }

        @Override
        public Account getOriginalAccount(final Address address) {
          return null;
        }
      };

  /**
   * Get an account provided it's address.
   *
   * @param address the address of the account to retrieve.
   * @return the {@link Account} corresponding to {@code address} or {@code null} if there is no
   *     such account.
   */
  Account get(Address address);

  /**
   * Retrieve the original account, prior to any modifications made in this transaction.
   *
   * @param address the address of the account.
   * @return the account {@code address} or {@code null} if the account does not exist.
   */
  Account getOriginalAccount(Address address);
}
