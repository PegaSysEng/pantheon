/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockBody;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.Hash;

import java.io.File;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

public class BlockSubCommandTest extends CommandTestAbstract {

  private static final String EXPECTED_BLOCK_USAGE =
      "Usage: pantheon blocks [-hV] [COMMAND]"
          + System.lineSeparator()
          + "This command provides blocks related actions."
          + System.lineSeparator()
          + "  -h, --help      Show this help message and exit."
          + System.lineSeparator()
          + "  -V, --version   Print version information and exit."
          + System.lineSeparator()
          + "Commands:"
          + System.lineSeparator()
          + "  import  This command imports blocks from a file into the database."
          + System.lineSeparator();

  private static final String EXPECTED_BLOCK_IMPORT_USAGE =
      "Usage: pantheon blocks import [-hV] --from=<FILE>"
          + System.lineSeparator()
          + "This command imports blocks from a file into the database."
          + System.lineSeparator()
          + "      --from=<FILE>   File containing blocks to import"
          + System.lineSeparator()
          + "  -h, --help          Show this help message and exit."
          + System.lineSeparator()
          + "  -V, --version       Print version information and exit."
          + System.lineSeparator();

  private static final String EXPECTED_BLOCK_DUMP_USAGE =
      "Usage: pantheon blocks dump [-hV] --number=<LONG> [--to=<FILE>]"
          + System.lineSeparator()
          + "This command dump a specific block from storage"
          + System.lineSeparator()
          + "      --number=<LONG>   Number of the block to dump"
          + System.lineSeparator()
          + "      --to=<FILE>       File to write the block instead of standard output"
          + System.lineSeparator()
          + "  -h, --help            Show this help message and exit."
          + System.lineSeparator()
          + "  -V, --version         Print version information and exit."
          + System.lineSeparator();

  private static final String BLOCK_SUBCOMMAND_NAME = "blocks";
  private static final String BLOCK_IMPORT_SUBCOMMAND_NAME = "import";
  private static final String BLOCK_DUMP_SUBCOMMAND_NAME = "dump";

  // Block sub-command
  @Test
  public void blockSubCommandExistAnbHaveSubCommands() {
    CommandSpec spec = parseCommand();
    assertThat(spec.subcommands()).containsKeys(BLOCK_SUBCOMMAND_NAME);
    assertThat(spec.subcommands().get(BLOCK_SUBCOMMAND_NAME).getSubcommands())
        .containsKeys(BLOCK_IMPORT_SUBCOMMAND_NAME);
    assertThat(commandOutput.toString()).isEmpty();
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  @Test
  public void callingBlockSubCommandWithoutSubSubcommandMustDisplayUsage() {
    parseCommand(BLOCK_SUBCOMMAND_NAME);
    assertThat(commandOutput.toString()).startsWith(EXPECTED_BLOCK_USAGE);
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  @Test
  public void callingBlockSubCommandHelpMustDisplayUsage() {
    parseCommand(BLOCK_SUBCOMMAND_NAME, "--help");
    assertThat(commandOutput.toString()).startsWith(EXPECTED_BLOCK_USAGE);
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  // Import sub-sub-command
  @Test
  public void callingBlockImportSubCommandWithoutPathMustDisplayErrorAndUsage() {
    parseCommand(BLOCK_SUBCOMMAND_NAME, BLOCK_IMPORT_SUBCOMMAND_NAME);
    final String expectedErrorOutputStart = "Missing required option '--from=<FILE>'";
    assertThat(commandOutput.toString()).isEmpty();
    assertThat(commandErrorOutput.toString()).startsWith(expectedErrorOutputStart);
  }

  @Test
  public void callingBlockImportSubCommandHelpMustDisplayUsage() {
    parseCommand(BLOCK_SUBCOMMAND_NAME, BLOCK_IMPORT_SUBCOMMAND_NAME, "--help");
    assertThat(commandOutput.toString()).startsWith(EXPECTED_BLOCK_IMPORT_USAGE);
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  @Test
  public void callingBlockImportSubCommandWithPathMustImportBlocksWithThisPath() throws Exception {
    final File fileToImport = temp.newFile("blocks.file");
    parseCommand(
        BLOCK_SUBCOMMAND_NAME, BLOCK_IMPORT_SUBCOMMAND_NAME, "--from", fileToImport.getPath());

    verify(mockBlockImporter).importBlockchain(pathArgumentCaptor.capture(), any());

    assertThat(pathArgumentCaptor.getValue()).isEqualByComparingTo(fileToImport.toPath());

    assertThat(commandOutput.toString()).isEmpty();
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  // Dump sub-sub-command
  @Test
  public void callingBlockDumpSubCommandWithoutNumberMustDisplayErrorAndUsage() {
    parseCommand(BLOCK_SUBCOMMAND_NAME, BLOCK_DUMP_SUBCOMMAND_NAME);
    final String expectedErrorOutputStart = "Missing required option '--number=<LONG>'";
    assertThat(commandOutput.toString()).isEmpty();
    assertThat(commandErrorOutput.toString()).startsWith(expectedErrorOutputStart);
  }

  @Test
  public void callingBlockDumpSubCommandHelpMustDisplayUsage() {
    parseCommand(BLOCK_SUBCOMMAND_NAME, BLOCK_DUMP_SUBCOMMAND_NAME, "--help");
    assertThat(commandOutput.toString()).startsWith(EXPECTED_BLOCK_DUMP_USAGE);
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  @Test
  public void callingBlockDumpSubCommandWithNumberMustDisplayBlockInformation() throws Exception {
    final Block block = generateBlock();
    when(mockMutableBlockchain.getBlockHashByNumber(any(Long.class)))
        .thenReturn(Optional.of(block.getHash()));
    when(mockMutableBlockchain.getBlockByHash(any(Hash.class))).thenReturn(block);
    parseCommand(BLOCK_SUBCOMMAND_NAME, BLOCK_DUMP_SUBCOMMAND_NAME, "--number=0");

    assertThat(commandOutput.toString()).contains(block.toString());
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  @Test(expected = CommandLine.ExecutionException.class)
  public void callingBlockDumpSubCommandWithInvalidNumberMustDisplayErrorMessage()
      throws Exception {
    parseCommand(BLOCK_SUBCOMMAND_NAME, BLOCK_DUMP_SUBCOMMAND_NAME, "--number=0");
  }

  @Test
  public void callingDumpSubCommandWithFilePathMustWriteBlockInThisFile() throws Exception {

    final Block block = generateBlock();
    when(mockMutableBlockchain.getBlockHashByNumber(any(Long.class)))
        .thenReturn(Optional.of(block.getHash()));
    when(mockMutableBlockchain.getBlockByHash(any(Hash.class))).thenReturn(block);

    final File file = File.createTempFile("dump", "store");

    parseCommand(
        BLOCK_SUBCOMMAND_NAME, BLOCK_DUMP_SUBCOMMAND_NAME, "--number=0", "--to=" + file.getPath());

    assertThat(contentOf(file)).contains(block.toString());

    assertThat(commandOutput.toString()).isEmpty();
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  private Block generateBlock() {
    final BlockBody body = new BlockBody(Collections.emptyList(), Collections.emptyList());
    return new Block(new BlockHeaderTestFixture().buildHeader(), body);
  }
}
