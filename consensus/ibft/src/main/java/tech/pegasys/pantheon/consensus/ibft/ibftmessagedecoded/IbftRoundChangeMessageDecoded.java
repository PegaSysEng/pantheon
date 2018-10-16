package tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.protocol.IbftSubProtocol;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Optional;

public class IbftRoundChangeMessageDecoded extends AbstractIbftMessageDecoded {

  private static final int TYPE = IbftSubProtocol.NotificationType.PREPARE.getValue();
  private final ConsensusRoundIdentifier roundChangeIdentifier;

  // The validator may not hae any prepared certificate
  private final Optional<IbftPreparedCertificate> preparedCertificate;

  // Used to avoid running another serialisation when the message will be written out.
  // The message data (round, sequence, prepared certificate) must be serialised everytime that a
  // message is created in order to either compute the getSender address (if the message is read
  // from rlp)  or create the getSignature if the message is created by "us"
  // Alternative approach: decode each field on demand (e.g. when getSender() is called) and cache
  // the value
  private final BytesValue cachedRlpEncodedIbftMessage;
  private final Signature signature;

  /** Constructor used when a validator wants to send a message */
  public IbftRoundChangeMessageDecoded(
      final ConsensusRoundIdentifier roundIdentifier,
      final Optional<IbftPreparedCertificate> preparedCertificate,
      final KeyPair nodeKeys) {
    super(Util.publicKeyToAddress(nodeKeys.getPublicKey()));
    this.roundChangeIdentifier = roundIdentifier;
    this.preparedCertificate = preparedCertificate;

    cachedRlpEncodedIbftMessage = encodeIbftData(roundIdentifier, preparedCertificate);
    signature = sign(TYPE, cachedRlpEncodedIbftMessage, nodeKeys);
  }

  /** Constructor used only by the {@link #readFrom(RLPInput)} method */
  private IbftRoundChangeMessageDecoded(
      final ConsensusRoundIdentifier roundIdentifier,
      final Optional<IbftPreparedCertificate> preparedCertificate,
      final Address sender,
      final Signature signagure,
      final BytesValue encodedIbftMessage) {
    super(sender);
    this.roundChangeIdentifier = roundIdentifier;
    this.preparedCertificate = preparedCertificate;

    cachedRlpEncodedIbftMessage = encodedIbftMessage;
    this.signature = signagure;
  }

  public ConsensusRoundIdentifier getRoundChangeIdentifier() {
    return roundChangeIdentifier;
  }

  public Optional<IbftPreparedCertificate> getPreparedCertificate() {
    return preparedCertificate;
  }

  public static IbftRoundChangeMessageDecoded readFrom(final RLPInput rlpInput) {

    final Address sender;
    final ConsensusRoundIdentifier roundIdentifier;
    final Optional<IbftPreparedCertificate> preparedCertificate;

    rlpInput.enterList();
    RLPInput ibftMessageData = readIbftMessageData(rlpInput);
    Signature signature = readSignature(rlpInput);
    rlpInput.leaveList();

    sender = recoverSender(TYPE, ibftMessageData, signature);

    ibftMessageData.enterList();
    roundIdentifier = ConsensusRoundIdentifier.readFrom(ibftMessageData);
    if (ibftMessageData.nextIsNull()) {
      preparedCertificate = Optional.empty();
    } else {
      preparedCertificate = Optional.of(IbftPreparedCertificate.readFrom(ibftMessageData));
    }
    ibftMessageData.leaveList();
    return new IbftRoundChangeMessageDecoded(
        roundIdentifier, preparedCertificate, sender, signature, ibftMessageData.raw());
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
      final ConsensusRoundIdentifier roundIdentifier,
      final Optional<IbftPreparedCertificate> preparedCertificate) {
    // RLP encode of the message data content (round identifier and prepared certificate)
    BytesValueRLPOutput ibftMessage = new BytesValueRLPOutput();
    ibftMessage.startList();
    roundIdentifier.writeTo(ibftMessage);

    if (preparedCertificate.isPresent()) {
      preparedCertificate.get().writeTo(ibftMessage);
    } else {
      ibftMessage.writeNull();
    }
    ibftMessage.endList();

    return ibftMessage.encoded();
  }
}
