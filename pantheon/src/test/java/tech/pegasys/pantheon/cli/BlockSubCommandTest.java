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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import picocli.CommandLine.Model.CommandSpec;

public class BlockSubCommandTest extends CommandTestAbstract {
  @Rule public final TemporaryFolder temp = new TemporaryFolder();

  @Before
  public void resetSystemProps() {
    System.setProperty("pantheon.docker", "false");
  }

  @Override
  @Before
  public void initMocks() throws Exception {
    super.initMocks();

    when(mockRunnerBuilder.vertx(any())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.pantheonController(any())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.discovery(anyBoolean())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.bootstrapPeers(any())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.discoveryHost(anyString())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.discoveryPort(anyInt())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.maxPeers(anyInt())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.jsonRpcConfiguration(any())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.webSocketConfiguration(any())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.permissioningConfiguration(any())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.dataDir(any())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.bannedNodeIds(any())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.metricsSystem(any())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.metricsConfiguration(any())).thenReturn(mockRunnerBuilder);
    when(mockRunnerBuilder.build()).thenReturn(mockRunner);
  }

  private final String EXPECTED_BLOCK_USAGE =
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

  private final String EXPECTED_BLOCK_IMPORT_USAGE =
      "Usage: pantheon blocks import [-hV] <FILE>"
          + System.lineSeparator()
          + "This command imports blocks from a file into the database."
          + System.lineSeparator()
          + "      <FILE>      File containing blocks to import"
          + System.lineSeparator()
          + "  -h, --help      Show this help message and exit."
          + System.lineSeparator()
          + "  -V, --version   Print version information and exit."
          + System.lineSeparator();

  // Block sub-command
  @Test
  public void blockSubCommandExistAnbHaveImportSubCommand() {
    CommandSpec spec = parseCommand();
    assertThat(spec.subcommands()).containsKeys("blocks");
    assertThat(spec.subcommands().get("blocks").getSubcommands()).containsKeys("import");
    assertThat(commandOutput.toString()).isEmpty();
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  @Test
  public void callingBlockSubCommandWithoutSubSubcommandMustDisplayUsage() {
    parseCommand("blocks");
    assertThat(commandOutput.toString()).startsWith(EXPECTED_BLOCK_USAGE);
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  @Test
  public void callingBlockSubCommandHelpMustDisplayUsage() {
    parseCommand("blocks", "--help");
    assertThat(commandOutput.toString()).startsWith(EXPECTED_BLOCK_USAGE);
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  // Import sub-sub-command
  @Test
  public void callingBlockImportSubCommandWithoutPathMustDisplayErrorAndUsage() {
    parseCommand("blocks", "import");
    final String expectedErrorOutputStart = "Missing required parameter: <FILE>";
    assertThat(commandOutput.toString()).isEmpty();
    assertThat(commandErrorOutput.toString()).startsWith(expectedErrorOutputStart);
  }

  @Test
  public void callingBlockImportSubCommandHelpMustDisplayImportUsage() {
    parseCommand("blocks", "import", "--help");
    assertThat(commandOutput.toString()).startsWith(EXPECTED_BLOCK_IMPORT_USAGE);
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  @Test
  public void callingBlockImportSubCommandWithPathMustImportBlocksWithThisPath() throws Exception {
    File fileToImport = temp.newFile("blocks.file");
    parseCommand("blocks", "import", fileToImport.getPath());

    verify(mockBlockImporter).importBlockchain(pathArgumentCaptor.capture(), any());

    assertThat(pathArgumentCaptor.getValue()).isEqualByComparingTo(fileToImport.toPath());

    assertThat(commandOutput.toString()).isEmpty();
    assertThat(commandErrorOutput.toString()).isEmpty();
  }
}
