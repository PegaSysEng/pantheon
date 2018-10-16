package tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded;

import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

import java.util.Collection;

public class IbftPreparedCertificate {

  private final IbftPrePrepareMessageDecoded ibftPrePrepareMessage;
  private final Collection<IbftPrepareMessageDecoded> ibftPrepareMessages;

  public IbftPreparedCertificate(
      final IbftPrePrepareMessageDecoded ibftPrePrepareMessage,
      final Collection<IbftPrepareMessageDecoded> ibftPrepareMessages) {
    this.ibftPrePrepareMessage = ibftPrePrepareMessage;
    this.ibftPrepareMessages = ibftPrepareMessages;
  }

  public static IbftPreparedCertificate readFrom(final RLPInput rlpInput) {
    final IbftPrePrepareMessageDecoded ibftPrePreparedMessage;
    final Collection<IbftPrepareMessageDecoded> ibftPrepareMessages;
    rlpInput.enterList();
    ibftPrePreparedMessage = IbftPrePrepareMessageDecoded.readFrom(rlpInput);
    ibftPrepareMessages = rlpInput.readList(IbftPrepareMessageDecoded::readFrom);
    rlpInput.leaveList();

    return new IbftPreparedCertificate(ibftPrePreparedMessage, ibftPrepareMessages);
  }

  public void writeTo(final RLPOutput rlpOutput) {
    rlpOutput.startList();
    ibftPrePrepareMessage.writeTo(rlpOutput);
    rlpOutput.writeList(ibftPrepareMessages, IbftPrepareMessageDecoded::writeTo);
    rlpOutput.endList();
  }

  public Collection<IbftPrepareMessageDecoded> getIbftPrepareMessages() {
    return ibftPrepareMessages;
  }

  public IbftPrePrepareMessageDecoded getIbftPrePrepareMessage() {
    return ibftPrePrepareMessage;
  }
}
