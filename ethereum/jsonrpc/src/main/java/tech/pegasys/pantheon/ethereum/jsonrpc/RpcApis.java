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
package tech.pegasys.pantheon.ethereum.jsonrpc;

import tech.pegasys.pantheon.ethereum.jsonrpc.rpcapi.DEBUG;
import tech.pegasys.pantheon.ethereum.jsonrpc.rpcapi.ETH;
import tech.pegasys.pantheon.ethereum.jsonrpc.rpcapi.MINER;
import tech.pegasys.pantheon.ethereum.jsonrpc.rpcapi.NET;
import tech.pegasys.pantheon.ethereum.jsonrpc.rpcapi.RpcApi;
import tech.pegasys.pantheon.ethereum.jsonrpc.rpcapi.WEB3;

import java.util.Arrays;
import java.util.Collection;

public abstract class RpcApis {
  public static final RpcApi ETH = new ETH();
  public static final RpcApi DEBUG = new DEBUG();
  public static final RpcApi MINER = new MINER();
  public static final RpcApi NET = new NET();
  public static final RpcApi WEB3 = new WEB3();

  public static final Collection<RpcApi> DEFAULT_JSON_RPC_APIS = Arrays.asList(ETH, NET, WEB3);

  public static final RpcApi valueOf(final String name) {
    if (name.equals(ETH.name())) {
      return ETH;
    } else if (name.equals(DEBUG.name())) {
      return DEBUG;
    } else if (name.equals(MINER.name())) {
      return MINER;
    } else if (name.equals(NET.name())) {
      return NET;
    } else if (name.equals(WEB3.name())) {
      return WEB3;
    } else {
      throw new IllegalArgumentException("Unknown RpcApi " + name);
    }
  }

  public static final String getValue(final RpcApi rpcapi) {
    return rpcapi.name();
  }
}
