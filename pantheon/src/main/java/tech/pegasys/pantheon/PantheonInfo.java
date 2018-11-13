package tech.pegasys.pantheon;

import tech.pegasys.pantheon.util.PlatformDetector;

// This file is generated via a gradle task and should not be edited directly.
public class PantheonInfo {
  private static final String version =
      "pantheon/0.9.0-SNAPSHOT/"
          + PlatformDetector.getOS()
          + "/" 
          + PlatformDetector.getVM();

  private PantheonInfo() {}

  public static String version() {
    return version;
  }
}
