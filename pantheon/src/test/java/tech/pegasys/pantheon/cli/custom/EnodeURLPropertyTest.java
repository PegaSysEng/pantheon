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
package tech.pegasys.pantheon.cli.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import tech.pegasys.pantheon.cli.custom.EnodeURLProperty.EnodeURLPropertyConverter;

import org.junit.Test;

public class EnodeURLPropertyTest {

  private final EnodeURLPropertyConverter converter = new EnodeURLPropertyConverter();

  @Test
  public void newInstanceOfEnodeURLWithDiscoveryPort() {
    EnodeURLProperty enodeURLProperty =
        new EnodeURLProperty("6f8a80d1", "192.168.0.1", 30303, 30301);

    assertThat(enodeURLProperty.getNodeId()).isEqualTo("6f8a80d1");
    assertThat(enodeURLProperty.getIp()).isEqualTo("192.168.0.1");
    assertThat(enodeURLProperty.getListeningPort()).isEqualTo(30303);
    assertThat(enodeURLProperty.getDiscoveryPort().isPresent()).isTrue();
    assertThat(enodeURLProperty.getDiscoveryPort().getAsInt()).isEqualTo(30301);
  }

  @Test
  public void newInstanceOfEnodeURLWithoutDiscoveryPort() {
    EnodeURLProperty enodeURLProperty = new EnodeURLProperty("6f8a80d1", "192.168.0.1", 30303);

    assertThat(enodeURLProperty.getNodeId()).isEqualTo("6f8a80d1");
    assertThat(enodeURLProperty.getIp()).isEqualTo("192.168.0.1");
    assertThat(enodeURLProperty.getListeningPort()).isEqualTo(30303);
    assertThat(enodeURLProperty.getDiscoveryPort().isPresent()).isFalse();
  }

  @Test
  public void convertEnodeURLWithDiscoveryPort() {
    String value = "enode://6f8a80d1@192.168.0.1:30303?discport=30301";
    EnodeURLProperty expectedProperty =
        new EnodeURLProperty("6f8a80d1", "192.168.0.1", 30303, 30301);

    EnodeURLProperty convertedProperty = new EnodeURLPropertyConverter().convert(value);

    assertThat(convertedProperty).isEqualTo(expectedProperty);
  }

  @Test
  public void convertEnodeURLWithoutDiscoveryPort() {
    String value = "enode://6f8a80d1@192.168.0.1:30303";
    EnodeURLProperty expectedProperty = new EnodeURLProperty("6f8a80d1", "192.168.0.1", 30303);

    EnodeURLProperty convertedProperty = new EnodeURLPropertyConverter().convert(value);

    assertThat(convertedProperty).isEqualTo(expectedProperty);
  }

  @Test
  public void convertEnodeURLWithIPV6() {
    String value = "enode://6f8a80d1@2001:0db8:85a3:0:0:8a2e:0370:7334:30303?discport=30301";
    EnodeURLProperty expectedProperty =
        new EnodeURLProperty("6f8a80d1", "2001:0db8:85a3:0:0:8a2e:0370:7334", 30303, 30301);

    EnodeURLProperty convertedProperty = new EnodeURLPropertyConverter().convert(value);

    assertThat(convertedProperty).isEqualTo(expectedProperty);
  }

  @Test
  public void convertEnodeURLWithIPV6InCompactForm() {
    String value = "enode://6f8a80d1@fe80::200:f8ff:fe21:67cf:30303?discport=30301";
    EnodeURLProperty expectedProperty =
        new EnodeURLProperty("6f8a80d1", "fe80::200:f8ff:fe21:67cf", 30303, 30301);

    EnodeURLProperty convertedProperty = new EnodeURLPropertyConverter().convert(value);

    assertThat(convertedProperty).isEqualTo(expectedProperty);
  }

  @Test
  public void convertEnodeURLWithoutNodeId() {
    String value = "enode://@192.168.0.1:30303";
    Throwable thrown = catchThrowable(() -> converter.convert(value));

    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Invalid enode URL syntax. Enode URL should have the following format 'enode://<node_id>@<ip>:<listening_port>[?discport=<discovery_port>]'.");
  }

  @Test
  public void convertEnodeURLWithoutIp() {
    String value = "enode://6f8a80d1@:30303";
    Throwable thrown = catchThrowable(() -> converter.convert(value));

    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid enode URL IP format.");
  }

  @Test
  public void convertEnodeURLWithInvalidIpFormat() {
    String value = "enode://6f8a80d1@192.0.1:30303";
    Throwable thrown = catchThrowable(() -> converter.convert(value));

    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid enode URL IP format.");
  }

  @Test
  public void convertEnodeURLWithoutListeningPort() {
    String value = "enode://6f8a80d1@192.168.0.1:";
    Throwable thrown = catchThrowable(() -> converter.convert(value));

    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Invalid enode URL syntax. Enode URL should have the following format 'enode://<node_id>@<ip>:<listening_port>[?discport=<discovery_port>]'.");
  }

  @Test
  public void convertEnodeURLWithoutListeningPortAnWithDiscoveryPort() {
    String value = "enode://6f8a80d1@192.168.0.1:?30301";
    Throwable thrown = catchThrowable(() -> converter.convert(value));

    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Invalid enode URL syntax. Enode URL should have the following format 'enode://<node_id>@<ip>:<listening_port>[?discport=<discovery_port>]'.");
  }

  @Test
  public void convertEnodeURLWithAboveRangeListeningPort() {
    String value = "enode://6f8a80d1@192.168.0.1:98765";
    Throwable thrown = catchThrowable(() -> converter.convert(value));

    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid listening port range. Port should be between 0 - 65535");
  }

  @Test
  public void convertEnodeURLWithAboveRangeDiscoveryPort() {
    String value = "enode://6f8a80d1@192.168.0.1:30303?discport=98765";
    Throwable thrown = catchThrowable(() -> converter.convert(value));

    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid discovery port range. Port should be between 0 - 65535");
  }

  @Test
  public void convertNullEnodeURL() {
    Throwable thrown = catchThrowable(() -> converter.convert(null));

    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Can't convert null/empty string to EnodeURLProperty.");
  }

  @Test
  public void convertEmptyEnodeURL() {
    Throwable thrown = catchThrowable(() -> converter.convert(""));

    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Can't convert null/empty string to EnodeURLProperty.");
  }
}
