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
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;
import static tech.pegasys.pantheon.cli.DefaultCommandValues.MANDATORY_FILE_FORMAT_HELP;
import static tech.pegasys.pantheon.cli.DefaultCommandValues.MANDATORY_PATH_FORMAT_HELP;
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

    @Option(
        names = "--genesis-file-name",
        paramLabel = MANDATORY_PATH_FORMAT_HELP,
        description = "Name of the genesis file.",
        arity = "1..1")
    private String genesisFileName = "genesis.json";

    @Option(
        names = "--private-key-file-name",
        paramLabel = MANDATORY_PATH_FORMAT_HELP,
        description = "Name of the private key file.",
        arity = "1..1")
    private String privateKeyFileName = "key.priv";

    @Option(
        names = "--public-key-file-name",
        paramLabel = MANDATORY_PATH_FORMAT_HELP,
        description = "Name of the public key file.",
        arity = "1..1")
    private String publicKeyFileName = "key.pub";

    @SuppressWarnings("unused")
    @ParentCommand
    private OperatorSubCommand parentCommand; // Picocli injects reference to parent command

    private JsonObject operatorConfig;
    private JsonObject genesisConfig;
    private JsonObject networkConfig;
    private JsonObject nodesConfig;
    private boolean generateNodesKeys;

    @Override
    public void run() {
      checkNotNull(parentCommand);
      checkNotNull(parentCommand.parentCommand);
      generateNetworkConfig();
    }

    /** Generates output directory with all required configuration files. */
    private void generateNetworkConfig() {
      try {
        createOutputDirectorIfNotExists();
        parseConfig();
        if (generateNodesKeys) {
          generateNodesKeys();
        }
        writeGenesisFile(outputDirectory, genesisFileName, genesisConfig);
      } catch (IOException e) {
        LOG.error("An error occurred while trying to generate network configuration.", e);
      }
    }

    /** Generates nodes keypairs. */
    private void generateNodesKeys() {
      int nodesCount = nodesConfig.getInteger("count");
      LOG.info("Generating {} nodes keys.", nodesCount);
      IntStream.range(0, nodesCount).parallel().forEach(this::generateNodeKeypair);
    }

    /**
     * Generate a keypair for a node.
     *
     * @param node The number of the node.
     */
    private void generateNodeKeypair(int node) {
      try {
        LOG.info("Generating keypair for node {}.", node);
        SECP256K1.KeyPair keyPair = SECP256K1.KeyPair.generate();
        Address nodeAddress = Util.publicKeyToAddress(keyPair.getPublicKey());
        Path nodeDirectoryPath = outputDirectory.toPath().resolve(nodeAddress.toString());
        Files.createDirectory(nodeDirectoryPath);
        createFileAndWrite(nodeDirectoryPath, publicKeyFileName, keyPair.getPublicKey().toString());
        createFileAndWrite(
            nodeDirectoryPath, privateKeyFileName, keyPair.getPrivateKey().toString());

      } catch (IOException e) {
        LOG.error("An error occurred while trying to generate node keypair.", e);
      }
    }

    private void createFileAndWrite(Path directory, String fileName, String content)
        throws IOException {
      Path filePath = directory.resolve(fileName);
      Files.createFile(filePath);
      Files.write(filePath, content.getBytes(UTF_8));
    }

    /**
     * Parses the root configuration file and related sub elements.
     *
     * @throws IOException
     */
    private void parseConfig() throws IOException {
      String configString = Resources.toString(configurationFile.toPath().toUri().toURL(), UTF_8);
      operatorConfig = new JsonObject(configString);
      genesisConfig = operatorConfig.getJsonObject("genesis");
      networkConfig = operatorConfig.getJsonObject("network");
      nodesConfig = networkConfig.getJsonObject("nodes");
      generateNodesKeys = nodesConfig.getBoolean("generate", false);
    }

    /**
     * Creates the output directory if not exists.
     *
     * @throws IOException
     */
    private void createOutputDirectorIfNotExists() throws IOException {
      if (!outputDirectory.exists()) {
        Files.createDirectory(outputDirectory.toPath());
      }
    }

    /**
     * Write the content of the genesis to the output file.
     *
     * @param directory The directory to write the file to.
     * @param fileName The name of the output file.
     * @param genesis The genesis content.
     * @throws IOException
     */
    private void writeGenesisFile(File directory, String fileName, JsonObject genesis)
        throws IOException {
      Path genesisPath = directory.toPath().resolve(fileName);
      Files.deleteIfExists(genesisPath);
      Files.write(genesisPath, genesis.encodePrettily().getBytes(UTF_8));
    }
  }
}
