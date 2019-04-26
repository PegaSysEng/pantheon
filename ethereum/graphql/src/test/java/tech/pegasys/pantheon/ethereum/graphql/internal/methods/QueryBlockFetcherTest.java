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
package tech.pegasys.pantheon.ethereum.graphql.internal.methods;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import tech.pegasys.pantheon.ethereum.graphql.internal.exception.InvalidGraphQLParameters;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLSuccessResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.ResponseType;

import java.io.IOException;
import java.util.Collections;

import io.vertx.core.json.Json;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryBlockFetcherTest extends DataFetcherTester {

  private QueryBlockFetcher method;
  private final GraphQLMethodType GRAPHQL_METHOD = GraphQLMethodType.BLOCK;
  //  private final String ZERO_HASH = String.valueOf(Hash.ZERO);

  @Before
  public void setUp() {
    method = new QueryBlockFetcher(blockchainQueries);
    setGraphql(Collections.singletonList(method));
  }

  @Test
  public void returnsCorrectMethodType() {
    assertThat(method.getType()).isEqualTo(GRAPHQL_METHOD);
  }

  @Test(expected = InvalidGraphQLParameters.class)
  public void exceptionWhenNoParamsSupplied() {
    executeNoQuery();

    verifyNoMoreInteractions(blockchainQueries);
  }

  @Test
  public void exceptionWhenHashParamInvalid() throws IOException {
    final GraphQLSuccessResponse response =
        (GraphQLSuccessResponse) executeWithQuery("{ block(hash: \"0x00\") { number } }");

    final GraphQLSuccessResponse expected =
        Json.mapper.readValue("{ \"data\": {\"block\": null} }", GraphQLSuccessResponse.class);

    assertThat(response).isEqualToComparingFieldByField(expected);

    verifyNoMoreInteractions(blockchainQueries);
  }

  @Test
  public void returnsWhenHashParamValid() {
    final GraphQLResponse response =
        executeWithQuery(
            "{ block(hash: \"0x00000000000000000000000000000000000000000000000000000000000000\") { number } }");

    assertThat(response.getType()).isEqualTo(ResponseType.SUCCESS);

    verifyNoMoreInteractions(blockchainQueries);
  }

  @Test
  public void returnsWhenNumberParamValid() {
    final GraphQLResponse response = executeWithQuery("{ block(number: 0) { number } }");

    assertThat(response.getType()).isEqualTo(ResponseType.SUCCESS);

    verifyNoMoreInteractions(blockchainQueries);
  }
}
