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
package tech.pegasys.pantheon.ethereum.graphql.internal.methods;

import tech.pegasys.pantheon.ethereum.eth.EthProtocol;
import tech.pegasys.pantheon.ethereum.p2p.wire.Capability;

import java.util.OptionalInt;
import java.util.Set;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class QueryProtocolVersionFetcher implements DataFetcher<Integer>, GraphQLMethod {
  private final Integer highestEthVersion;

  public QueryProtocolVersionFetcher(final Set<Capability> supportedCapabilities) {
    final OptionalInt version =
        supportedCapabilities.stream()
            .filter(cap -> EthProtocol.NAME.equals(cap.getName()))
            .mapToInt(Capability::getVersion)
            .max();
    highestEthVersion = version.isPresent() ? version.getAsInt() : null;
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.PROTOCOL_VERSION;
  }

  @Override
  public Integer get(final DataFetchingEnvironment environment) {
    return highestEthVersion;
  }
}
