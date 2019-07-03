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

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;
import static tech.pegasys.pantheon.cli.BlocksSubCommand.COMMAND_NAME;
import static tech.pegasys.pantheon.cli.DefaultCommandValues.MANDATORY_FILE_FORMAT_HELP;
import static tech.pegasys.pantheon.cli.DefaultCommandValues.MANDATORY_LONG_FORMAT_HELP;

import tech.pegasys.pantheon.cli.BlocksSubCommand.DumpSubCommand;
import tech.pegasys.pantheon.cli.BlocksSubCommand.ImportSubCommand;
import tech.pegasys.pantheon.ethereum.chain.MutableBlockchain;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.metrics.prometheus.MetricsConfiguration;
import tech.pegasys.pantheon.metrics.prometheus.MetricsService;
import tech.pegasys.pantheon.util.BlockImporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

/** Blocks related sub-command */
@Command(
    name = COMMAND_NAME,
    description = "This command provides blocks related actions.",
    mixinStandardHelpOptions = true,
    subcommands = {ImportSubCommand.class, DumpSubCommand.class})
class BlocksSubCommand implements Runnable {
  private static final Logger LOG = LogManager.getLogger();

  static final String COMMAND_NAME = "blocks";

  @SuppressWarnings("unused")
  @ParentCommand
  private PantheonCommand parentCommand; // Picocli injects reference to parent command

  @SuppressWarnings("unused")
  @Spec
  private CommandSpec spec; // Picocli injects reference to command spec

  private final BlockImporter blockImporter;
  private final PrintStream out;

  BlocksSubCommand(final BlockImporter blockImporter, final PrintStream out) {
    this.blockImporter = blockImporter;
    this.out = out;
  }

  @Override
  public void run() {
    spec.commandLine().usage(out);
  }

  /**
   * blocks import sub-command
   *
   * <p>Imports blocks from a file into the database
   */
  @Command(
      name = "import",
      description = "This command imports blocks from a file into the database.",
      mixinStandardHelpOptions = true)
  static class ImportSubCommand implements Runnable {
    @SuppressWarnings("unused")
    @ParentCommand
    private BlocksSubCommand parentCommand; // Picocli injects reference to parent command

    @Option(
        names = "--from",
        required = true,
        paramLabel = MANDATORY_FILE_FORMAT_HELP,
        description = "File containing blocks to import",
        arity = "1..1")
    private final File blocksImportFile = null;

    @Override
    public void run() {
      LOG.info("Runs import sub command with blocksImportFile : {}", blocksImportFile);

      checkCommand(parentCommand);
      checkNotNull(parentCommand.blockImporter);

      Optional<MetricsService> metricsService = initMetrics(parentCommand);

      try {
        // As blocksImportFile even if initialized as null is injected by PicoCLI and param is
        // mandatory
        // So we are sure it's always not null, we can remove the warning
        //noinspection ConstantConditions
        final Path path = blocksImportFile.toPath();

        parentCommand.blockImporter.importBlockchain(
            path, parentCommand.parentCommand.buildController());
      } catch (final FileNotFoundException e) {
        throw new ExecutionException(
            new CommandLine(this), "Could not find file to import: " + blocksImportFile);
      } catch (final IOException e) {
        throw new ExecutionException(
            new CommandLine(this), "Unable to import blocks from " + blocksImportFile, e);
      } finally {
        metricsService.ifPresent(MetricsService::stop);
      }
    }
  }

  /**
   * blocks dump sub-command
   *
   * <p>Dump a specific block from storage
   */
  @Command(
      name = "dump",
      description = "This command dump a specific block from storage",
      mixinStandardHelpOptions = true)
  static class DumpSubCommand implements Runnable {
    @SuppressWarnings("unused")
    @ParentCommand
    private BlocksSubCommand parentCommand; // Picocli injects reference to parent command

    @Option(
        names = "--number",
        required = true,
        paramLabel = MANDATORY_LONG_FORMAT_HELP,
        description = "Number of the block to dump",
        arity = "1..1")
    private final Long blockNumber = null;

    @Option(
        names = "--to",
        paramLabel = MANDATORY_FILE_FORMAT_HELP,
        description = "File to write the block instead of standard output",
        arity = "1..1")
    private File dumpExportFile = null;

    @Override
    public void run() {

      LOG.info("Runs dump sub command with block number : {}", blockNumber);

      checkCommand(parentCommand);
      checkNotNull(blockNumber);

      Optional<MetricsService> metricsService = initMetrics(parentCommand);

      try {

        final MutableBlockchain blockchain =
            parentCommand.parentCommand.buildController().getProtocolContext().getBlockchain();

        Optional<Hash> blockHashByNumber = blockchain.getBlockHashByNumber(blockNumber);

        if (blockHashByNumber.isPresent()) {
          final Hash hash = blockHashByNumber.get();
          outputDump(blockchain.getBlockByHash(hash));
        } else {
          throw new ExecutionException(
              new CommandLine(this), "Unable to find block number " + blockNumber);
        }

      } finally {
        metricsService.ifPresent(MetricsService::stop);
      }
    }

    private void outputDump(final Block block) {
      if (dumpExportFile != null) {
        final Path path = dumpExportFile.toPath();
        try (final BufferedWriter fileWriter = Files.newBufferedWriter(path, UTF_8)) {
          fileWriter.write(block.toString());
        } catch (final IOException e) {
          throw new ExecutionException(
              new CommandLine(this),
              "An error occurred while trying to write the dump of the block");
        }
      } else {
        parentCommand.out.println(block.toString());
      }
    }
  }

  private static void checkCommand(final BlocksSubCommand parentCommand) {
    checkNotNull(parentCommand);
    checkNotNull(parentCommand.parentCommand);
  }

  private static Optional<MetricsService> initMetrics(final BlocksSubCommand parentCommand) {
    Optional<MetricsService> metricsService = Optional.empty();
    final MetricsConfiguration metricsConfiguration =
        parentCommand.parentCommand.metricsConfiguration();
    if (metricsConfiguration.isEnabled() || metricsConfiguration.isPushEnabled()) {
      metricsService =
          Optional.of(
              MetricsService.create(
                  Vertx.vertx(),
                  metricsConfiguration,
                  parentCommand.parentCommand.getMetricsSystem()));
      metricsService.ifPresent(MetricsService::start);
    }
    return metricsService;
  }
}
