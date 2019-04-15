package tech.pegasys.pantheon.ethereum.eth.sync.fullsync;

import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockImporter;
import tech.pegasys.pantheon.ethereum.eth.sync.tasks.exceptions.InvalidBlockException;
import tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FullImportBlockStep<C> implements Consumer<Block> {
  private static final Logger LOG = LogManager.getLogger();
  private final ProtocolSchedule<C> protocolSchedule;
  private final ProtocolContext<C> protocolContext;

  public FullImportBlockStep(
      final ProtocolSchedule<C> protocolSchedule, final ProtocolContext<C> protocolContext) {
    this.protocolSchedule = protocolSchedule;
    this.protocolContext = protocolContext;
  }

  @Override
  public void accept(final Block block) {
    final long blockNumber = block.getHeader().getNumber();
    final BlockImporter<C> importer =
        protocolSchedule.getByBlockNumber(blockNumber).getBlockImporter();
    if (!importer.importBlock(protocolContext, block, HeaderValidationMode.SKIP_DETACHED)) {
      throw new InvalidBlockException("Failed to import block", blockNumber, block.getHash());
    }
    if (blockNumber % 200 == 0) {
      LOG.info("Import reached block {}", blockNumber);
    }
  }
}
