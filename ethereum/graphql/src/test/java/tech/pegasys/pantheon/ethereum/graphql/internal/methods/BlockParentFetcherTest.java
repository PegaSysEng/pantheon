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
import tech.pegasys.pantheon.ethereum.graphql.internal.response.ResponseType;

import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BlockParentFetcherTest extends DataFetcherTester {

  private static BlockParentFetcher method;
  private final String ETH_METHOD = "parent";
  //  private final String ZERO_HASH = String.valueOf(Hash.ZERO);

  @BeforeClass
  public static void setUpOnce() {
    method = new BlockParentFetcher(blockchainQueries);

    setGraphql(
        new HashMap<String, GraphQLMethod>() {
          {
            put("block", new QueryBlockFetcher(blockchainQueries));
            put("parent", method);
          }
        });
  }

  @Test
  public void returnsCorrectMethodName() {
    assertThat(method.getType()).isEqualTo(ETH_METHOD);
  }

  @Test(expected = InvalidGraphQLParameters.class)
  public void exceptionWhenNoParamsSupplied() {
    executeNoQuery();

    verifyNoMoreInteractions(blockchainQueries);
  }

  @Test
  public void returnsWhenParamValid() {
    final GraphQLResponse response =
        executeWithQuery(
            "{ block(hash: \"0x00000000000000000000000000000000000000000000000000000000000000\") { number parent { hash } } }");

    assertThat(response.getType()).isEqualTo(ResponseType.SUCCESS);

    verifyNoMoreInteractions(blockchainQueries);
  }
}
