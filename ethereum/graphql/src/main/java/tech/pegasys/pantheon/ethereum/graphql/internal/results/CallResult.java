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
package tech.pegasys.pantheon.ethereum.graphql.internal.results;

import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulatorResult;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CallResult implements GraphQLResult {

  private final String data;
  private final BigInteger gasUsed;
  private final BigInteger status;

  @JsonCreator
  public CallResult(
      @JsonProperty("data") final String data,
      @JsonProperty("gasUsed") final BigInteger gasUsed,
      @JsonProperty("status") final BigInteger status) {
    this.data = data;
    this.gasUsed = gasUsed;
    this.status = status;
  }

  public CallResult(final TransactionSimulatorResult result) {
    this(
        result.getOutput().toString(), BigInteger.valueOf(result.getGasEstimate()), BigInteger.ONE);
  }

  public CallResult() {
    this("", BigInteger.ZERO, BigInteger.ZERO);
  }

  @JsonGetter(value = "data")
  public String getData() {
    return data;
  }

  @JsonGetter(value = "gasUsed")
  public BigInteger getGasUsed() {
    return gasUsed;
  }

  @JsonGetter(value = "status")
  public BigInteger getStatus() {
    return status;
  }
}
