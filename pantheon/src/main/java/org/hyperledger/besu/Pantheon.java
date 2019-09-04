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
package org.hyperledger.besu;

import static org.apache.logging.log4j.LogManager.getLogger;

import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.chainexport.RlpBlockExporter;
import org.hyperledger.besu.chainimport.JsonBlockImporter;
import org.hyperledger.besu.chainimport.RlpBlockImporter;
import org.hyperledger.besu.cli.PantheonCommand;
import org.hyperledger.besu.controller.PantheonController;
import org.hyperledger.besu.services.PantheonPluginContextImpl;
import picocli.CommandLine.RunLast;

public final class Pantheon {
  private static final int SUCCESS_EXIT_CODE = 0;
  private static final int ERROR_EXIT_CODE = 1;

  public static void main(final String... args) {
    final Logger logger = getLogger();
    Thread.setDefaultUncaughtExceptionHandler(
        (thread, error) ->
            logger.error("Uncaught exception in thread \"" + thread.getName() + "\"", error));
    final PantheonCommand pantheonCommand =
        new PantheonCommand(
            logger,
            new RlpBlockImporter(),
            JsonBlockImporter::new,
            RlpBlockExporter::new,
            new RunnerBuilder(),
            new PantheonController.Builder(),
            new PantheonPluginContextImpl(),
            System.getenv());

    pantheonCommand.parse(
        new RunLast().andExit(SUCCESS_EXIT_CODE),
        pantheonCommand.exceptionHandler().andExit(ERROR_EXIT_CODE),
        System.in,
        args);
  }
}
