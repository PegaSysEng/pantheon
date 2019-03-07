package tech.pegasys.pantheon.ethereum.eth.manager.task;

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RetryingGetNodeDataFromPeerTask
    extends AbstractRetryingPeerTask<Map<Hash, BytesValue>> {

  private final EthContext ethContext;
  private final Set<Hash> hashes;
  private final long pivotBlockNumber;
  private final MetricsSystem metricsSystem;

  private RetryingGetNodeDataFromPeerTask(
      final EthContext ethContext,
      final Collection<Hash> hashes,
      final long pivotBlockNumber,
      final MetricsSystem metricsSystem) {
    super(ethContext, 3, data -> false, metricsSystem);
    this.ethContext = ethContext;
    this.hashes = new HashSet<>(hashes);
    this.pivotBlockNumber = pivotBlockNumber;
    this.metricsSystem = metricsSystem;
  }

  public static RetryingGetNodeDataFromPeerTask forHashes(
      final EthContext ethContext,
      final Collection<Hash> hashes,
      final long pivotBlockNumber,
      final MetricsSystem metricsSystem) {
    return new RetryingGetNodeDataFromPeerTask(ethContext, hashes, pivotBlockNumber, metricsSystem);
  }

  @Override
  protected CompletableFuture<Map<Hash, BytesValue>> executePeerTask(
      final Optional<EthPeer> assignedPeer) {
    final GetNodeDataFromPeerTask task =
        GetNodeDataFromPeerTask.forHashes(ethContext, hashes, pivotBlockNumber, metricsSystem);
    assignedPeer.ifPresent(task::assignPeer);
    return executeSubTask(task::run)
        .thenApply(
            peerResult -> {
              result.get().complete(peerResult.getResult());
              return peerResult.getResult();
            });
  }
}
