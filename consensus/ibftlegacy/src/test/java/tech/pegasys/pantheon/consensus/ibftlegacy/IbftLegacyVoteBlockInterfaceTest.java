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
package tech.pegasys.pantheon.consensus.ibftlegacy;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Java6Assertions.assertThat;

import tech.pegasys.pantheon.consensus.common.CastVote;
import tech.pegasys.pantheon.consensus.common.ValidatorVotePolarity;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.AddressHelpers;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderBuilder;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockHashFunction;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

public class IbftLegacyVoteBlockInterfaceTest {

  private final KeyPair proposerKeys = KeyPair.generate();
  private final Address proposerAddress = Util.publicKeyToAddress(proposerKeys.getPublicKey());
  private final List<Address> validatorList = singletonList(proposerAddress);

  @Test
  public void headerWithZeroCoinbaseReturnsAnEmptyVote() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();
    // must set "number" to ensure extradata is correctly deserialised during hashing.
    headerBuilder.coinbase(AddressHelpers.ofValue(0)).number(1);
    final IbftLegacyVoteBlockInterface serDeser = new IbftLegacyVoteBlockInterface();

    assertThat(serDeser.extractVoteFromHeader(headerBuilder.buildHeader()))
        .isEqualTo(Optional.empty());
  }

  @Test
  public void headerWithNonceOfZeroReportsDropVote() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();
    // must set "number" to ensure extradata is correctly deserialised during hashing.
    headerBuilder.coinbase(AddressHelpers.ofValue(1)).nonce(0L).number(1);
    final IbftLegacyVoteBlockInterface serDeser = new IbftLegacyVoteBlockInterface();

    final BlockHeader header =
        TestHelpers.createIbftSignedBlockHeader(headerBuilder, proposerKeys, validatorList);
    final Optional<CastVote> extractedVote = serDeser.extractVoteFromHeader(header);

    assertThat(extractedVote).isNotEqualTo(Optional.empty());
    assertThat(extractedVote.get().getRecipient()).isEqualTo(header.getCoinbase());
    assertThat(extractedVote.get().getVotePolarity()).isEqualTo(ValidatorVotePolarity.DROP);
    assertThat(extractedVote.get().getProposer()).isEqualTo(proposerAddress);
  }

  @Test
  public void headerWithNonceOfMaxLongReportsAddVote() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();
    // must set "number" to ensure extradata is correctly deserialised during hashing.
    headerBuilder.coinbase(AddressHelpers.ofValue(2)).nonce(0xFFFFFFFFFFFFFFFFL).number(1);
    final IbftLegacyVoteBlockInterface serDeser = new IbftLegacyVoteBlockInterface();

    final BlockHeader header =
        TestHelpers.createIbftSignedBlockHeader(headerBuilder, proposerKeys, validatorList);
    final Optional<CastVote> extractedVote = serDeser.extractVoteFromHeader(header);

    assertThat(extractedVote).isNotEqualTo(Optional.empty());
    assertThat(extractedVote.get().getRecipient()).isEqualTo(header.getCoinbase());
    assertThat(extractedVote.get().getVotePolarity()).isEqualTo(ValidatorVotePolarity.ADD);
    assertThat(extractedVote.get().getProposer()).isEqualTo(proposerAddress);
  }

  @Test
  public void blendingAddVoteToHeaderResultsInHeaderWithNonceOfMaxLong() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();

    final BlockHeaderBuilder builder =
        BlockHeaderBuilder.fromHeader(headerBuilder.buildHeader())
            .blockHashFunction(MainnetBlockHashFunction::createHash);

    final IbftLegacyVoteBlockInterface serDeser = new IbftLegacyVoteBlockInterface();

    CastVote vote =
        new CastVote(
            ValidatorVotePolarity.ADD, AddressHelpers.ofValue(1), AddressHelpers.ofValue(2));
    final BlockHeaderBuilder builderWithVote =
        serDeser.insertVoteToHeaderBuilder(builder, Optional.of(vote));

    final BlockHeader header = builderWithVote.buildBlockHeader();

    assertThat(header.getCoinbase()).isEqualTo(vote.getRecipient());
    assertThat(header.getNonce()).isEqualTo(0xFFFFFFFFFFFFFFFFL);
  }

  @Test
  public void blendingDropVoteToHeaderResultsInHeaderWithNonceOfZero() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();

    final BlockHeaderBuilder builder =
        BlockHeaderBuilder.fromHeader(headerBuilder.buildHeader())
            .blockHashFunction(MainnetBlockHashFunction::createHash);

    final IbftLegacyVoteBlockInterface serDeser = new IbftLegacyVoteBlockInterface();

    CastVote vote =
        new CastVote(
            ValidatorVotePolarity.DROP, AddressHelpers.ofValue(1), AddressHelpers.ofValue(2));
    final BlockHeaderBuilder builderWithVote =
        serDeser.insertVoteToHeaderBuilder(builder, Optional.of(vote));

    final BlockHeader header = builderWithVote.buildBlockHeader();

    assertThat(header.getCoinbase()).isEqualTo(vote.getRecipient());
    assertThat(header.getNonce()).isEqualTo(0x0L);
  }

  @Test
  public void nonVoteBlendedIntoHeaderResultsInACoinbaseOfZero() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();

    final BlockHeaderBuilder builder =
        BlockHeaderBuilder.fromHeader(headerBuilder.buildHeader())
            .blockHashFunction(MainnetBlockHashFunction::createHash);

    final IbftLegacyVoteBlockInterface serDeser = new IbftLegacyVoteBlockInterface();
    final BlockHeaderBuilder builderWithVote =
        serDeser.insertVoteToHeaderBuilder(builder, Optional.empty());

    final BlockHeader header = builderWithVote.buildBlockHeader();

    assertThat(header.getCoinbase()).isEqualTo(AddressHelpers.ofValue(0));
    assertThat(header.getNonce()).isEqualTo(0x0L);
  }

  @Test
  public void extractsValidatorsFromHeader() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();
    final BlockHeader header =
        TestHelpers.createIbftSignedBlockHeader(headerBuilder, proposerKeys, validatorList);

    final IbftLegacyVoteBlockInterface serDeser = new IbftLegacyVoteBlockInterface();
    final List<Address> extractedValidators = serDeser.validatorsInBlock(header);

    assertThat(extractedValidators).isEqualTo(validatorList);
  }
}
