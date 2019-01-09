package tech.pegasys.pantheon.consensus.ibft.support;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.IbftBlockHashing;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.CommitPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Block;

public class TestHelpers   {

  public static SignedData<CommitPayload> createSignedCommentPayload(final Block block,
      final KeyPair signingKeyPair, final ConsensusRoundIdentifier roundId) {

    final IbftExtraData extraData =
        IbftExtraData.decode(block.getHeader().getExtraData());

    final Signature commitSeal =
        SECP256K1.sign(IbftBlockHashing.calculateDataHashForCommittedSeal(
            block.getHeader(),
            extraData),
            signingKeyPair);

    final MessageFactory messageFactory = new MessageFactory(signingKeyPair);

    return messageFactory.createSignedCommitPayload(roundId, block.getHash(), commitSeal);
  }
}
