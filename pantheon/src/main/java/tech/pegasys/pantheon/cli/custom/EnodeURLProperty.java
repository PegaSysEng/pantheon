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

import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Objects;
import picocli.CommandLine.ITypeConverter;

public class EnodeURLProperty {

  private final String nodeId;
  private final String ip;
  private final Integer listeningPort;
  private final OptionalInt discoveryPort;

  EnodeURLProperty(String nodeId, String ip, Integer listeningPort, Integer discoveryPort) {
    this.nodeId = nodeId;
    this.ip = ip;
    this.listeningPort = listeningPort;
    this.discoveryPort = OptionalInt.of(discoveryPort);
  }

  EnodeURLProperty(String nodeId, String ip, Integer listeningPort) {
    this.nodeId = nodeId;
    this.ip = ip;
    this.listeningPort = listeningPort;
    this.discoveryPort = OptionalInt.empty();
  }

  public String getNodeId() {
    return nodeId;
  }

  public String getIp() {
    return ip;
  }

  public Integer getListeningPort() {
    return listeningPort;
  }

  public OptionalInt getDiscoveryPort() {
    return discoveryPort;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnodeURLProperty that = (EnodeURLProperty) o;
    return Objects.equal(nodeId, that.nodeId)
        && Objects.equal(ip, that.ip)
        && Objects.equal(listeningPort, that.listeningPort)
        && Objects.equal(discoveryPort, that.discoveryPort);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(nodeId, ip, listeningPort, discoveryPort);
  }

  public static class EnodeURLPropertyConverter implements ITypeConverter<EnodeURLProperty> {

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
    public EnodeURLProperty convert(String value) throws IllegalArgumentException {
      if (value == null || "".equals(value)) {
        throw new IllegalArgumentException("Can't convert null/empty string to EnodeURLProperty.");
      }

      boolean containsDiscoveryPort = value.contains("discport");
      boolean isIPV4 = Pattern.compile(".*" + IPV4_PATTERN + ".*").matcher(value).matches();
      boolean isIPV6 = Pattern.compile(".*" + IPV6_PATTERN + ".*").matcher(value).matches();
      boolean isIPV6Compact =
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

      Matcher matcher = Pattern.compile(pattern).matcher(value);
      if (!matcher.matches()) {
        throw new IllegalArgumentException(
            "Invalid enode URL syntax. Enode URL should have the following format 'enode://<node_id>@<ip>:<listening_port>[?discport=<discovery_port>]'.");
      }

      String nodeId = matcher.group("nodeId");
      String ip = matcher.group("ip");
      Integer listeningPort = getAndValidatePort(matcher, "listening");

      if (containsDiscoveryPort(value)) {
        Integer discoveryPort = getAndValidatePort(matcher, "discovery");
        return new EnodeURLProperty(nodeId, ip, listeningPort, discoveryPort);
      } else {
        return new EnodeURLProperty(nodeId, ip, listeningPort);
      }
    }

    private Integer getAndValidatePort(Matcher matcher, String portName) {
      int port = Integer.valueOf(matcher.group(portName));
      if (port < 0 || port > 65535) {
        throw new IllegalArgumentException(
            "Invalid " + portName + " port range. Port should be between 0 - 65535");
      } else {
        return port;
      }
    }

    private boolean containsDiscoveryPort(String value) {
      return value.contains("discport");
    }
  }
}
