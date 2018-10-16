package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded.AbstractIbftMessageDecoded;
import tech.pegasys.pantheon.ethereum.p2p.wire.AbstractMessageData;

import io.netty.buffer.ByteBuf;

public abstract class AbstractIbftMessageData extends AbstractMessageData {
  protected AbstractIbftMessageData(final ByteBuf data) {
    super(data);
  }

  public abstract AbstractIbftMessageDecoded decode();
}
