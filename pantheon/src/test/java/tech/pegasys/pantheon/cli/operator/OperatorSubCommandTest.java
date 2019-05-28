/*
 * Copyright 2019 ConsenSys AG.
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
package tech.pegasys.pantheon.cli.operator;

import org.junit.After;
import org.junit.Test;
import picocli.CommandLine.Model.CommandSpec;
import tech.pegasys.pantheon.cli.CommandTestAbstract;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

public class OperatorSubCommandTest extends CommandTestAbstract {

  private static final String EXPECTED_OPERATOR_USAGE =
      "Usage: pantheon operator [-hV] [COMMAND]"
          + System.lineSeparator()
          + "This command provides operator related actions."
          + System.lineSeparator()
          + "  -h, --help      Show this help message and exit."
          + System.lineSeparator()
          + "  -V, --version   Print version information and exit."
          + System.lineSeparator()
          + "Commands:"
          + System.lineSeparator()
          + "  generate-blockchain-config  This command generates blockchain network"
          + System.lineSeparator()
          + "                                configuration files.";


  private static final String OPERATOR_SUBCOMMAND_NAME = "operator";
  private static final String GENERATE_BLOCKCHAIN_CONFIG_SUBCOMMAND_NAME =
      "generate-blockchain-config";

  // RLP sub-command
  @Test
  public void operatorSubCommandExistAnbHaveSubCommands() {
    final CommandSpec spec = parseCommand();
    assertThat(spec.subcommands()).containsKeys(OPERATOR_SUBCOMMAND_NAME);
    assertThat(spec.subcommands().get(OPERATOR_SUBCOMMAND_NAME).getSubcommands())
        .containsKeys(GENERATE_BLOCKCHAIN_CONFIG_SUBCOMMAND_NAME);
    assertThat(commandOutput.toString()).isEmpty();
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  @Test
  public void callingOperatorSubCommandWithoutSubSubcommandMustDisplayUsage() {
    parseCommand(OPERATOR_SUBCOMMAND_NAME);
    assertThat(commandOutput.toString()).startsWith(EXPECTED_OPERATOR_USAGE);
    assertThat(commandErrorOutput.toString()).isEmpty();
  }

  @Test
  public void callingOperatorCommandHelpMustDisplayUsage() {
    parseCommand(OPERATOR_SUBCOMMAND_NAME, "--help");
    assertThat(commandOutput.toString()).startsWith(EXPECTED_OPERATOR_USAGE);
    assertThat(commandErrorOutput.toString()).isEmpty();
  }


}
