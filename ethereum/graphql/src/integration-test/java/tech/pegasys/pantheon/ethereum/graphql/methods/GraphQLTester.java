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
package tech.pegasys.pantheon.ethereum.graphql.methods;

import tech.pegasys.pantheon.ethereum.graphql.internal.GraphQLRequest;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.GraphQLMethod;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.QueryBlockFetcher;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLErrorResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLSuccessResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.schema.GraphQLFactory;

import java.util.HashMap;
import java.util.Map;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.vertx.core.json.Json;
import org.mockito.Mock;

public class GraphQLTester {

  @Mock BlockchainQueries blockchainQueries;

  private GraphQL graphQL;
  private Map<String, GraphQLMethod> map = new HashMap<>();

  public GraphQLTester() {
    map.put("block", new QueryBlockFetcher(blockchainQueries));
    this.graphQL = GraphQLFactory.buildGraphQL(map);
  }

  void setMethods(final Map<String, GraphQLMethod> map) {
    this.map = map;
    this.graphQL = GraphQLFactory.buildGraphQL(map);
  }

  GraphQLResponse executeRequest(final GraphQLRequest request) {
    ExecutionResult executionResult =
        graphQL.execute(
            ExecutionInput.newExecutionInput()
                .query(request.getQuery())
                .operationName(request.getOperationName())
                .variables(request.getVariables())
                .build());

    if (executionResult.getErrors().size() == 0) {
      return Json.mapper.convertValue(
          executionResult.toSpecification(), GraphQLSuccessResponse.class);
    } else {
      return new GraphQLErrorResponse(executionResult.getErrors());
    }
  }

  GraphQLRequest requestNoQuery() {
    return requestWithQuery(null);
  }

  GraphQLRequest requestWithQuery(final String query) {
    return requestWithQueryAndVariables(query, null);
  }

  GraphQLRequest requestWithQueryAndVariables(
      final String query, final Map<String, Object> variables) {
    return new GraphQLRequest(null, query, variables);
  }
}
