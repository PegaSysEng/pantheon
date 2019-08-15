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
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters;

import tech.pegasys.pantheon.ethereum.core.Address;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EeaGetTransactionCountParameters {

  private final String privateFrom;
  private final String[] privateFor;
  private final Address address;

  @JsonCreator
  public EeaGetTransactionCountParameters(
      @JsonProperty("privateFrom") final String privateFrom,
      @JsonProperty("privateFor") final String[] privateFor,
      @JsonProperty("address") final Address address) {
    this.privateFrom = privateFrom;
    this.privateFor = privateFor;
    this.address = address;
  }

  public String getPrivateFrom() {
    return privateFrom;
  }

  public String[] getPrivateFor() {
    return privateFor;
  }

  public Address getAddress() {
    return address;
  }
}
