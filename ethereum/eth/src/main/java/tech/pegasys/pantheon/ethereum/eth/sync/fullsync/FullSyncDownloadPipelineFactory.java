package tech.pegasys.pantheon.ethereum.eth.sync.fullsync;

import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.ethereum.eth.sync.CheckpointHeaderFetcher;
import tech.pegasys.pantheon.ethereum.eth.sync.CheckpointHeaderValidationStep;
import tech.pegasys.pantheon.ethereum.eth.sync.CheckpointRangeSource;
import tech.pegasys.pantheon.ethereum.eth.sync.DownloadBodiesStep;
import tech.pegasys.pantheon.ethereum.eth.sync.DownloadHeadersStep;
import tech.pegasys.pantheon.ethereum.eth.sync.DownloadPipelineFactory;
import tech.pegasys.pantheon.ethereum.eth.sync.SynchronizerConfiguration;
import tech.pegasys.pantheon.ethereum.eth.sync.ValidationPolicy;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncTarget;
import tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.metrics.MetricCategory;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.services.pipeline.Pipeline;
import tech.pegasys.pantheon.services.pipeline.PipelineBuilder;

import java.time.Duration;
import java.util.Optional;

public class FullSyncDownloadPipelineFactory<C> implements DownloadPipelineFactory {

  private final SynchronizerConfiguration syncConfig;
  private final ProtocolSchedule<C> protocolSchedule;
  private final ProtocolContext<C> protocolContext;
  private final EthContext ethContext;
  private final MetricsSystem metricsSystem;
  private final ValidationPolicy detachedValidationPolicy =
      () -> HeaderValidationMode.DETACHED_ONLY;

  public FullSyncDownloadPipelineFactory(
      final SynchronizerConfiguration syncConfig,
      final ProtocolSchedule<C> protocolSchedule,
      final ProtocolContext<C> protocolContext,
      final EthContext ethContext,
      final MetricsSystem metricsSystem) {
    this.syncConfig = syncConfig;
    this.protocolSchedule = protocolSchedule;
    this.protocolContext = protocolContext;
    this.ethContext = ethContext;
    this.metricsSystem = metricsSystem;
  }

  @Override
  public Pipeline<?> createDownloadPipelineForSyncTarget(final SyncTarget target) {
    final int downloaderParallelism = syncConfig.downloaderParallelism();
    final int headerRequestSize = syncConfig.downloaderHeaderRequestSize();
    final int singleHeaderBufferSize = headerRequestSize * downloaderParallelism;
    final CheckpointRangeSource checkpointRangeSource =
        new CheckpointRangeSource(
            new CheckpointHeaderFetcher(
                syncConfig, protocolSchedule, ethContext, Optional.empty(), metricsSystem),
            this::shouldContinueDownloadingFromPeer,
            ethContext.getScheduler(),
            target.peer(),
            target.commonAncestor(),
            syncConfig.downloaderCheckpointTimeoutsPermitted(),
            Duration.ofSeconds(5));
    final DownloadHeadersStep<C> downloadHeadersStep =
        new DownloadHeadersStep<>(
            protocolSchedule, protocolContext, ethContext, detachedValidationPolicy, metricsSystem);
    final CheckpointHeaderValidationStep<C> validateHeadersJoinUpStep =
        new CheckpointHeaderValidationStep<>(
            protocolSchedule, protocolContext, detachedValidationPolicy);
    final DownloadBodiesStep<C> downloadBodiesStep =
        new DownloadBodiesStep<>(protocolSchedule, ethContext, metricsSystem);
    final ExtractTxSignaturesTask extractTxSignaturesTask = new ExtractTxSignaturesTask();
    final FullImportBlockStep<C> importBlockStep =
        new FullImportBlockStep<>(protocolSchedule, protocolContext);

    return PipelineBuilder.createPipelineFrom(
            "fetchCheckpoints",
            checkpointRangeSource,
            downloaderParallelism,
            metricsSystem.createLabelledCounter(
                MetricCategory.SYNCHRONIZER,
                "chain_download_pipeline_processed_total",
                "Number of entries process by each chain download pipeline stage",
                "step",
                "action"))
        .thenProcessAsyncOrdered("downloadHeaders", downloadHeadersStep, downloaderParallelism)
        .thenFlatMap("validateHeadersJoin", validateHeadersJoinUpStep, singleHeaderBufferSize)
        .inBatches(headerRequestSize)
        .thenProcessAsyncOrdered("downloadBodies", downloadBodiesStep, downloaderParallelism)
        .thenFlatMap("extractTxSignatures", extractTxSignaturesTask, singleHeaderBufferSize)
        .andFinishWith("importBlock", importBlockStep);
  }

  private boolean shouldContinueDownloadingFromPeer(
      final EthPeer peer, final BlockHeader lastCheckpointHeader) {
    // TODO: Need to handle cases where the peer's chain is just beyond the last checkpoint but
    // within the skip range (so it doesn't return a new checkpoint when requested)
    // Also handle the case where peer's chain head is less than 200 checkpoint headers away.
    final boolean caughtUpToPeer =
        peer.chainState().getEstimatedHeight() <= lastCheckpointHeader.getNumber();
    return !peer.isDisconnected() && !caughtUpToPeer;
  }
}
