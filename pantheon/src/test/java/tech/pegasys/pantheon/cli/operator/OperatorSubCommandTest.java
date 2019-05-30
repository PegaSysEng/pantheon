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

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.vertx.core.json.JsonObject;
import org.junit.BeforeClass;
import org.junit.Test;
import picocli.CommandLine.Model.CommandSpec;
import tech.pegasys.pantheon.cli.CommandTestAbstract;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempDirectory;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static tech.pegasys.pantheon.cli.operator.OperatorSubCommandTest.Cmd.cmd;

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
  private static Path tmpOutputDirectoryPath;

  @BeforeClass
  public static void init() throws IOException {
    tmpOutputDirectoryPath = createTempDirectory("output");
  }

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

  @Test
  public void generateBlockchainConfigMustGenerateKeysWhenGenerateIsTrue() throws IOException {
    generateBlockchainConfig(
        cmd(),
        "/operator/config_generate_keys.json",
        tmpOutputDirectoryPath,
        "genesis.json",
        true,
        asList("key.pub", "key.priv"));
  }

  @Test
  public void generateBlockchainConfigMustImportKeysWhenGenerateIsFalse() throws IOException {
    generateBlockchainConfig(
        cmd(),
        "/operator/config_import_keys.json",
        tmpOutputDirectoryPath,
        "genesis.json",
        false,
        singletonList("key.pub"));
  }

  @Test
  public void genesisFileNameShouldBeEqualToOption() throws IOException {
    generateBlockchainConfig(
        cmd("--genesis-file-name", "option.json"),
        "/operator/config_generate_keys.json",
        tmpOutputDirectoryPath,
        "option.json",
        true,
        asList("key.pub", "key.priv"));
  }

  @Test
  public void publicKeyFileNameShouldBeEqualToOption() throws IOException {
    generateBlockchainConfig(
        cmd("--public-key-file-name", "pub.test"),
        "/operator/config_generate_keys.json",
        tmpOutputDirectoryPath,
        "genesis.json",
        true,
        asList("pub.test", "key.priv"));
  }

  @Test
  public void privateKeyFileNameShouldBeEqualToOption() throws IOException {
    generateBlockchainConfig(
        cmd("--private-key-file-name", "priv.test"),
        "/operator/config_generate_keys.json",
        tmpOutputDirectoryPath,
        "genesis.json",
        true,
        asList("key.pub", "priv.test"));
  }

  private void generateBlockchainConfig(
      final Cmd cmd,
      final String configFile,
      final Path outputDirectoryPath,
      final String genesisFileName,
      final boolean generate,
      final Collection<String> expectedKeyFiles) {
    final URL configFilePath = this.getClass().getResource(configFile);
    final DocumentContext configDocumentContext = JsonPath.parse(contentOf(configFilePath, UTF_8));
    parseCommand(
        cmd(
                OPERATOR_SUBCOMMAND_NAME,
                GENERATE_BLOCKCHAIN_CONFIG_SUBCOMMAND_NAME,
                "--config-file",
                configFilePath.getPath(),
                "--to",
                outputDirectoryPath.toString())
            .args(cmd.argsArray())
            .argsArray());
    assertThat(commandErrorOutput.toString()).isEmpty();

    final Path outputGenesisExpectedPath = outputDirectoryPath.resolve(genesisFileName);
    final File outputGenesisFile = new File(outputGenesisExpectedPath.toUri());
    assertTrue("Output genesis file must exist.", outputGenesisFile.exists());
    final String genesisString = contentOf(outputGenesisFile, UTF_8);
    final JsonObject genesisContent = new JsonObject(genesisString);
    assertTrue(genesisContent.containsKey("extraData"));

    final Path expectedKeysPath = outputDirectoryPath.resolve("keys");
    final File keysDirectory = new File(expectedKeysPath.toUri());
    assertTrue(keysDirectory.exists());
    final File[] nodesKeysFolders = keysDirectory.listFiles();
    assert nodesKeysFolders != null;
    if (generate) {
      assertEquals(
          nodesKeysFolders.length,
          (int) configDocumentContext.read("$.blockchain.nodes.count", Integer.class));
    }
    final Stream<File> nodesKeysFoldersStream = stream(nodesKeysFolders);

    nodesKeysFoldersStream.forEach(
        nodeFolder ->
            assertTrue(asList(requireNonNull(nodeFolder.list())).containsAll(expectedKeyFiles)));
  }

  static class Cmd {
    private List<String> args;

    private Cmd(final List<String> args) {
      this.args = args;
    }

    static Cmd cmd(final String... args) {
      return new Cmd(new ArrayList<>(asList(args)));
    }

    Cmd args(final String... args) {
      this.args.addAll(asList(args));
      return this;
    }

    String[] argsArray() {
      String[] wrapper = new String[] {};
      return args.toArray(wrapper);
    }
  }
}
