package tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.util.bytes.BytesValue;

// NOTE: Implementation of all methods of this class is still pending. This class was added to show
// how a PreparedCertificate is encoded and decoded inside a RoundChange message
public class IbftPrePrepareMessageDecoded extends AbstractIbftInRoundMessageDecoded {

  public IbftPrePrepareMessageDecoded(
      final ConsensusRoundIdentifier roundIdentifier, final Block block, final KeyPair nodeKeys) {
    super(roundIdentifier, Util.publicKeyToAddress(nodeKeys.getPublicKey()));
  }

  public Block getBlock() {
    return null;
  }

  public static IbftPrePrepareMessageDecoded readFrom(final RLPInput rlpInput) {
    return null;
  }

  @Override
  protected Signature getSignature() {
    return null;
  }

  @Override
  protected BytesValue getRlpEncodedMessage() {
    return null;
  }
}
