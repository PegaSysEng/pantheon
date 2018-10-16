package tech.pegasys.pantheon.ethereum.mainnet;

import tech.pegasys.pantheon.util.uint.UInt256;

public class EthHashSolverInputs {
  private final UInt256 target;
  private final byte[] prePowHash;
  private final long blockNumber;

  public EthHashSolverInputs(
      final UInt256 target, final byte[] prePowHash, final long blockNumber) {
    this.target = target;
    this.prePowHash = prePowHash;
    this.blockNumber = blockNumber;
  }

  public UInt256 getTarget() {
    return target;
  }

  public byte[] getPrePowHash() {
    return prePowHash;
  }

  public long getBlockNumber() {
    return blockNumber;
  }
}
