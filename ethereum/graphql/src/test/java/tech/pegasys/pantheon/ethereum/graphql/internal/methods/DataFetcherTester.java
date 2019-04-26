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

import tech.pegasys.pantheon.ethereum.graphql.internal.GraphQLRequest;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLErrorResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLSuccessResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.schema.GraphQLFactory;

import java.util.List;
import java.util.Map;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.vertx.core.json.Json;
import org.mockito.Mock;

public class DataFetcherTester {

  @Mock static BlockchainQueries blockchainQueries;

  private GraphQL graphQL;

  public DataFetcherTester() {}

  void setGraphql(final List<GraphQLMethod> methods) {
    graphQL = GraphQLFactory.buildGraphQL(methods);
  }

  GraphQLResponse executeRequest(final GraphQLRequest request) {
    ExecutionResult executionResult =
        graphQL.execute(
            ExecutionInput.newExecutionInput()
                .query(request.getQuery())
                .operationName(request.getOperationName())
                .variables(request.getVariables())
                .build());

    System.out.println("error = [" + executionResult.getErrors() + "]");
    System.out.println("error = [" + executionResult.getData() + "]");

    if (executionResult.getErrors().size() == 0) {
      return Json.mapper.convertValue(
          executionResult.toSpecification(), GraphQLSuccessResponse.class);
    } else {
      return new GraphQLErrorResponse(executionResult.getErrors());
    }
  }

  GraphQLResponse executeNoQuery() {
    return executeWithQuery(null);
  }

  GraphQLResponse executeWithQuery(final String query) {
    return executeWithQueryAndVariables(query, null);
  }

  GraphQLResponse executeWithQueryAndVariables(
      final String query, final Map<String, Object> variables) {
    return executeRequest(new GraphQLRequest(null, query, variables));
  }
}
