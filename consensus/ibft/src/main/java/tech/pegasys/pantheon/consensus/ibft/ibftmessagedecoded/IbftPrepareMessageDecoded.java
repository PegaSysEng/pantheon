package tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.protocol.IbftSubProtocol;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public class IbftPrepareMessageDecoded extends AbstractIbftInRoundMessageDecoded {
  private static final int TYPE = IbftSubProtocol.NotificationType.PREPARE.getValue();
  private final Hash digest;
  private final Signature signature;

  // Used to avoid running another serialisation when the message will be written out.
  // The message data (round, sequence, getDigest) must be serialised everytime that a message is
  // created in order to either compute the getSender address (if the message is read from rlp)  or
  // create the getSignature if the message is created by "us"
  // Alternative approach: decode each field on demand (e.g. when getSender() is called) and cache
  // the value
  private final BytesValue cachedRlpEncodedIbftMessage;

  /** Constructor used when a validator wants to send a message */
  public IbftPrepareMessageDecoded(
      final ConsensusRoundIdentifier roundIdentifier, final Hash digest, final KeyPair nodeKeys) {
    super(roundIdentifier, Util.publicKeyToAddress(nodeKeys.getPublicKey()));
    this.digest = digest;

    cachedRlpEncodedIbftMessage = encodeIbftData(roundIdentifier, digest);
    signature = sign(TYPE, cachedRlpEncodedIbftMessage, nodeKeys);
  }

  /** Constructor used only by the {@link #readFrom(RLPInput)} method */
  private IbftPrepareMessageDecoded(
      final ConsensusRoundIdentifier roundIdentifier,
      final Hash digest,
      final Address sender,
      final Signature signature,
      final BytesValue encodedIbftMessage) {
    super(roundIdentifier, sender);
    this.digest = digest;

    this.signature = signature;
    cachedRlpEncodedIbftMessage = encodedIbftMessage;
  }

  public Hash getDigest() {
    return digest;
  }

  public static IbftPrepareMessageDecoded readFrom(final RLPInput rlpInput) {
    final Address sender;
    final ConsensusRoundIdentifier roundIdentifier;
    final Hash digest;

    rlpInput.enterList();
    RLPInput ibftMessageData = readIbftMessageData(rlpInput);
    Signature signature = readSignature(rlpInput);
    rlpInput.leaveList();

    sender = recoverSender(TYPE, ibftMessageData, signature);

    ibftMessageData.enterList();
    roundIdentifier = ConsensusRoundIdentifier.readFrom(ibftMessageData);
    digest = readDigest(ibftMessageData);
    ibftMessageData.leaveList();

    return new IbftPrepareMessageDecoded(
        roundIdentifier, digest, sender, signature, ibftMessageData.raw());
  }

  @Override
  protected Signature getSignature() {
    return signature;
  }

  @Override
  protected BytesValue getRlpEncodedMessage() {
    return cachedRlpEncodedIbftMessage;
  }

  private static BytesValue encodeIbftData(
      final ConsensusRoundIdentifier roundIdentifier, final Hash digest) {
    // RLP encode of the message data content (round identifier and getDigest)
    BytesValueRLPOutput ibftMessage = new BytesValueRLPOutput();
    ibftMessage.startList();
    roundIdentifier.writeTo(ibftMessage);
    ibftMessage.writeBytesValue(digest);
    ibftMessage.endList();

    return ibftMessage.encoded();
  }
}
