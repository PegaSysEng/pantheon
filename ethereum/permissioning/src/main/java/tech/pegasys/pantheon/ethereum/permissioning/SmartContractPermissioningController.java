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
import tech.pegasys.pantheon.ethereum.permissioning.node.NodePermissioningProvider;
import tech.pegasys.pantheon.ethereum.transaction.CallParameter;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulator;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulatorResult;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.enode.EnodeURL;

import java.util.Optional;

public class SmartContractPermissioningController implements NodePermissioningProvider {
  private final Address contractAddress;
  private final TransactionSimulator transactionSimulator;

  public SmartContractPermissioningController(
      final Address contractAddress, final TransactionSimulator transactionSimulator) {
    this.contractAddress = contractAddress;
    this.transactionSimulator = transactionSimulator;
  }

  public boolean isPermitted(final String sourceEnodeURL, final String destinationEnodeUrl) {
    return isPermitted(new EnodeURL(sourceEnodeURL), new EnodeURL(destinationEnodeUrl));
  }

  @Override
  private boolean isPermitted(final EnodeURL sourceEnode, final EnodeURL destinationEnode) {
    final BytesValue payload = createPayload(sourceEnode, destinationEnode);
    final CallParameter callParams =
        new CallParameter(null, contractAddress, -1, null, null, payload);
    final Optional<TransactionSimulatorResult> result =
        transactionSimulator.processAtHead(callParams);
    return result.map(r -> false).orElse(false);
  }

  private BytesValue createPayload(final EnodeURL sourceEnode, final EnodeURL destinationEnode) {
    return null;
  }
}
