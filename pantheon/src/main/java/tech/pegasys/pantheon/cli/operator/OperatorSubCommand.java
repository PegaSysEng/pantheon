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
package tech.pegasys.pantheon.cli.operator;

import com.google.common.io.Resources;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;
import tech.pegasys.pantheon.cli.PantheonCommand;
import tech.pegasys.pantheon.config.GenesisConfigFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;
import static tech.pegasys.pantheon.cli.DefaultCommandValues.MANDATORY_FILE_FORMAT_HELP;
import static tech.pegasys.pantheon.cli.operator.OperatorSubCommand.COMMAND_NAME;

/** Operator related sub-command */
@Command(
    name = COMMAND_NAME,
    description = "This command provides operator related actions.",
    mixinStandardHelpOptions = true,
    subcommands = {OperatorSubCommand.GenerateNetworkConfigSubCommand.class})
public class OperatorSubCommand implements Runnable {
  private static final Logger LOG = LogManager.getLogger();

  public static final String COMMAND_NAME = "operator";

  @SuppressWarnings("unused")
  @ParentCommand
  private PantheonCommand parentCommand; // Picocli injects reference to parent command

  @SuppressWarnings("unused")
  @Spec
  private CommandSpec spec; // Picocli injects reference to command spec

  private final PrintStream out;

  public OperatorSubCommand(final PrintStream out) {
    this.out = out;
  }

  @Override
  public void run() {
    spec.commandLine().usage(out);
  }

  @Command(
      name = "generate-network-config",
      description = "This command generates blockchain network configuration files.",
      mixinStandardHelpOptions = true)
  static class GenerateNetworkConfigSubCommand implements Runnable {

    @Option(
        required = true,
        names = "--config-file",
        paramLabel = MANDATORY_FILE_FORMAT_HELP,
        description = "Configuration file.",
        arity = "1..1")
    private File configurationFile = null;

    @Option(
        required = true,
        names = "--to",
        paramLabel = MANDATORY_FILE_FORMAT_HELP,
        description = "Directory to write output files to.",
        arity = "1..1")
    private File outputDirectory = null;

    @SuppressWarnings("unused")
    @ParentCommand
    private OperatorSubCommand parentCommand; // Picocli injects reference to parent command

    @Override
    public void run() {
      checkNotNull(parentCommand);
      checkNotNull(parentCommand.parentCommand);
      generateNetworkConfig();
    }

    private void generateNetworkConfig() {
      parentCommand.out.println("generateNetworkConfig");
      parentCommand.out.println(outputDirectory.toPath().toString());
      try {
        if (!outputDirectory.exists()) {
          Files.createDirectory(outputDirectory.toPath());
        }
        // Read configuration file.
        String configString = Resources.toString(configurationFile.toPath().toUri().toURL(), UTF_8);
        JsonObject operatorConfig = new JsonObject(configString);
        // Retrieve genesis section.
        JsonObject genesisJsonObject = operatorConfig.getJsonObject("genesis");
        // Create output genesis.
        Path genesisFile = Files.createFile(outputDirectory.toPath().resolve("genesis.json"));
        // Write genesis content.
        Files.write(genesisFile, genesisJsonObject.encodePrettily().getBytes(UTF_8));
      } catch (IOException e) {
        LOG.error("An error occurred while trying to generate network configuration.", e);
      }
    }
  }
}
