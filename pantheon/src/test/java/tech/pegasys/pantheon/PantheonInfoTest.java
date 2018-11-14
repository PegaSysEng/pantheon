package tech.pegasys.pantheon;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public final class PantheonInfoTest {

  /**
   * Ethstats wants a version string like &lt;foo&gt/v&lt;bar&gt/&lt;baz&gt/&lt;bif&gt. Foo is the
   * client identity (pantheon, Geth, Parity, etc). Bar is the version, in semantic version form
   * (1.2.3-whatever), baz is OS and chip architecture, and bif is "compiler" - which we use as JVM
   * info.
   */
  @Test
  public void versionStringIsEthstatsFriendly() {
    assertThat(PantheonInfo.version()).matches("[^/]+/v(\\d+\\.\\d+\\.\\d+[^/]*|null)/[^/]+/[^/]+");
  }
}
