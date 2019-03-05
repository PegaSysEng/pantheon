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
package tech.pegasys.pantheon.ethereum.p2p;

import tech.pegasys.pantheon.ethereum.p2p.config.DiscoveryConfiguration;
import tech.pegasys.pantheon.ethereum.p2p.config.NetworkingConfiguration;
import tech.pegasys.pantheon.ethereum.p2p.config.RlpxConfiguration;
import tech.pegasys.pantheon.util.NetworkUtility;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkingTestHelper {

  public static NetworkingConfiguration configWithRandomPorts() {
    final String bindHost = getLoopbackBindHost();
    return NetworkingConfiguration.create()
        .setRlpx(RlpxConfiguration.create().setBindHost(bindHost).setBindPort(0))
        .setDiscovery(
            DiscoveryConfiguration.create()
                .setBindHost(bindHost)
                .setBindPort(0)
                .setAdvertisedHost(bindHost));
  }

  public static String getLoopbackBindHost() {
    try {
      if (NetworkUtility.isIPv6Available()) {
        return InetAddress.getByName("::1").getHostAddress();
      } else {
        return InetAddress.getByName("127.0.0.1").getHostAddress();
      }
    } catch (UnknownHostException uhe) {
      // this shouldn't happen unless networking is really screwed. Use fall back 0.0.0.0
      return "0.0.0.0";
    }
  }
}
