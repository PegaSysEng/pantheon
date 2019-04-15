package tech.pegasys.pantheon.ethereum.eth.sync.fullsync;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode.SKIP_DETACHED;

import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockDataGenerator;
import tech.pegasys.pantheon.ethereum.core.BlockImporter;
import tech.pegasys.pantheon.ethereum.eth.sync.tasks.exceptions.InvalidBlockException;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSpec;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FullImportBlockStepTest {

  @Mock private ProtocolSchedule<Void> protocolSchedule;
  @Mock private ProtocolSpec<Void> protocolSpec;
  @Mock private ProtocolContext<Void> protocolContext;
  @Mock private BlockImporter<Void> blockImporter;
  private final BlockDataGenerator gen = new BlockDataGenerator();

  private FullImportBlockStep<Void> importBlocksStep;

  @Before
  public void setUp() {
    when(protocolSchedule.getByBlockNumber(anyLong())).thenReturn(protocolSpec);
    when(protocolSpec.getBlockImporter()).thenReturn(blockImporter);

    importBlocksStep = new FullImportBlockStep<>(protocolSchedule, protocolContext);
  }

  @Test
  public void shouldImportBlock() {
    final Block block = gen.block();

    when(blockImporter.importBlock(protocolContext, block, SKIP_DETACHED)).thenReturn(true);
    importBlocksStep.accept(block);

    verify(protocolSchedule).getByBlockNumber(block.getHeader().getNumber());
    verify(blockImporter).importBlock(protocolContext, block, SKIP_DETACHED);
  }

  @Test
  public void shouldThrowExceptionWhenValidationFails() {
    final Block block = gen.block();

    when(blockImporter.importBlock(protocolContext, block, SKIP_DETACHED)).thenReturn(false);
    assertThatThrownBy(() -> importBlocksStep.accept(block))
        .isInstanceOf(InvalidBlockException.class);
  }
}
