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
import static tech.pegasys.pantheon.cli.DefaultCommandValues.MANDATORY_FILE_FORMAT_HELP;

import tech.pegasys.pantheon.metrics.prometheus.MetricsConfiguration;
import tech.pegasys.pantheon.metrics.prometheus.MetricsHttpService;
import tech.pegasys.pantheon.util.BlockImporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;

import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command(
  name = "blocks",
  description = "This command provides blocks related actions.",
  mixinStandardHelpOptions = true,
  subcommands = {ImportSubCommand.class}
)
class BlocksSubCommand implements Runnable {
  private static final Logger LOG = LogManager.getLogger();

  @ParentCommand PantheonCommand parentCommand; // Picocli injects reference to parent command

  @Spec private CommandSpec spec; // Picocli injects reference to command spec

  final BlockImporter blockImporter;
  final PrintStream out;

  BlocksSubCommand(final BlockImporter blockImporter, PrintStream out) {
    this.blockImporter = blockImporter;
    this.out = out;
  }

  @Override
  public void run() {
    spec.commandLine().usage(out);
  }
}

@Command(
  name = "import",
  description = "This command imports blocks from a file into the database.",
  mixinStandardHelpOptions = true
)
class ImportSubCommand implements Runnable {
  private static final Logger LOG = LogManager.getLogger();

  @ParentCommand
  private BlocksSubCommand parentCommand; // Picocli injects reference to parent command

  /*  @Option(
    names = "--from",
    paramLabel = MANDATORY_PATH_FORMAT_HELP,
    description = "File containing blocks to import",
    arity = "1..1"
  )
  private final File blocksImportFile = null;*/

  @Parameters(
    arity = "1..1",
    paramLabel = MANDATORY_FILE_FORMAT_HELP,
    description = "File containing blocks to import"
  )
  private final File blocksImportFile = null;

  @Override
  public void run() {
    LOG.info("Runs import sub command with blocksImportFile : {}", blocksImportFile);

    checkNotNull(parentCommand);
    checkNotNull(parentCommand.blockImporter);

    Optional<MetricsHttpService> metricsHttpService = Optional.empty();
    try {
      final MetricsConfiguration metricsConfiguration =
          parentCommand.parentCommand.metricsConfiguration();
      if (metricsConfiguration.isEnabled()) {
        metricsHttpService =
            Optional.of(
                new MetricsHttpService(
                    Vertx.vertx(),
                    metricsConfiguration,
                    parentCommand.parentCommand.getMetricsSystem()));
        metricsHttpService.ifPresent(MetricsHttpService::start);
      }
      parentCommand.blockImporter.importBlockchain(
          blocksImportFile.toPath(), parentCommand.parentCommand.buildController());
    } catch (final FileNotFoundException e) {
      throw new ExecutionException(
          new CommandLine(this), "Could not find file to import: " + blocksImportFile);
    } catch (final IOException e) {
      throw new ExecutionException(
          new CommandLine(this), "Unable to import blocks from " + blocksImportFile, e);
    } finally {
      metricsHttpService.ifPresent(MetricsHttpService::stop);
    }
  }
}
