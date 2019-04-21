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

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EthGetBlockByHashTest {
  //
  //  @Rule public final ExpectedException thrown = ExpectedException.none();
  //
  //  @Mock private BlockchainQueries blockchainQueries;
  //  private final BlockResultFactory blockResult = new BlockResultFactory();
  //  private EthGetBlockByHash method;
  //  private final String ETH_METHOD = "eth_getBlockByHash";
  //  //  private final String ZERO_HASH = String.valueOf(Hash.ZERO);
  //
  //  @Before
  //  public void setUp() {
  //    method = new EthGetBlockByHash(blockchainQueries, blockResult);
  //  }

  //  @Test
  //  public void returnsCorrectMethodName() {
  //    assertThat(method.getType()).isEqualTo(ETH_METHOD);
  //  }
  //
  //    @Test
  //    public void exceptionWhenNoParamsSupplied() {
  //      final GraphQLRequest request = requestWithParams(null, null);
  //
  //      thrown.expect(InvalidGraphQLParameters.class);
  //      thrown.expectMessage("Missing required json rpc parameter at index 0");
  //
  //      method.response(request);
  //
  //      verifyNoMoreInteractions(blockchainQueries);
  //    }

  //    @Test
  //    public void exceptionWhenNoHashSupplied() {
  //      final JsonRpcRequest request = requestWithParams("false");
  //
  //      thrown.expect(InvalidJsonRpcParameters.class);
  //      thrown.expectMessage("Invalid json rpc parameter at index 0");
  //
  //      method.response(request);
  //
  //      verifyNoMoreInteractions(blockchainQueries);
  //    }
  //
  //    @Test
  //    public void exceptionWhenNoBoolSupplied() {
  //      final JsonRpcRequest request = requestWithParams(ZERO_HASH);
  //
  //      thrown.expect(InvalidGraphQlParameters.class);
  //      thrown.expectMessage("Missing required json rpc parameter at index 1");
  //
  //      method.response(request);
  //
  //      verifyNoMoreInteractions(blockchainQueries);
  //    }
  //
  //    @Test
  //    public void exceptionWhenHashParamInvalid() {
  //      final GraphQLRequest request = requestWithParams("hash", "true");
  //
  //      thrown.expect(InvalidJsonRpcParameters.class);
  //      thrown.expectMessage("Invalid json rpc parameter at index 0");
  //
  //      method.response(request);
  //
  //      verifyNoMoreInteractions(blockchainQueries);
  //    }
  //
  //    @Test
  //    public void exceptionWhenBoolParamInvalid() {
  //      final GraphQLRequest request = requestWithParams(ZERO_HASH, "maybe");
  //
  //      thrown.expect(InvalidJsonRpcParameters.class);
  //      thrown.expectMessage("Invalid json rpc parameter at index 1");
  //
  //      method.response(request);
  //
  //      verifyNoMoreInteractions(blockchainQueries);
  //    }
  //
  //    private GraphQLRequest requestWithParams(String query, Map<String,Object> variables) {
  //      return new GraphQLRequest(ETH_METHOD, query, variables);
  //    }
}
