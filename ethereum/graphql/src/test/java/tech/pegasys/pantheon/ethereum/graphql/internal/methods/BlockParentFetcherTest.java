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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.graphql.internal.exception.InvalidGraphQLParameters;
import tech.pegasys.pantheon.ethereum.graphql.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.ResponseType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BlockParentFetcherTest extends DataFetcherTester {

  @Mock static BlockchainQueries blockchainQueries;

  @Mock private QueryBlockFetcher method;
  @Mock private BlockParentFetcher method1;
  private GraphQLMethod[] methods = new GraphQLMethod[2];
  private final GraphQLMethodType GRAPHQL_METHOD = GraphQLMethodType.PARENT;
  private final String ZERO_HASH = String.valueOf(Hash.ZERO);

  @Before
  public void setUp() {
    method = mock(QueryBlockFetcher.class);
    when(method.getType()).thenReturn(GraphQLMethodType.BLOCK);
    method1 = mock(BlockParentFetcher.class);
    when(method1.getType()).thenReturn(GRAPHQL_METHOD);
    methods[0] = method;
    methods[1] = method1;
  }

  @Test
  public void returnsCorrectMethodType() {
    assertThat(method1.getType()).isEqualTo(GRAPHQL_METHOD);
  }

  @Test(expected = InvalidGraphQLParameters.class)
  public void exceptionWhenNoParamsSupplied() {
    executeNoQuery(method1);

    verifyNoMoreInteractions(blockchainQueries);
  }

  @Test
  public void returnsWhenParamValid() {
    final GraphQLResponse response =
        executeWithQuery(
            "{ block(hash: \"" + ZERO_HASH + "\") { number parent { hash } } }", methods);

    assertThat(response.getType()).isEqualTo(ResponseType.SUCCESS);
  }
}
