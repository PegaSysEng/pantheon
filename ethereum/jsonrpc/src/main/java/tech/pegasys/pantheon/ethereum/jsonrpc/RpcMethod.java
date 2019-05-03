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
package tech.pegasys.pantheon.ethereum.jsonrpc;

import java.util.ArrayList;
import java.util.Collection;

public enum RpcMethod {
  ETH_ACCOUNTS("eth_accounts"),
  ETH_BLOCK_NUMBER("eth_blockNumber"),
  ETH_CALL("eth_call"),
  ETH_CHAIN_ID("eth_chainId"),
  ETH_COINBASE("eth_coinbase"),
  ETH_ESTIMATE_GAS("eth_estimateGas"),
  ETH_GAS_PRICE("eth_gasPrice"),
  ETH_GET_BALANCE("eth_getBalance"),
  ETH_GET_BLOCK_BY_HASH("eth_getBlockByHash"),
  ETH_GET_BLOCK_BY_NUMBER("eth_getBlockByNumber"),
  ETH_GET_BLOCK_TRANSACTION_COUNT_BY_HASH("eth_getBlockTransactionCountByHash"),
  ETH_GET_BLOCK_TRANSACTION_COUNT_BY_NUMBER("eth_getBlockTransactionCountByNumber"),
  ETH_GET_CODE("eth_getCode"),
  ETH_GET_FILTER_CHANGES("eth_getFilterChanges"),
  ETH_GET_FILTER_LOGS("eth_getFilterLogs"),
  ETH_GET_LOGS("eth_getLogs"),
  ETH_GET_STORAGE_AT("eth_getStorageAt"),
  NET_ENODE("net_enode"),
  NET_LISTENING("net_listening"),
  NET_PEER_COUNT("net_peerCount"),
  NET_SERVICES("net_services"),
  NET_VERSION("net_version");

  private final String methodName;

  private static Collection<String> allMethodNames;

  public String getMethodName() {
    return methodName;
  }

  static {
    allMethodNames = new ArrayList<>();
    for (RpcMethod m : RpcMethod.values()) {
      allMethodNames.add(m.getMethodName());
    }
  }

  RpcMethod(final String methodName) {
    this.methodName = methodName;
  }

  public static boolean rpcMethodExists(final String rpcMethodName) {
    return allMethodNames.contains(rpcMethodName);
  }
}
