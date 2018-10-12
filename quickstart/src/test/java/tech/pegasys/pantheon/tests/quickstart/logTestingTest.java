package tech.pegasys.pantheon.tests.quickstart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

public class logTestingTest {

  private static final Logger LOG = LogManager.getLogger();

  @BeforeClass
  public static void beforeClass() {
    LOG.error("error log test from beforeClass");
    LOG.info("info log test from beforeClass");
    LOG.debug("debug log test from beforeClass");
  }

  @Test
  public void test() {
    LOG.error("error log test from test");
    LOG.info("info log test from test");
    LOG.debug("debug log test from test");
  }
}
