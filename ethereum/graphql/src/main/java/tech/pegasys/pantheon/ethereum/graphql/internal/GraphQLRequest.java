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
package tech.pegasys.pantheon.ethereum.graphql.internal;

import tech.pegasys.pantheon.ethereum.graphql.internal.exception.InvalidGraphQLParameters;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQLRequest {

  private final String operationName;
  private final String query;
  private final Map<String, Object> variables;

  @JsonCreator
  public GraphQLRequest(
      @JsonProperty("operationName") final String operationName,
      @JsonProperty("query") final String query,
      @JsonProperty("variables") final Map<String, Object> variables) {
    this.operationName = operationName;
    this.query = query;
    this.variables = variables;
    if (query == null) {
      throw new InvalidGraphQLParameters("Must provide query string.");
    }
  }

  @JsonGetter("operationName")
  public String getOperationName() {
    return operationName;
  }

  @JsonGetter("query")
  public String getQuery() {
    return query;
  }

  @JsonGetter("variables")
  public Map<String, Object> getVariables() {
    return variables;
  }
}
