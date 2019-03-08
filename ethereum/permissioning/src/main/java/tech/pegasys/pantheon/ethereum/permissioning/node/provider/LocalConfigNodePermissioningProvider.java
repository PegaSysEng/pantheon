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
package tech.pegasys.pantheon.ethereum.permissioning.node.provider;

import tech.pegasys.pantheon.ethereum.permissioning.NodeLocalConfigPermissioningController;
import tech.pegasys.pantheon.ethereum.permissioning.node.NodePermissioningProvider;
import tech.pegasys.pantheon.util.enode.EnodeURL;

public class LocalConfigNodePermissioningProvider implements NodePermissioningProvider {

  private NodeLocalConfigPermissioningController localConfigPermissioningController;

  public LocalConfigNodePermissioningProvider(
      final NodeLocalConfigPermissioningController localConfigPermissioningController) {
    this.localConfigPermissioningController = localConfigPermissioningController;
  }

  /**
   * If local config permissioning enabled, check the local config file based permissions.
   *
   * @param sourceEnode the enode source of the packet or connection
   * @param destinationEnode the enode target of the packet or connection
   * @return true, if the communication from sourceEnode to destinationEnode is permitted, false
   *     otherwise
   */
  @Override
  public boolean isPermitted(final EnodeURL sourceEnode, final EnodeURL destinationEnode) {
    return localConfigPermissioningController.isPermitted(sourceEnode)
        && localConfigPermissioningController.isPermitted(destinationEnode);
  }
}
