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
package org.hyperledger.besu.tests.acceptance.dsl.node.configuration.privacy;

import tech.pegasys.orion.testutil.OrionTestHarness;

import org.hyperledger.besu.tests.acceptance.dsl.node.configuration.BesuFactoryConfiguration;

public class PrivacyBesuFactoryConfigurationBuilder {

  protected BesuFactoryConfiguration config;
  protected OrionTestHarness orion;

  public PrivacyBesuFactoryConfigurationBuilder setConfig(final BesuFactoryConfiguration config) {
    this.config = config;
    return this;
  }

  public PrivacyBesuFactoryConfigurationBuilder setOrion(final OrionTestHarness orion) {
    this.orion = orion;
    return this;
  }

  public PrivacyBesuFactoryConfiguration build() {
    return new PrivacyBesuFactoryConfiguration(
        config.getName(),
        config.getMiningParameters(),
        config.getPrivacyParameters(),
        config.getJsonRpcConfiguration(),
        config.getWebSocketConfiguration(),
        config.getMetricsConfiguration(),
        config.getPermissioningConfiguration(),
        config.getKeyFilePath(),
        config.isDevMode(),
        config.getGenesisConfigProvider(),
        config.isP2pEnabled(),
        config.getNetworkingConfiguration(),
        config.isDiscoveryEnabled(),
        config.isBootnodeEligible(),
        config.isRevertReasonEnabled(),
        config.getPlugins(),
        config.getExtraCLIOptions(),
        orion);
  }
}
