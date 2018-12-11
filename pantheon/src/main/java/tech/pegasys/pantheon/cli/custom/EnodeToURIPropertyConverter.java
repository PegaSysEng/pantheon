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

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import picocli.CommandLine.ITypeConverter;

public class EnodeToURIPropertyConverter implements ITypeConverter<URI> {

  private static final String IP_REPLACE_MARKER = "$$IP_PATTERN$$";
  private static final String IPV4_PATTERN =
      "(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}";
  private static final String IPV6_PATTERN = "(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}";
  private static final String IPV6_COMPACT_PATTERN =
      "((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)";
  private static final String DISCOVERY_PORT_PATTERN = "\\?discport=(?<discovery>\\d+)";

  private static final String ENODE_URL_PATTERN =
      "enode://(?<nodeId>\\w+)@(?<ip>" + IP_REPLACE_MARKER + "):(?<listening>\\d+)";

  @Override
  public URI convert(final String value) throws IllegalArgumentException {
    if (value == null || "".equals(value)) {
      throw new IllegalArgumentException("Can't convert null/empty string to EnodeURLProperty.");
    }

    final boolean containsDiscoveryPort = value.contains("discport");
    final boolean isIPV4 = Pattern.compile(".*" + IPV4_PATTERN + ".*").matcher(value).matches();
    final boolean isIPV6 = Pattern.compile(".*" + IPV6_PATTERN + ".*").matcher(value).matches();
    final boolean isIPV6Compact =
        Pattern.compile(".*" + IPV6_COMPACT_PATTERN + ".*").matcher(value).matches();

    String pattern = ENODE_URL_PATTERN;
    if (isIPV4) {
      pattern = pattern.replace(IP_REPLACE_MARKER, IPV4_PATTERN);
    } else if (isIPV6) {
      pattern = pattern.replace(IP_REPLACE_MARKER, IPV6_PATTERN);
    } else if (isIPV6Compact) {
      pattern = pattern.replace(IP_REPLACE_MARKER, IPV6_COMPACT_PATTERN);
    } else {
      throw new IllegalArgumentException("Invalid enode URL IP format.");
    }

    if (containsDiscoveryPort) {
      pattern += DISCOVERY_PORT_PATTERN;
    }
    if (isIPV6) {
      pattern = pattern.replace(IP_REPLACE_MARKER, IPV6_PATTERN);
    } else {
      pattern = pattern.replace(IP_REPLACE_MARKER, IPV4_PATTERN);
    }

    final Matcher matcher = Pattern.compile(pattern).matcher(value);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          "Invalid enode URL syntax. Enode URL should have the following format 'enode://<node_id>@<ip>:<listening_port>[?discport=<discovery_port>]'.");
    }

    final String nodeId = matcher.group("nodeId");
    final String ip = matcher.group("ip");
    final Integer listeningPort = getAndValidatePort(matcher, "listening");

    if (containsDiscoveryPort(value)) {
      final Integer discoveryPort = getAndValidatePort(matcher, "discovery");
      return URI.create(
          String.format("enode://%s@%s:%d?discport=%d", nodeId, ip, listeningPort, discoveryPort));
    } else {
      return URI.create(String.format("enode://%s@%s:%d", nodeId, ip, listeningPort));
    }
  }

  private Integer getAndValidatePort(final Matcher matcher, final String portName) {
    int port = Integer.valueOf(matcher.group(portName));
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException(
          "Invalid " + portName + " port range. Port should be between 0 - 65535");
    } else {
      return port;
    }
  }

  private boolean containsDiscoveryPort(final String value) {
    return value.contains("discport");
  }
}
