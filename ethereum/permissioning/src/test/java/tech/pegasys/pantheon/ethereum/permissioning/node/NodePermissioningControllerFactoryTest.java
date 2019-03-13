/*
 * Copyright 2019 ConsenSys AG.
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
package tech.pegasys.pantheon.ethereum.permissioning.node;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Synchronizer;
import tech.pegasys.pantheon.ethereum.permissioning.NodePermissioningControllerFactory;
import tech.pegasys.pantheon.ethereum.permissioning.PermissioningConfiguration;
import tech.pegasys.pantheon.ethereum.permissioning.SmartContractPermissioningConfiguration;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulator;
import tech.pegasys.pantheon.util.enode.EnodeURL;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NodePermissioningControllerFactoryTest {

  @Mock private Synchronizer synchronizer;
  @Mock private TransactionSimulator transactionSimulator;

  Collection<EnodeURL> bootnodes = Collections.emptyList();
  PermissioningConfiguration localPermissioningConfig;
  SmartContractPermissioningConfiguration smartContractPermissioningConfiguration;

  @Before
  public void setUp() {
    localPermissioningConfig = PermissioningConfiguration.createDefault();
    smartContractPermissioningConfiguration = new SmartContractPermissioningConfiguration();
  }

  @Test
  public void testCreateWithNeitherPermissioningEnabled() {
    NodePermissioningControllerFactory factory = new NodePermissioningControllerFactory();
    NodePermissioningController controller =
        factory.create(
            localPermissioningConfig,
            synchronizer,
            bootnodes,
            smartContractPermissioningConfiguration,
            transactionSimulator);

    List<NodePermissioningProvider> providers = controller.getProviders();
    assertThat(providers.size()).isEqualTo(0);
  }

  @Test
  public void testCreateWithSmartContractNodePermissioningEnabledOnly() {
    smartContractPermissioningConfiguration.setSmartContractAddress(
        Address.fromHexString("0x0000000000000000000000000000000000001234"));
    smartContractPermissioningConfiguration.setSmartContractNodeWhitelistEnabled(true);

    NodePermissioningControllerFactory factory = new NodePermissioningControllerFactory();
    NodePermissioningController controller =
        factory.create(
            localPermissioningConfig,
            synchronizer,
            bootnodes,
            smartContractPermissioningConfiguration,
            transactionSimulator);

    List<NodePermissioningProvider> providers = controller.getProviders();
    assertThat(providers.size()).isEqualTo(1);

    NodePermissioningProvider p1 = providers.get(0);
    assertThat(p1).isNotNull();
  }

  @Test
  public void testCreateWithLocalNodePermissioningEnabledOnly() {
    localPermissioningConfig.setNodeWhitelist(Collections.emptyList());
    localPermissioningConfig.setConfigurationFilePath("fake-file-path");

    NodePermissioningControllerFactory factory = new NodePermissioningControllerFactory();
    NodePermissioningController controller =
        factory.create(
            localPermissioningConfig,
            synchronizer,
            bootnodes,
            smartContractPermissioningConfiguration,
            transactionSimulator);

    List<NodePermissioningProvider> providers = controller.getProviders();
    assertThat(providers.size()).isEqualTo(1);

    NodePermissioningProvider p1 = providers.get(0);
    assertThat(p1).isNotNull();
  }
}
