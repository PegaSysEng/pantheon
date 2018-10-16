package tech.pegasys.pantheon.consensus.ibft;

import tech.pegasys.pantheon.consensus.ibft.ibftevent.IbftMessageEvent;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded.AbstractIbftMessageDecoded;

/** Static helper functions for producing and working with IbftEvent objects */
public class IbftEvents {

  public static IbftEvent fromMessage(final AbstractIbftMessageDecoded ibftMessageDecoded) {
    return new IbftMessageEvent(ibftMessageDecoded);
  }

  public enum Type {
    IBFT_MESSAGE,
    ROUND_EXPIRY
  }
}
