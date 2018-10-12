package net.consensys.pantheon.consensus.clique;

import net.consensys.pantheon.consensus.clique.blockcreation.CliqueProposerSelector;
import net.consensys.pantheon.consensus.common.EpochManager;
import net.consensys.pantheon.consensus.common.ValidatorProvider;
import net.consensys.pantheon.consensus.common.VoteTally;
import net.consensys.pantheon.ethereum.ProtocolContext;
import net.consensys.pantheon.ethereum.chain.Blockchain;
import net.consensys.pantheon.ethereum.core.Address;
import net.consensys.pantheon.ethereum.core.BlockHeader;
import net.consensys.pantheon.ethereum.jsonrpc.internal.queries.BlockWithMetadata;
import net.consensys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;

import java.util.List;
import java.util.Optional;

public class CliqueHelpers {

  public static Address getProposerOfBlock(final BlockHeader header) {
    final CliqueExtraData extraData = CliqueExtraData.decode(header.getExtraData());
    return CliqueBlockHashing.recoverProposerAddress(header, extraData);
  }

  public static Optional<List<Address>> getValidatorsForLastEpochBlock(
      final long blockNumber,
      final EpochManager epochManager,
      final BlockchainQueries blockchainQueries) {
    final long lastEpochBlock = epochManager.getLastEpochBlock(blockNumber);
    return blockchainQueries
        .blockByNumber(lastEpochBlock)
        .map(BlockWithMetadata::getHeader)
        .map(BlockHeader::getExtraData)
        .map(CliqueExtraData::decode)
        .map(CliqueExtraData::getValidators);
  }

  public static Address getProposerForBlockAfter(
      final BlockHeader parent, final VoteTallyCache voteTallyCache) {
    final CliqueProposerSelector proposerSelector = new CliqueProposerSelector(voteTallyCache);
    return proposerSelector.selectProposerForNextBlock(parent);
  }

  public static boolean addressIsAllowedToProduceNextBlock(
      final Address candidate,
      final ProtocolContext<CliqueContext> protocolContext,
      final BlockHeader parent) {
    final VoteTally validatorProvider =
        protocolContext.getConsensusState().getVoteTallyCache().getVoteTallyAtBlock(parent);

    if (!validatorProvider.getCurrentValidators().contains(candidate)) {
      return false;
    }

    final int minimumUnsignedPastBlocks = minimumBlocksSincePreviousSigning(validatorProvider);

    final Blockchain blockchain = protocolContext.getBlockchain();
    int unsignedBlockCount = 0;
    BlockHeader localParent = parent;

    while (unsignedBlockCount < minimumUnsignedPastBlocks) {

      if (localParent.getNumber() == 0) {
        return true;
      }

      final Address parentSigner = CliqueHelpers.getProposerOfBlock(localParent);
      if (parentSigner.equals(candidate)) {
        return false;
      }
      unsignedBlockCount++;

      localParent =
          blockchain
              .getBlockHeader(localParent.getParentHash())
              .orElseThrow(() -> new IllegalStateException("The block was on a orphaned chain."));
    }

    return true;
  }

  private static int minimumBlocksSincePreviousSigning(final ValidatorProvider validatorProvider) {
    final int validatorCount = validatorProvider.getCurrentValidators().size();
    // The number of contiguous blocks in which a signer may only sign 1 (as taken from clique spec)
    final int signerLimit = (validatorCount / 2) + 1;
    return signerLimit - 1;
  }
}
