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
package tech.pegasys.pantheon.plugins;

import tech.pegasys.pantheon.plugins.services.PantheonEvents;
import tech.pegasys.pantheon.plugins.services.PicoCLIOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Optional;

import com.google.auto.service.AutoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Option;

@AutoService(PantheonPlugin.class)
public class TestPlugin implements PantheonPlugin {
  private static final Logger LOG = LogManager.getLogger();

  private PantheonContext context;
  private Optional<Object> listenerReference;

  @Option(names = "--Xtest-option", hidden = true, defaultValue = "UNSET")
  String testOption;

  @Override
  public void register(final PantheonContext context) {
    this.context = context;

    if (System.getProperty("testPlugin.failAtRegister") != null) {
      throw new RuntimeException("I was told to fail at registration");
    }
    context
        .getService(PicoCLIOptions.class)
        .ifPresent(
            picoCLIOptions -> picoCLIOptions.addPicoCLIOptions("Test Plugin", TestPlugin.this));
    writeSignal("registered");
  }

  @Override
  public void start() {
    LOG.info("Starting.  Test Option is '{}'", testOption);

    if ("FAILSTART".equals(testOption)) {
      throw new RuntimeException("I was told to fail at startup");
    }
    listenerReference =
        context
            .getService(PantheonEvents.class)
            .map(
                pantheonEvents ->
                    pantheonEvents.addBlockAddedListener(
                        s -> System.out.println("BlockAdded - " + s)));
    writeSignal("started");
  }

  @Override
  public void stop() {
    LOG.info("Stopping.  Test Option is '{}'", testOption);
    if ("FAILSTOP".equals(testOption)) {
      throw new RuntimeException("I was told to fail at stop");
    }
    listenerReference.ifPresent(
        reference ->
            context
                .getService(PantheonEvents.class)
                .ifPresent(pantheonEvents -> pantheonEvents.removeBlockAddedObserver(reference)));
    writeSignal("stopped");
  }

  /** This is used to signal to the acceptance test that certain tasks were completed. */
  private void writeSignal(final String signal) {
    try {
      final File callbackFile =
          new File(System.getProperty("pantheon.plugins.dir", "plugins"), "testPlugin." + signal);
      Files.write(
          callbackFile.toPath(), Collections.singletonList(signal + "\ntestOption=" + testOption));
      callbackFile.deleteOnExit();
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
