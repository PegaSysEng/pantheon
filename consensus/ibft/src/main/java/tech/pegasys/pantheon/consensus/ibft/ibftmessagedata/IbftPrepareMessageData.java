package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded.IbftPrepareMessageDecoded;
import tech.pegasys.pantheon.consensus.ibft.protocol.IbftSubProtocol;
import tech.pegasys.pantheon.ethereum.p2p.NetworkMemoryPool;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Optional;

import io.netty.buffer.ByteBuf;

public class IbftPrepareMessageData extends AbstractIbftMessageData {

  private static final int MESSAGE_CODE = IbftSubProtocol.NotificationType.PREPARE.getValue();

  private IbftPrepareMessageData(final ByteBuf data) {
    super(data);
  }

  public static Optional<IbftPrepareMessageData> fromMessage(final MessageData message) {
    if (message instanceof IbftPrepareMessageData) {
      message.retain();
      return Optional.of((IbftPrepareMessageData) message);
    }
    final int code = message.getCode();
    if (code != MESSAGE_CODE) {
      return Optional.empty();
    }

    final ByteBuf data = NetworkMemoryPool.allocate(message.getSize());
    message.writeTo(data);
    return Optional.of(new IbftPrepareMessageData(data));
  }

  // NOTE: Alternative interface: IbftPrepareMessageDecoded decode(final MessageData message). This
  // would avoid having to call the constructor and allocate memory for a ByteBuf when all that is
  // required is to decode the message into an IbftPrepareMessageDecoded class
  @Override
  public IbftPrepareMessageDecoded decode() {
    return IbftPrepareMessageDecoded.readFrom(RLP.input(BytesValue.wrapBuffer(data)));
  }

  public static IbftPrepareMessageData create(
      final IbftPrepareMessageDecoded ibftPrepareMessageDecoded) {

    // RLP encode the message data content (round identifier and getDigest)
    BytesValueRLPOutput rlpEncode = new BytesValueRLPOutput();
    ibftPrepareMessageDecoded.writeTo(rlpEncode);

    final ByteBuf data = NetworkMemoryPool.allocate(rlpEncode.encodedSize());
    data.writeBytes(rlpEncode.encoded().extractArray());

    return new IbftPrepareMessageData(data);
  }

  @Override
  public int getCode() {
    return MESSAGE_CODE;
  }
}
