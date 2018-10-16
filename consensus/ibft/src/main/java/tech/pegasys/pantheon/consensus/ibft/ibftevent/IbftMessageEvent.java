package tech.pegasys.pantheon.consensus.ibft.ibftevent;

import tech.pegasys.pantheon.consensus.ibft.IbftEvent;
import tech.pegasys.pantheon.consensus.ibft.IbftEvents.Type;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded.AbstractIbftMessageDecoded;

/** Event indicating a new iBFT message has been received */
public final class IbftMessageEvent implements IbftEvent {
  private final AbstractIbftMessageDecoded message;

  public IbftMessageEvent(final AbstractIbftMessageDecoded message) {
    this.message = message;
  }

  @Override
  public Type getType() {
    return Type.IBFT_MESSAGE;
  }

  public AbstractIbftMessageDecoded ibftMessage() {
    return message;
  }
}
