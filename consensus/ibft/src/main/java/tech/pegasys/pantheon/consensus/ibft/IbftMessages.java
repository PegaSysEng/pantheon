package tech.pegasys.pantheon.consensus.ibft;

import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.AbstractIbftMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftPrePrepareMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftPrepareMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftRoundChangeMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded.AbstractIbftMessageDecoded;
import tech.pegasys.pantheon.ethereum.p2p.api.Message;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class IbftMessages {

  public static Optional<AbstractIbftMessageDecoded> fromMessage(final Message message) {
    final MessageData messageData = message.getData();

    Optional<? extends AbstractIbftMessageData> optionalIbftMessageData =
        Stream.<Function<MessageData, Optional<? extends AbstractIbftMessageData>>>of(
                IbftPrePrepareMessageData::fromMessage,
                IbftPrepareMessageData::fromMessage,
                IbftRoundChangeMessageData::fromMessage)
            .map(fromMessage -> fromMessage.apply(messageData))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

    return optionalIbftMessageData.map(AbstractIbftMessageData::decode);
  }
}
