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
package tech.pegasys.pantheon.ethereum.permissioning;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Synchronizer;
import tech.pegasys.pantheon.ethereum.permissioning.node.NodePermissioningController;
import tech.pegasys.pantheon.ethereum.permissioning.node.NodePermissioningProvider;
import tech.pegasys.pantheon.ethereum.permissioning.node.provider.SyncStatusNodePermissioningProvider;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulator;
import tech.pegasys.pantheon.util.enode.EnodeURL;

import java.util.ArrayList;
import java.util.Collection;

public class NodePermissioningControllerFactory {

  public NodePermissioningController create(
      final PermissioningConfiguration config,
      final Synchronizer synchronizer,
      final Collection<EnodeURL> bootnodes,
      final Address contractAddress,
      final TransactionSimulator transactionSimulator) {
    final SyncStatusNodePermissioningProvider syncStatusProvider =
        new SyncStatusNodePermissioningProvider(synchronizer, bootnodes);
    NodePermissioningProvider[] providers = {};
    int i = 0;
    if (config.isNodeWhitelistEnabled()) {
      NodeLocalConfigPermissioningController localProvider =
          new NodeLocalConfigPermissioningController(config, new ArrayList<>(bootnodes));
      providers[i] = localProvider;
      i++;
    }
    // TODO if smart contract permissioning enabled
    if (true) {
      SmartContractPermissioningController smartContractProvider =
          new SmartContractPermissioningController(contractAddress, transactionSimulator);
      providers[i] = smartContractProvider;
    }
    return new NodePermissioningController(syncStatusProvider, providers);
  }
}
