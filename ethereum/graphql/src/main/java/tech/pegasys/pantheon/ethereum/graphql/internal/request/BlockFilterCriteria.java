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
package tech.pegasys.pantheon.ethereum.graphql.internal.request;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.util.bytes.Bytes32;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlockFilterCriteria {

  private final List<Address> addresses;
  private final List<List<Bytes32>> topcis;

  @JsonCreator
  public BlockFilterCriteria(
      @JsonProperty("addresses") final List<Address> addresses,
      @JsonProperty("topics") final List<List<Bytes32>> topics) {
    this.addresses = addresses;
    this.topcis = topics;
  }

  public List<Address> getAddresses() {
    return addresses;
  }

  public List<List<Bytes32>> getTopcis() {
    return topcis;
  }
}
