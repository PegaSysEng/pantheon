package tech.pegasys.pantheon;

import tech.pegasys.pantheon.util.PlatformDetector;

// This file is generated via a gradle task and should not be edited directly.
public final class PantheonInfo {
  private static final String clientIdentity = "pantheon";
  private static final String version =
      clientIdentity
          + "/"
          + PantheonInfo.class.getPackage().getImplementationVersion()
          + "/"
          + PlatformDetector.getOS()
          + "/"
          + PlatformDetector.getVM();

  private PantheonInfo() {}

  public static String clientIdentity() {
    return clientIdentity;
  }

  public static String version() {
    return version;
  }
}
