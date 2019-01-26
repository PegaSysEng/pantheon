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
package tech.pegasys.pantheon.util;

import tech.pegasys.pantheon.cli.EthNetworkConfig;
import tech.pegasys.pantheon.ethereum.p2p.config.DiscoveryConfiguration;
import tech.pegasys.pantheon.ethereum.p2p.peers.DefaultPeer;
import tech.pegasys.pantheon.ethereum.p2p.peers.Peer;
import tech.pegasys.pantheon.ethereum.permissioning.PermissioningConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PermissioningConfigurationValidator {

  public static void areAllBootnodesAreInWhitelist(
      final EthNetworkConfig ethNetworkConfig,
      final PermissioningConfiguration permissioningConfiguration)
      throws Exception {
    final List<Peer> bootnodesNotInWhitelist = new ArrayList<Peer>();
    final List<Peer> bootnodes =
        DiscoveryConfiguration.getBootstrapPeersFromGenericCollection(
            ethNetworkConfig.getBootNodes());
    if (permissioningConfiguration.isNodeWhitelistSet() && bootnodes != null) {
      final List<Peer> whitelist =
          permissioningConfiguration
              .getNodeWhitelist()
              .stream()
              .map(DefaultPeer::fromURI)
              .collect(Collectors.toList());
      for (final Peer bootnode : bootnodes) {
        if (!whitelist.contains(bootnode)) {
          bootnodesNotInWhitelist.add(bootnode);
        }
      }
      if (!bootnodesNotInWhitelist.isEmpty()) {
        throw new Exception("Bootnode(s) not in nodes-whitelist " + bootnodesNotInWhitelist);
      }
    }
  }
}
