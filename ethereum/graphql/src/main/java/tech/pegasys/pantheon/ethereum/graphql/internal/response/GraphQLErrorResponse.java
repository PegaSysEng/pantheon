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
package tech.pegasys.pantheon.ethereum.graphql.internal.response;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"errors"})
public class GraphQLErrorResponse implements GraphQLResponse {

  private final GraphQLError[] errors;

  public GraphQLErrorResponse(final GraphQLError... errors) {
    this.errors = errors;
  }

  @JsonGetter("errors")
  public GraphQLError[] getErrors() {
    return errors;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GraphQLErrorResponse that = (GraphQLErrorResponse) o;
    return Arrays.equals(errors, that.errors);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(errors);
  }
}
