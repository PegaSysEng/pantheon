package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded.IbftPrepareMessageDecoded;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.PrivateKey;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.p2p.NetworkMemoryPool;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.ethereum.p2p.wire.AbstractMessageData;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.nio.charset.Charset;
import java.util.Optional;

import io.netty.buffer.ByteBuf;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

public class IbftPrepareMessageDataTest {

  private final String HEX_PRIVATE_KEY =
      "8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63";
  private final KeyPair VALIDATOR_KEY_PAIR =
      KeyPair.create(PrivateKey.create(Bytes32.fromHexString(HEX_PRIVATE_KEY)));
  private final Address VALIDATOR_ADDRESS =
      Util.publicKeyToAddress(VALIDATOR_KEY_PAIR.getPublicKey());
  private final int MESSAGE_CODE = 1;
  private final int ROUND = 0xFEDCBA98;
  private final long SEQUENCE = 0x1234567890ABCDEFL;
  private final Hash DIGEST =
      Hash.hash(BytesValue.wrap("randomString".getBytes(Charset.defaultCharset())));
  private final String HEX_ENCODED_PREPARE_MESSAGE =
      "f873ef881234567890abcdef84fedcba98a017ea9555c69c9fb4ab30d425cf5fe027a27562344ad65ca0d4ed63ceffafcb72b8419d3ed845f4e6bde242f597a0f8eebbf3038905a87e3a59b7a8ac6ff786316cf211973ec6c94fcbc43802168ad32f7019bc98da73956e261a2d9ceb845f78bc7100";

  // NOTE: The following tests heavily rely on the IbftPrepareMessageDecoded class. I couldn't come
  // up with a better way to test the IbftPrepareMessageData class without relying on the
  // functionality provided by the IbftPrepareMessageDecoded
  @Test
  public void messageCreationFromGenericMesssageData() {
    BytesValue encodedPrepareMessage = BytesValue.fromHexString(HEX_ENCODED_PREPARE_MESSAGE);

    final ByteBuf dataByteBuf = NetworkMemoryPool.allocate(encodedPrepareMessage.size());
    dataByteBuf.writeBytes(Hex.decode(HEX_ENCODED_PREPARE_MESSAGE));

    MessageData messageData =
        new AbstractMessageData(dataByteBuf) {
          @Override
          public int getCode() {
            return MESSAGE_CODE;
          }
        };

    Optional<IbftPrepareMessageData> ibftPrepareMessage =
        IbftPrepareMessageData.fromMessage(messageData);

    assertThat(ibftPrepareMessage.isPresent()).isTrue();
    assertThat(ibftPrepareMessage.get().getCode()).isEqualTo(MESSAGE_CODE);

    IbftPrepareMessageDecoded ibftPrepareMessageDecoded = ibftPrepareMessage.get().decode();

    ConsensusRoundIdentifier expecterRoundIdentifier =
        new ConsensusRoundIdentifier(SEQUENCE, ROUND);
    assertThat(ibftPrepareMessageDecoded.getRoundIdentifier())
        .isEqualByComparingTo(expecterRoundIdentifier);
    assertThat(ibftPrepareMessageDecoded.getDigest()).isEqualByComparingTo(DIGEST);
    assertThat(ibftPrepareMessageDecoded.getSender()).isEqualTo(VALIDATOR_ADDRESS);
  }

  @Test
  public void emptyOptionalIsReturnedOnCreationIfCodeIsNotCorrect() {
    BytesValue encodedPrepareMessage = BytesValue.fromHexString(HEX_ENCODED_PREPARE_MESSAGE);

    final ByteBuf dataByteBuf = NetworkMemoryPool.allocate(encodedPrepareMessage.size());
    dataByteBuf.writeBytes(Hex.decode(HEX_ENCODED_PREPARE_MESSAGE));

    MessageData messageData =
        new AbstractMessageData(dataByteBuf) {
          @Override
          public int getCode() {
            return MESSAGE_CODE + 1;
          }
        };

    Optional<IbftPrepareMessageData> ibftPrepareMessage =
        IbftPrepareMessageData.fromMessage(messageData);

    assertThat(ibftPrepareMessage.isPresent()).isFalse();
  }

  @Test
  public void writeToByteBuf() {

    ConsensusRoundIdentifier roundIdentifier = new ConsensusRoundIdentifier(SEQUENCE, ROUND);

    IbftPrepareMessageDecoded ibftPrepareMessageDecoded =
        new IbftPrepareMessageDecoded(roundIdentifier, DIGEST, VALIDATOR_KEY_PAIR);

    IbftPrepareMessageData ibftPrepareMessageData =
        IbftPrepareMessageData.create(ibftPrepareMessageDecoded);

    final ByteBuf dataByteBuf = NetworkMemoryPool.allocate(ibftPrepareMessageData.getSize());
    ibftPrepareMessageData.writeTo(dataByteBuf);

    byte[] expectedEncoding = Hex.decode(HEX_ENCODED_PREPARE_MESSAGE);

    assertThat(byteBufToByteArray(dataByteBuf)).isEqualTo(expectedEncoding);
  }

  private byte[] byteBufToByteArray(final ByteBuf dataByteBuf) {
    byte[] bytes = new byte[dataByteBuf.readableBytes()];
    int readerIndex = dataByteBuf.readerIndex();
    dataByteBuf.getBytes(readerIndex, bytes);

    return bytes;
  }
}
