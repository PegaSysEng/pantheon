package tech.pegasys.pantheon.ethereum.mainnet;

import static tech.pegasys.pantheon.ethereum.mainnet.EthHash.EPOCH_LENGTH;
import static tech.pegasys.pantheon.ethereum.mainnet.EthHash.KECCAK_256;

import java.security.DigestException;
import java.security.MessageDigest;

public class DirectAcyclicGraphSeed {

  public static byte[] dagSeed(final long block) {
    final byte[] seed = new byte[32];
    if (Long.compareUnsigned(block, EPOCH_LENGTH) >= 0) {
      final MessageDigest keccak256 = KECCAK_256.get();
      for (int i = 0; i < Long.divideUnsigned(block, EPOCH_LENGTH); ++i) {
        keccak256.update(seed);
        try {
          keccak256.digest(seed, 0, seed.length);
        } catch (final DigestException ex) {
          throw new IllegalStateException(ex);
        }
      }
    }
    return seed;
  }
}
