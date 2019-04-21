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
package tech.pegasys.pantheon.ethereum.graphql;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class GraphQLConfiguration {
  private static final String DEFAULT_GRAPHQL_HOST = "127.0.0.1";
  public static final int DEFAULT_GRAPHQL_PORT = 8547;

  private boolean enabled;
  private int port;
  private String host;
  private Collection<String> corsAllowedDomains = Collections.emptyList();
  private Collection<String> hostsWhitelist = Arrays.asList("localhost", "127.0.0.1");;
  private boolean authenticationEnabled = false;
  private String authenticationCredentialsFile;

  public static GraphQLConfiguration createDefault() {
    final GraphQLConfiguration config = new GraphQLConfiguration();
    config.setEnabled(false);
    config.setPort(DEFAULT_GRAPHQL_PORT);
    config.setHost(DEFAULT_GRAPHQL_HOST);
    return config;
  }

  private GraphQLConfiguration() {}

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public int getPort() {
    return port;
  }

  public void setPort(final int port) {
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public void setHost(final String host) {
    this.host = host;
  }

  public Collection<String> getCorsAllowedDomains() {
    return corsAllowedDomains;
  }

  public void setCorsAllowedDomains(final Collection<String> corsAllowedDomains) {
    if (corsAllowedDomains != null) {
      this.corsAllowedDomains = corsAllowedDomains;
    }
  }

  public Collection<String> getHostsWhitelist() {
    return Collections.unmodifiableCollection(this.hostsWhitelist);
  }

  public void setHostsWhitelist(final Collection<String> hostsWhitelist) {
    this.hostsWhitelist = hostsWhitelist;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("enabled", enabled)
        .add("port", port)
        .add("host", host)
        .add("corsAllowedDomains", corsAllowedDomains)
        .add("hostsWhitelist", hostsWhitelist)
        .add("authenticationEnabled", authenticationEnabled)
        .add("authenticationCredentialsFile", authenticationCredentialsFile)
        .toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final GraphQLConfiguration that = (GraphQLConfiguration) o;
    return enabled == that.enabled
        && port == that.port
        && Objects.equal(host, that.host)
        && Objects.equal(
            Lists.newArrayList(corsAllowedDomains), Lists.newArrayList(that.corsAllowedDomains))
        && Objects.equal(
            Lists.newArrayList(hostsWhitelist), Lists.newArrayList(that.hostsWhitelist));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(enabled, port, host, corsAllowedDomains, hostsWhitelist);
  }

  public boolean isAuthenticationEnabled() {
    return authenticationEnabled;
  }

  public void setAuthenticationEnabled(final boolean authenticationEnabled) {
    this.authenticationEnabled = authenticationEnabled;
  }

  public void setAuthenticationCredentialsFile(final String authenticationCredentialsFile) {
    this.authenticationCredentialsFile = authenticationCredentialsFile;
  }

  public String getAuthenticationCredentialsFile() {
    return authenticationCredentialsFile;
  }
}
