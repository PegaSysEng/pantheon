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

import tech.pegasys.pantheon.ethereum.graphql.internal.GraphQLRequest;
import tech.pegasys.pantheon.ethereum.graphql.internal.exception.InvalidGraphQLParameters;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLSuccessResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.ResponseType;

import java.io.IOException;

import io.vertx.core.json.Json;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EthBlockMethodTest extends GraphQLTester {

  private EthBlockMethod method;
  private final String ETH_METHOD = "block";
  //  private final String ZERO_HASH = String.valueOf(Hash.ZERO);

  @Before
  public void setUp() {
    method = new EthBlockMethod(blockchainQueries, blockResult);
  }

  @Test
  public void returnsCorrectMethodName() {
    assertThat(method.getType()).isEqualTo(ETH_METHOD);
  }

  @Test
  public void returnsCorrectMethodName1() {
    Long.valueOf("0");
  }

  @Test(expected = InvalidGraphQLParameters.class)
  public void exceptionWhenNoParamsSupplied() {
    final GraphQLRequest request = requestNoQuery();

    executeRequest(request);

    verifyNoMoreInteractions(blockchainQueries);
  }

  @Test
  public void exceptionWhenHashParamInvalid() throws IOException {
    final GraphQLRequest request = requestWithQuery("{ block(hash: \"0x00\") { number } }");

    GraphQLSuccessResponse response = (GraphQLSuccessResponse) executeRequest(request);

    final GraphQLSuccessResponse expected =
        Json.mapper.readValue("{ \"data\": {\"block\": null} }", GraphQLSuccessResponse.class);

    assertThat(response).isEqualToComparingFieldByField(expected);

    verifyNoMoreInteractions(blockchainQueries);
  }

  @Test
  public void returnsWhenHashParamValid() {
    final GraphQLRequest request =
        requestWithQuery(
            "{ block(hash: \"0x00000000000000000000000000000000000000000000000000000000000000\") { number } }");

    GraphQLResponse response = executeRequest(request);

    assertThat(response.getType()).isEqualTo(ResponseType.SUCCESS);

    verifyNoMoreInteractions(blockchainQueries);
  }

  @Test
  public void returnsWhenNumberParamValid() {
    final GraphQLRequest request = requestWithQuery("{ block(number: 0) { number } }");

    GraphQLResponse response = executeRequest(request);

    assertThat(response.getType()).isEqualTo(ResponseType.SUCCESS);

    verifyNoMoreInteractions(blockchainQueries);
  }
}
