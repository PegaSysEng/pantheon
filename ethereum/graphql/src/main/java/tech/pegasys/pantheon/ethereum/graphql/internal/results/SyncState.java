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

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SyncState implements GraphQLResult {

  private final BigInteger startingBlock;
  private final BigInteger currentBlock;
  private final BigInteger highestBlock;
  private final BigInteger pulledStates;
  private final BigInteger knownStates;

  @JsonCreator
  public SyncState(
      @JsonProperty("startingBlock") final BigInteger startingBlock,
      @JsonProperty("currentBlock") final BigInteger currentBlock,
      @JsonProperty("highestBlock") final BigInteger highestBlock,
      @JsonProperty("pulledStates") final BigInteger pulledStates,
      @JsonProperty("knownStates") final BigInteger knownStates) {

    this.startingBlock = startingBlock;
    this.currentBlock = currentBlock;
    this.highestBlock = highestBlock;
    this.pulledStates = pulledStates;
    this.knownStates = knownStates;
  }

  @JsonGetter(value = "startingBlock")
  public BigInteger getStartingBlock() {
    return startingBlock;
  }

  @JsonGetter(value = "currentBlock")
  public BigInteger getCurrentBlock() {
    return currentBlock;
  }

  @JsonGetter(value = "highestBlock")
  public BigInteger getHighestBlock() {
    return highestBlock;
  }

  @JsonGetter(value = "pulledStates")
  public BigInteger getPulledStates() {
    return pulledStates;
  }

  @JsonGetter(value = "knownStates")
  public BigInteger getKnownStates() {
    return knownStates;
  }
}
