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
package tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.methods;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.api.BlockWithMetadata;
import tech.pegasys.pantheon.ethereum.api.TransactionWithMetadata;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.exception.InvalidJsonRpcParameters;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.api.jsonrpc.internal.results.BlockResult;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockBody;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.core.TransactionTestFixture;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EthGetUncleByBlockHashAndIndexTest {

  private final BlockHeaderTestFixture blockHeaderTestFixture = new BlockHeaderTestFixture();
  private final TransactionTestFixture transactionTestFixture = new TransactionTestFixture();

  private EthGetUncleByBlockHashAndIndex method;
  private final Hash zeroHash = Hash.ZERO;

  @Mock private BlockchainQueries blockchainQueries;

  @Before
  public void before() {
    this.method = new EthGetUncleByBlockHashAndIndex(blockchainQueries, new JsonRpcParameter());
  }

  @Test
  public void methodShouldReturnExpectedName() {
    assertThat(method.getName()).isEqualTo("eth_getUncleByBlockHashAndIndex");
  }

  @Test
  public void shouldReturnErrorWhenMissingBlockHashParam() {
    final JsonRpcRequest request = getUncleByBlockHashAndIndex(new Object[] {});

    final Throwable thrown = catchThrowable(() -> method.response(request));

    assertThat(thrown)
        .isInstanceOf(InvalidJsonRpcParameters.class)
        .hasMessage("Missing required json rpc parameter at index 0");
  }

  @Test
  public void shouldReturnErrorWhenMissingIndexParam() {
    final JsonRpcRequest request = getUncleByBlockHashAndIndex(new Object[] {zeroHash});

    final Throwable thrown = catchThrowable(() -> method.response(request));

    assertThat(thrown)
        .isInstanceOf(InvalidJsonRpcParameters.class)
        .hasMessage("Missing required json rpc parameter at index 1");
  }

  @Test
  public void shouldReturnErrorWhenInvalidBlockHashParam() {
    final JsonRpcRequest request = getUncleByBlockHashAndIndex(new Object[] {"not-a-hash"});

    final Throwable thrown = catchThrowable(() -> method.response(request));

    assertThat(thrown)
        .isInstanceOf(InvalidJsonRpcParameters.class)
        .hasMessage("Invalid json rpc parameter at index 0");
  }

  @Test
  public void shouldReturnErrorWhenInvalidIndexParam() {
    final JsonRpcRequest request =
        getUncleByBlockHashAndIndex(new Object[] {zeroHash, "not-an-index"});

    final Throwable thrown = catchThrowable(() -> method.response(request));

    assertThat(thrown)
        .isInstanceOf(InvalidJsonRpcParameters.class)
        .hasMessage("Invalid json rpc parameter at index 1");
  }

  @Test
  public void shouldReturnNullResultWhenBlockDoesNotHaveOmmer() {
    final JsonRpcRequest request = getUncleByBlockHashAndIndex(new Object[] {zeroHash, "0x0"});
    final JsonRpcResponse expectedResponse = new JsonRpcSuccessResponse(null, null);

    when(blockchainQueries.getOmmer(eq(zeroHash), eq(0))).thenReturn(Optional.empty());

    final JsonRpcResponse response = method.response(request);

    assertThat(response).isEqualToComparingFieldByFieldRecursively(expectedResponse);
  }

  @Test
  public void shouldReturnExpectedBlockResult() {
    final JsonRpcRequest request = getUncleByBlockHashAndIndex(new Object[] {zeroHash, "0x0"});
    final BlockHeader header = blockHeaderTestFixture.buildHeader();
    final BlockResult expectedBlockResult = blockResult(header);
    final JsonRpcResponse expectedResponse = new JsonRpcSuccessResponse(null, expectedBlockResult);

    when(blockchainQueries.getOmmer(eq(zeroHash), eq(0))).thenReturn(Optional.of(header));

    final JsonRpcResponse response = method.response(request);

    assertThat(response).isEqualToComparingFieldByFieldRecursively(expectedResponse);
  }

  private BlockResult blockResult(final BlockHeader header) {
    final Block block =
        new Block(header, new BlockBody(Collections.emptyList(), Collections.emptyList()));
    return new BlockResult(
        header,
        Collections.emptyList(),
        Collections.emptyList(),
        UInt256.ZERO,
        block.calculateSize());
  }

  private JsonRpcRequest getUncleByBlockHashAndIndex(final Object[] params) {
    return new JsonRpcRequest("2.0", "eth_getUncleByBlockHashAndIndex", params);
  }

  public BlockWithMetadata<TransactionWithMetadata, Hash> blockWithMetadata(
      final BlockHeader header) {
    final KeyPair keyPair = KeyPair.generate();
    final List<TransactionWithMetadata> transactions = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      final Transaction transaction = transactionTestFixture.createTransaction(keyPair);
      transactions.add(
          new TransactionWithMetadata(transaction, header.getNumber(), header.getHash(), 0));
    }

    final List<Hash> ommers = new ArrayList<>();
    ommers.add(Hash.ZERO);

    return new BlockWithMetadata<>(header, transactions, ommers, header.getDifficulty(), 0);
  }
}
