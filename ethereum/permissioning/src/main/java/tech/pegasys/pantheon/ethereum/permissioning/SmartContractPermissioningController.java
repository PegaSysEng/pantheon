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

import tech.pegasys.pantheon.crypto.Hash;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.permissioning.node.NodePermissioningProvider;
import tech.pegasys.pantheon.ethereum.transaction.CallParameter;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulator;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulatorResult;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.bytes.BytesValues;
import tech.pegasys.pantheon.util.enode.EnodeURL;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Optional;

public class SmartContractPermissioningController implements NodePermissioningProvider {
  private final Address contractAddress;
  private final TransactionSimulator transactionSimulator;

  private static final String IPV4_FUNCTION_SIGNATURE =
      "connectionAllowedIpv4(bytes32,bytes32,bytes4,uint16,bytes32,bytes32,bytes4,uint16)";
  private static final BytesValue IPV4_FUNCTION_SIGNATURE_HASH =
      hashSignature(IPV4_FUNCTION_SIGNATURE);
  private static final String IPV6_FUNCTION_SIGNATURE =
      "connectionAllowedIpv6(bytes32,bytes32,bytes16,uint16,bytes32,bytes32,bytes16,uint16)";
  private static final BytesValue IPV6_FUNCTION_SIGNATURE_HASH =
      hashSignature(IPV6_FUNCTION_SIGNATURE);

  private static BytesValue hashSignature(final String signature) {
    return Hash.keccak256(BytesValue.of(signature.getBytes())).slice(0, 4);
  }

  public SmartContractPermissioningController(
      final Address contractAddress, final TransactionSimulator transactionSimulator) {
    this.contractAddress = contractAddress;
    this.transactionSimulator = transactionSimulator;
  }

  public boolean isPermitted(final String sourceEnodeURL, final String destinationEnodeUrl) {
    return isPermitted(new EnodeURL(sourceEnodeURL), new EnodeURL(destinationEnodeUrl));
  }

  @Override
  public boolean isPermitted(final EnodeURL sourceEnode, final EnodeURL destinationEnode) {
    final BytesValue payload = createPayload(sourceEnode, destinationEnode);
    final CallParameter callParams =
        new CallParameter(null, contractAddress, -1, null, null, payload);

    final Optional<TransactionSimulatorResult> result =
        transactionSimulator.processAtHead(callParams);

    return result
        .map(r -> checkTransactionResult(r.getOutput()))
        .orElseThrow(() -> new IllegalStateException("the hell")); // .orElse(false);
  }

  private Boolean checkTransactionResult(BytesValue result) {
    if (result.size() != 32) {
      throw new IllegalArgumentException("Unexpected result size");
    }
    byte[] trueValue = new byte[32];
    trueValue[31] = (byte) (0xFF & 1L);
    if (result.isZero()) {
      return false;
    } else if (result.compareTo(BytesValue.wrap(trueValue)) == 0) {
      return true;
    } else {
      throw new IllegalStateException("Unexpected result form");
    }
  }

  private BytesValue createPayload(final EnodeURL sourceEnode, final EnodeURL destinationEnode) {
    final BytesValue signature;
    if (sourceEnode.getInetAddress() instanceof Inet4Address
        && destinationEnode.getInetAddress() instanceof Inet4Address) {
      signature = IPV4_FUNCTION_SIGNATURE_HASH;
    } else if (sourceEnode.getInetAddress() instanceof Inet6Address
        && destinationEnode.getInetAddress() instanceof Inet6Address) {
      signature = IPV6_FUNCTION_SIGNATURE_HASH;
    } else {
      throw new IllegalArgumentException(
          "No payload possible for checking an ipv4 to ipv6 connection");
    }
    return BytesValues.concatenate(
        signature, encodeEnodeUrl(sourceEnode), encodeEnodeUrl(destinationEnode));
  }

  private BytesValue encodeEnodeUrl(final EnodeURL enode) {
    final BytesValue id =
        encodeEnodeId(enode.getNodeId());
    final BytesValue ip =
        encodeIp(enode.getInetAddress());
    final BytesValue port =
        encodePort(enode.getListeningPort());
    return BytesValues.concatenate(
        id, ip, port);
  }

  private BytesValue encodeIp(final InetAddress addr) {
    final byte[] address = addr.getAddress();
    final byte[] res = new byte[32];
    System.arraycopy(address, 0, res, 0, address.length);
    return BytesValue.wrap(res);
  }

  private BytesValue encodePort(final Integer port) {
    final byte[] res = new byte[32];
    res[31] = (byte) ((port) & 0xFF);
    res[30] = (byte) ((port >> 8) & 0xFF);
    return BytesValue.wrap(res);
  }

  private BytesValue encodeEnodeId(String id) {
    return BytesValue.fromHexString(id);
  }
}
