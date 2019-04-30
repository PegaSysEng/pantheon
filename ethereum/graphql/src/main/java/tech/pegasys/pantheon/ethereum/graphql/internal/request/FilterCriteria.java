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

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FilterCriteria {

  private final BigInteger fromBlock;
  private final BigInteger toBlock;
  private final List<Address> addresses;
  private final List<List<Bytes32>> topics;

  @SuppressWarnings("unchecked")
  public FilterCriteria(Map<String, Object> filterMap) {
    this.fromBlock =
        filterMap.get("fromBlock") != null ? (BigInteger) filterMap.get("fromBlock") : null;

    this.toBlock = filterMap.get("toBlock") != null ? (BigInteger) filterMap.get("toBlock") : null;
    this.addresses =
        filterMap.get("addresses") != null
            ? (List<Address>) filterMap.get("addresses")
            : Collections.emptyList();
    this.topics =
        filterMap.get("topcis") != null
            ? (List<List<Bytes32>>) filterMap.get("topcis")
            : Collections.emptyList();
  }

  @JsonCreator
  public FilterCriteria(
      @JsonProperty("fromBlock") final BigInteger fromBlock,
      @JsonProperty("toBlock") final BigInteger toBlock,
      @JsonProperty("addresses") final List<Address> addresses,
      @JsonProperty("topics") final List<List<Bytes32>> topics) {
    this.fromBlock = fromBlock;
    this.toBlock = toBlock;
    this.addresses = addresses;
    this.topics = topics;
  }

  public BigInteger getFromBlock() {
    return fromBlock;
  }

  public BigInteger getToBlock() {
    return toBlock;
  }

  public List<Address> getAddresses() {
    return addresses;
  }

  public List<List<Bytes32>> getTopics() {
    return topics;
  }
}
