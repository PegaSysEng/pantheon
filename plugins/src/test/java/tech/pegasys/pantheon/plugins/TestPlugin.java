package tech.pegasys.pantheon.plugins;

import tech.pegasys.pantheon.plugins.services.PantheonEvents;
import tech.pegasys.pantheon.plugins.services.PicoCLIOptions;

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

  @Option(names = "--Xtest-option", hidden=true, defaultValue = "UNSET")
  String testOption;

  @Override
  public void register(final PantheonContext context) {
    this.context = context;

    if (System.getenv("FAILREGISTER") != null) {
      throw new RuntimeException("I was told to fail at registration");
    }
    context
        .getService(PicoCLIOptions.class)
        .ifPresent(picoCLIOptions -> picoCLIOptions.addPicoCLIOptions("Test Plugin", TestPlugin.this));
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
  }
}
