package tech.pegasys.pantheon.ethereum.worldstate;

public class PruningConfiguration {

  private final long retentionPeriodInBlocks;
  private final long transientForkOutlivingPeriod;

  public PruningConfiguration(
      final long transientForkOutlivingPeriod, final long retentionPeriodInBlocks) {
    this.transientForkOutlivingPeriod = transientForkOutlivingPeriod;
    this.retentionPeriodInBlocks = retentionPeriodInBlocks;
  }

  long getRetentionPeriod() {
    return retentionPeriodInBlocks;
  }

  long getTransientForkOutlivingPeriod() {
    return transientForkOutlivingPeriod;
  }
}
