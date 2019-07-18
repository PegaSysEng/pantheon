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
package tech.pegasys.pantheon.consensus.ibft.jsonrpc.methods;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.consensus.ibft.IbftBlockInterface;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraData;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraDataFixture;
import tech.pegasys.pantheon.consensus.ibft.Vote;
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
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IbftGetSignerMetricsTest {

  private static final int NB_KEY_PAIRS = 3;
  private static final SECP256K1.KeyPair[] KEY_PAIRS = new SECP256K1.KeyPair[NB_KEY_PAIRS];

  private final JsonRpcParameter jsonRpcParameter = new JsonRpcParameter();
  private final String IBFT_METHOD = "ibft_getSignerMetrics";
  private final String JSON_RPC_VERSION = "2.0";
  private IbftGetSignerMetrics method;

  private BlockchainQueries blockchainQueries;
  private IbftBlockInterface ibftBlockInterface;

  @Rule public ExpectedException expectedException = ExpectedException.none();

  @BeforeClass
  public static void beforeClass() {
    IntStream.range(0, NB_KEY_PAIRS)
        .forEach(value -> KEY_PAIRS[value] = SECP256K1.KeyPair.generate());
  }

  @Before
  public void setup() {
    blockchainQueries = mock(BlockchainQueries.class);
    ibftBlockInterface = mock(IbftBlockInterface.class);
    method = new IbftGetSignerMetrics(ibftBlockInterface, blockchainQueries, jsonRpcParameter);
  }

  @Test
  public void returnsCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(IBFT_METHOD);
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

    final long startBlock = 1L;
    final long endBlock = 3L;

    when(blockchainQueries.headBlockNumber()).thenReturn(endBlock);

    List<SignerMetricResult> signerMetricResultList = new ArrayList<>();

    LongStream.range(startBlock, endBlock)
        .forEach(value -> signerMetricResultList.add(generateBlock(value)));

    final JsonRpcRequest request = requestWithParams();

    final JsonRpcSuccessResponse response = (JsonRpcSuccessResponse) method.response(request);

    assertThat((Collection<SignerMetricResult>) response.getResult())
        .containsExactlyInAnyOrderElementsOf(signerMetricResultList);
  }

  @Test
  public void getSignerMetrics() {

    final long startBlock = 1L;
    final long endBlock = 5L;

    when(blockchainQueries.headBlockNumber()).thenReturn(endBlock);

    List<SignerMetricResult> signerMetricResultList = new ArrayList<>();

    // sign a first block with keypairs number 1
    SignerMetricResult signerMetricResultFirstKeyPairs = generateBlock(startBlock);
    signerMetricResultList.add(signerMetricResultFirstKeyPairs);
    // sign a second block with keypairs number 2
    SignerMetricResult signerMetricResultSecondKeyPairs = generateBlock(startBlock + 1);
    signerMetricResultList.add(signerMetricResultSecondKeyPairs);
    // sign a third block with keypairs number 3
    SignerMetricResult signerMetricResultThirdKeyPairs = generateBlock(startBlock + 2);
    signerMetricResultList.add(signerMetricResultThirdKeyPairs);
    // sign the last block with the keypairs number 1
    generateBlock(startBlock + 3);
    signerMetricResultFirstKeyPairs.setLastProposedBlockNumber(startBlock + 3);
    signerMetricResultFirstKeyPairs.incrementeNbBlock();

    final JsonRpcRequest request = requestWithParams();

    final JsonRpcSuccessResponse response = (JsonRpcSuccessResponse) method.response(request);

    assertThat((Collection<SignerMetricResult>) response.getResult())
        .containsExactlyInAnyOrderElementsOf(signerMetricResultList);
  }

  @Test
  public void getSignerMetricsWithLatest() {

    final long startBlock = 1L;
    final long endBlock = 3L;

    List<SignerMetricResult> signerMetricResultList = new ArrayList<>();

    when(blockchainQueries.headBlockNumber()).thenReturn(endBlock);

    LongStream.range(startBlock, endBlock)
        .forEach(value -> signerMetricResultList.add(generateBlock(value)));

    final JsonRpcRequest request = requestWithParams(String.valueOf(startBlock), "latest");

    final JsonRpcSuccessResponse response = (JsonRpcSuccessResponse) method.response(request);

    assertThat((Collection<SignerMetricResult>) response.getResult())
        .containsExactlyInAnyOrderElementsOf(signerMetricResultList);
  }

  @Test
  public void getSignerMetricsWithPending() {

    final long startBlock = 1L;
    final long endBlock = 3L;

    List<SignerMetricResult> signerMetricResultList = new ArrayList<>();

    when(blockchainQueries.headBlockNumber()).thenReturn(endBlock);

    LongStream.range(startBlock, endBlock)
        .forEach(value -> signerMetricResultList.add(generateBlock(value)));

    final JsonRpcRequest request = requestWithParams(String.valueOf(startBlock), "pending");

    final JsonRpcSuccessResponse response = (JsonRpcSuccessResponse) method.response(request);

    assertThat((Collection<SignerMetricResult>) response.getResult())
        .containsExactlyInAnyOrderElementsOf(signerMetricResultList);
  }

  @Test
  public void getSignerMetricsWithEarliest() {

    final long startBlock = 0L;
    final long endBlock = 3L;

    List<SignerMetricResult> signerMetricResultList = new ArrayList<>();

    LongStream.range(startBlock, endBlock)
        .forEach(value -> signerMetricResultList.add(generateBlock(value)));

    final JsonRpcRequest request = requestWithParams("earliest", String.valueOf(endBlock));

    final JsonRpcSuccessResponse response = (JsonRpcSuccessResponse) method.response(request);

    assertThat((Collection<SignerMetricResult>) response.getResult())
        .containsExactlyInAnyOrderElementsOf(signerMetricResultList);
  }

  private JsonRpcRequest requestWithParams(final Object... params) {
    return new JsonRpcRequest(JSON_RPC_VERSION, IBFT_METHOD, params);
  }

  private SignerMetricResult generateBlock(final long number) {

    final SECP256K1.KeyPair proposerKeyPair = KEY_PAIRS[(int) (number % KEY_PAIRS.length)];

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

    final IbftExtraData ibftExtraData =
        IbftExtraDataFixture.createExtraData(
            builder.buildHeader(),
            BytesValue.wrap(new byte[IbftExtraData.EXTRA_VANITY_LENGTH]),
            Optional.of(Vote.authVote(Address.fromHexString("1"))),
            singletonList(proposerAddressBlock),
            singletonList(proposerKeyPair),
            0xDEADBEEF);

    builder.extraData(ibftExtraData.encode());

    BlockHeader blockHeader = builder.buildHeader();

    when(blockchainQueries.getBlockHeaderByNumber(number)).thenReturn(Optional.of(blockHeader));
    when(ibftBlockInterface.getProposerOfBlock(blockHeader)).thenReturn(proposerAddressBlock);

    SignerMetricResult signerMetricResult = new SignerMetricResult(proposerAddressBlock);
    signerMetricResult.incrementeNbBlock();
    signerMetricResult.setLastProposedBlockNumber(number);

    return signerMetricResult;
  }
}
