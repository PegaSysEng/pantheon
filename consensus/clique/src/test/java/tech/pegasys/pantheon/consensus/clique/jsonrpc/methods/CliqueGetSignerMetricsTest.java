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
package tech.pegasys.pantheon.consensus.clique.jsonrpc.methods;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.consensus.clique.CliqueBlockInterface;
import tech.pegasys.pantheon.consensus.clique.TestHelpers;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.exception.InvalidJsonRpcParameters;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.results.SignerMetricResult;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CliqueGetSignerMetricsTest {

  private final JsonRpcParameter jsonRpcParameter = new JsonRpcParameter();
  private final String CLIQUE_METHOD = "clique_getSignerMetrics";
  private final String JSON_RPC_VERSION = "2.0";
  private CliqueGetSignerMetrics method;

  private BlockchainQueries blockchainQueries;
  private CliqueBlockInterface cliqueBlockInterface;

  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setup() {
    blockchainQueries = mock(BlockchainQueries.class);
    cliqueBlockInterface = mock(CliqueBlockInterface.class);
    method = new CliqueGetSignerMetrics(cliqueBlockInterface, blockchainQueries, jsonRpcParameter);
  }

  @Test
  public void returnsCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(CLIQUE_METHOD);
  }

  @Test
  public void exceptionWhenInvalidStartBlockSupplied() {
    final JsonRpcRequest request = requestWithParams("INVALID");

    expectedException.expect(InvalidJsonRpcParameters.class);
    expectedException.expectMessage("Invalid json rpc parameter at index 0");

    method.response(request);
  }

  @Test
  public void exceptionWhenInvalidEndBlockSupplied() {
    final JsonRpcRequest request = requestWithParams("1", "INVALID");

    expectedException.expect(InvalidJsonRpcParameters.class);
    expectedException.expectMessage("Invalid json rpc parameter at index 1");

    method.response(request);
  }

  @Test
  public void getSignerMetricsWhenNoParams() {

    Long startBlock = 1L;
    Long endBlock = 3L;

    when(blockchainQueries.headBlockNumber()).thenReturn(endBlock);

    List<SignerMetricResult> signerMetricResultList = new java.util.ArrayList<>();

    LongStream.range(startBlock, endBlock)
        .forEach(value -> signerMetricResultList.add(generateBlock(value)));

    final JsonRpcRequest request = requestWithParams();

    final JsonRpcSuccessResponse response = (JsonRpcSuccessResponse) method.response(request);

    LongStream.range(startBlock, endBlock)
        .forEach(value -> verify(blockchainQueries).getBlockHeaderByNumber(value));

    verify(blockchainQueries, atLeast(2)).headBlockNumber();

    signerMetricResultList.forEach(
        signerMetricResult ->
            assertThat(response.getResult().toString()).contains(signerMetricResult.toString()));
  }

  @Test
  public void getSignerMetrics() {

    Long startBlock = 1L;
    Long endBlock = 3L;

    List<SignerMetricResult> signerMetricResultList = new java.util.ArrayList<>();

    LongStream.range(startBlock, endBlock)
        .forEach(value -> signerMetricResultList.add(generateBlock(value)));

    final JsonRpcRequest request =
        requestWithParams(String.valueOf(startBlock), String.valueOf(endBlock));

    final JsonRpcSuccessResponse response = (JsonRpcSuccessResponse) method.response(request);

    LongStream.range(startBlock, endBlock)
        .forEach(value -> verify(blockchainQueries).getBlockHeaderByNumber(value));

    verify(blockchainQueries, never()).headBlockNumber();

    signerMetricResultList.forEach(
        signerMetricResult ->
            assertThat(response.getResult().toString()).contains(signerMetricResult.toString()));
  }

  @Test
  public void getSignerMetricsWithLatest() {

    Long startBlock = 1L;
    Long endBlock = 3L;

    List<SignerMetricResult> signerMetricResultList = new java.util.ArrayList<>();

    when(blockchainQueries.headBlockNumber()).thenReturn(endBlock);

    LongStream.range(startBlock, endBlock)
        .forEach(value -> signerMetricResultList.add(generateBlock(value)));

    final JsonRpcRequest request = requestWithParams(String.valueOf(startBlock), "latest");

    final JsonRpcSuccessResponse response = (JsonRpcSuccessResponse) method.response(request);

    LongStream.range(startBlock, endBlock)
        .forEach(value -> verify(blockchainQueries).getBlockHeaderByNumber(value));

    verify(blockchainQueries).headBlockNumber();

    signerMetricResultList.forEach(
        signerMetricResult ->
            assertThat(response.getResult().toString()).contains(signerMetricResult.toString()));
  }

  @Test
  public void getSignerMetricsWithPending() {

    Long startBlock = 1L;
    Long endBlock = 3L;

    List<SignerMetricResult> signerMetricResultList = new java.util.ArrayList<>();

    when(blockchainQueries.headBlockNumber()).thenReturn(endBlock);

    LongStream.range(startBlock, endBlock)
        .forEach(value -> signerMetricResultList.add(generateBlock(value)));

    final JsonRpcRequest request = requestWithParams(String.valueOf(startBlock), "pending");

    final JsonRpcSuccessResponse response = (JsonRpcSuccessResponse) method.response(request);

    LongStream.range(startBlock, endBlock)
        .forEach(value -> verify(blockchainQueries).getBlockHeaderByNumber(value));

    verify(blockchainQueries).headBlockNumber();

    signerMetricResultList.forEach(
        signerMetricResult ->
            assertThat(response.getResult().toString()).contains(signerMetricResult.toString()));
  }

  @Test
  public void getSignerMetricsWithEarliest() {

    Long startBlock = 0L;
    Long endBlock = 3L;

    List<SignerMetricResult> signerMetricResultList = new java.util.ArrayList<>();

    LongStream.range(startBlock, endBlock)
        .forEach(value -> signerMetricResultList.add(generateBlock(value)));

    final JsonRpcRequest request = requestWithParams("earliest", String.valueOf(endBlock));

    final JsonRpcSuccessResponse response = (JsonRpcSuccessResponse) method.response(request);

    LongStream.range(startBlock, endBlock)
        .forEach(value -> verify(blockchainQueries).getBlockHeaderByNumber(value));

    verify(blockchainQueries, never()).headBlockNumber();

    signerMetricResultList.forEach(
        signerMetricResult ->
            assertThat(response.getResult().toString()).contains(signerMetricResult.toString()));
  }

  private JsonRpcRequest requestWithParams(final Object... params) {
    return new JsonRpcRequest(JSON_RPC_VERSION, CLIQUE_METHOD, params);
  }

  private SignerMetricResult generateBlock(final long number) {
    final SECP256K1.KeyPair proposerKeyPair = SECP256K1.KeyPair.generate();
    final Address proposerAddressBlock = Util.publicKeyToAddress(proposerKeyPair.getPublicKey());

    final BlockHeaderTestFixture builder = new BlockHeaderTestFixture();

    builder.number(number);
    builder.gasLimit(5000);
    builder.timestamp(6000 * number);
    builder.mixHash(
        Hash.fromHexString("0x63746963616c2062797a616e74696e65206661756c7420746f6c6572616e6365"));
    builder.ommersHash(Hash.EMPTY_LIST_HASH);
    builder.nonce(0);
    builder.difficulty(UInt256.ONE);
    builder.coinbase(Util.publicKeyToAddress(proposerKeyPair.getPublicKey()));

    final BlockHeader header =
        TestHelpers.createCliqueSignedBlockHeader(
            builder, proposerKeyPair, singletonList(proposerAddressBlock));

    when(blockchainQueries.getBlockHeaderByNumber(number)).thenReturn(Optional.of(header));
    when(cliqueBlockInterface.getProposerOfBlock(header)).thenReturn(proposerAddressBlock);

    SignerMetricResult signerMetricResult = new SignerMetricResult(proposerAddressBlock);
    signerMetricResult.incrementeNbBlock();
    signerMetricResult.setLastProposedBlockNumber(number);

    return signerMetricResult;
  }
}
