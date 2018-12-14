package tech.pegasys.pantheon.consensus.ibft.blockcreation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.pegasys.pantheon.ethereum.core.InMemoryStorageProvider.createInMemoryWorldStateArchive;

import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import tech.pegasys.pantheon.config.GenesisConfigFile;
import tech.pegasys.pantheon.consensus.common.VoteProposer;
import tech.pegasys.pantheon.consensus.common.VoteTally;
import tech.pegasys.pantheon.consensus.ibft.IbftBlockHashing;
import tech.pegasys.pantheon.consensus.ibft.IbftBlockHeaderValidationRulesetFactory;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraData;
import tech.pegasys.pantheon.consensus.ibft.IbftProtocolSchedule;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.chain.MutableBlockchain;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.AddressHelpers;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.PendingTransactions;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.mainnet.BlockHeaderValidator;
import tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public class IbftBlockCreatorTest {

  @Test
  public void createdBlockPassesValidationRulesAndHasAppropriateHashAndMixHash() {
    // Construct a parent block.
    final BlockHeaderTestFixture blockHeaderBuilder = new BlockHeaderTestFixture();
    blockHeaderBuilder.gasLimit(5000); // required to pass validation rule checks.
    final BlockHeader parentHeader = blockHeaderBuilder.buildHeader();
    final Optional<BlockHeader> optionalHeader = Optional.of(parentHeader);

    // Construct a block chain and world state
    final MutableBlockchain blockchain = mock(MutableBlockchain.class);
    when(blockchain.getChainHeadHash()).thenReturn(parentHeader.getHash());
    when(blockchain.getBlockHeader(any())).thenReturn(optionalHeader);

    final List<Address> initialValidatorList = Lists.newArrayList();
    for(int i = 0; i < 4; i++) {
      initialValidatorList.add(AddressHelpers.ofValue(i));
    }

    final VoteTally voteTally = new VoteTally(initialValidatorList);

    final ProtocolSchedule<IbftContext> protocolSchedule =
        IbftProtocolSchedule.create(
            GenesisConfigFile.fromConfig("{\"config\": {\"spuriousDragonBlock\":0}}")
                .getConfigOptions());
    final ProtocolContext<IbftContext> protContext =new ProtocolContext<>(
        blockchain,
        createInMemoryWorldStateArchive(),
        new IbftContext(voteTally, new VoteProposer()));

    final IbftBlockCreator blockCreator =
        new IbftBlockCreator(
            initialValidatorList.get(0),
            parent ->
                new IbftExtraData(
                    BytesValue.wrap(new byte[32]),
                    Collections.emptyList(),
                    Optional.empty(),
                    0,
                    initialValidatorList)
                    .encode(),
            new PendingTransactions(1),
            protContext,
            protocolSchedule,
            parentGasLimit -> parentGasLimit,
            Wei.ZERO,
            parentHeader);

    final Block block = blockCreator.createBlock(Instant.now().getEpochSecond());

    final BlockHeaderValidator<IbftContext> rules =
        IbftBlockHeaderValidationRulesetFactory.ibftProposedBlockValidator(0);

    final boolean validationResult =
        rules.validateHeader(
            block.getHeader(), parentHeader, protContext, HeaderValidationMode.FULL);

    assertThat(validationResult).isTrue();

    final BlockHeader header = block.getHeader();
    final IbftExtraData extraData = IbftExtraData.decode(header.getExtraData());
    assertThat(block.getHash())
        .isEqualTo(IbftBlockHashing.calculateDataHashForCommittedSeal(header, extraData));
  }

}