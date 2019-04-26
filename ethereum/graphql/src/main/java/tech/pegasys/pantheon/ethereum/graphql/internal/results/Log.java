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

import tech.pegasys.pantheon.ethereum.graphql.internal.queries.LogWithMetadata;
import tech.pegasys.pantheon.ethereum.rlp.RLP;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Log implements GraphQLResult {

  private final Integer index;
  private Account account;
  private final List<String> topics;
  private final String data;
  private Transaction transaction;

  public Log(final LogWithMetadata logWithMetadata) {
    this(
        logWithMetadata.getLogIndex(),
        logWithMetadata.getTopics().stream()
            .map(topic -> RLP.encode(topic::writeTo).toString())
            .collect(Collectors.toList()),
        logWithMetadata.getData().toString());
  }

  @JsonCreator
  public Log(
      @JsonProperty("index") final Integer index,
      @JsonProperty("topics") final List<String> topics,
      @JsonProperty("data") final String data) {
    this.index = index;
    this.account = null;
    this.topics = topics;
    this.data = data;
    this.transaction = null;
  }

  @JsonGetter(value = "index")
  public Integer getIndex() {
    return index;
  }

  @JsonGetter(value = "account")
  public Account getAccount() {
    return account;
  }

  @JsonGetter(value = "topics")
  public List<String> getTopics() {
    return topics;
  }

  @JsonGetter(value = "data")
  public String getData() {
    return data;
  }

  @JsonGetter(value = "transaction")
  public Transaction getTransaction() {
    return transaction;
  }
}
