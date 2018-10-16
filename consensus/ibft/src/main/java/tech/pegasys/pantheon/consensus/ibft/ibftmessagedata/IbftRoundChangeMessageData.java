package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded.IbftRoundChangeMessageDecoded;
import tech.pegasys.pantheon.consensus.ibft.protocol.IbftSubProtocol;
import tech.pegasys.pantheon.ethereum.p2p.NetworkMemoryPool;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Optional;

import io.netty.buffer.ByteBuf;

public class IbftRoundChangeMessageData extends AbstractIbftMessageData {

  private static final int MESSAGE_CODE = IbftSubProtocol.NotificationType.ROUND_CHANGE.getValue();

  private IbftRoundChangeMessageData(final ByteBuf data) {
    super(data);
  }

  public static Optional<IbftRoundChangeMessageData> fromMessage(final MessageData message) {
    if (message instanceof IbftRoundChangeMessageData) {
      message.retain();
      return Optional.of((IbftRoundChangeMessageData) message);
    }
    final int code = message.getCode();
    if (code != MESSAGE_CODE) {
      return Optional.empty();
    }

    final ByteBuf data = NetworkMemoryPool.allocate(message.getSize());
    message.writeTo(data);
    return Optional.of(new IbftRoundChangeMessageData(data));
  }

  // NOTE: Alternative interface: IbftPrepareMessageDecoded decode(final MessageData message). This
  // would avoid having to call the constructor and allocate memory for a ByteBuf when all that is
  // required is to decode the message into an IbftPrepareMessageDecoded class
  @Override
  public IbftRoundChangeMessageDecoded decode() {
    return IbftRoundChangeMessageDecoded.readFrom(RLP.input(BytesValue.wrapBuffer(data)));
  }

  public static IbftRoundChangeMessageData create(
      final IbftRoundChangeMessageDecoded ibftRoundChangeMessageDecoded) {

    // RLP encode the message data content (round identifier and getDigest)
    BytesValueRLPOutput rlpEncode = new BytesValueRLPOutput();
    ibftRoundChangeMessageDecoded.writeTo(rlpEncode);

    final ByteBuf data = NetworkMemoryPool.allocate(rlpEncode.encodedSize());
    data.writeBytes(rlpEncode.encoded().extractArray());

    return new IbftRoundChangeMessageData(data);
  }

  @Override
  public int getCode() {
    return MESSAGE_CODE;
  }
}
