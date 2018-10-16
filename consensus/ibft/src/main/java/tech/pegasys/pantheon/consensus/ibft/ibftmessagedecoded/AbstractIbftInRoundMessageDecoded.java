package tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.ethereum.core.Address;

public abstract class AbstractIbftInRoundMessageDecoded extends AbstractIbftMessageDecoded {
  protected final ConsensusRoundIdentifier roundIdentifier;

  protected AbstractIbftInRoundMessageDecoded(
      final ConsensusRoundIdentifier roundIdentifier, final Address sender) {
    super(sender);
    this.roundIdentifier = roundIdentifier;
  }

  public ConsensusRoundIdentifier getRoundIdentifier() {
    return roundIdentifier;
  }
}
