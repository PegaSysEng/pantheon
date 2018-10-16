package tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.PrivateKey;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.nio.charset.Charset;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

public class IbftPrepareMessageDecodedTest {

  private final String HEX_PRIVATE_KEY =
      "8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63";
  private final KeyPair VALIDATOR_KEY_PAIR =
      KeyPair.create(PrivateKey.create(Bytes32.fromHexString(HEX_PRIVATE_KEY)));
  private final Address VALIDATOR_ADDRESS =
      Util.publicKeyToAddress(VALIDATOR_KEY_PAIR.getPublicKey());
  private final int ROUND = 0xFEDCBA98;
  private final long SEQUENCE = 0x1234567890ABCDEFL;
  private final Hash DIGEST =
      Hash.hash(BytesValue.wrap("randomString".getBytes(Charset.defaultCharset())));
  private final String HEX_ENCODED_PREPARE_MESSAGE =
      "f873ef881234567890abcdef84fedcba98a017ea9555c69c9fb4ab30d425cf5fe027a27562344ad65ca0d4ed63ceffafcb72b8419d3ed845f4e6bde242f597a0f8eebbf3038905a87e3a59b7a8ac6ff786316cf211973ec6c94fcbc43802168ad32f7019bc98da73956e261a2d9ceb845f78bc7100";

  @Test
  public void readFromRlp() {
    RLPInput rlp = RLP.input(BytesValue.wrap(Hex.decode(HEX_ENCODED_PREPARE_MESSAGE)));
    IbftPrepareMessageDecoded ibftPrepareMessageDecoded = IbftPrepareMessageDecoded.readFrom(rlp);

    ConsensusRoundIdentifier expecterRoundIdentifier =
        new ConsensusRoundIdentifier(SEQUENCE, ROUND);
    assertThat(ibftPrepareMessageDecoded.getRoundIdentifier())
        .isEqualByComparingTo(expecterRoundIdentifier);
    assertThat(ibftPrepareMessageDecoded.getDigest()).isEqualByComparingTo(DIGEST);
    assertThat(ibftPrepareMessageDecoded.getSender()).isEqualTo(VALIDATOR_ADDRESS);
  }

  @Test
  public void writeToRlp() {

    ConsensusRoundIdentifier roundIdentifier = new ConsensusRoundIdentifier(SEQUENCE, ROUND);

    IbftPrepareMessageDecoded ibftPrepareMessageDecoded =
        new IbftPrepareMessageDecoded(roundIdentifier, DIGEST, VALIDATOR_KEY_PAIR);
    BytesValueRLPOutput rlpOutput = new BytesValueRLPOutput();
    ibftPrepareMessageDecoded.writeTo(rlpOutput);

    byte[] expectedEncoding = Hex.decode(HEX_ENCODED_PREPARE_MESSAGE);
    assertThat(rlpOutput.encoded().extractArray()).isEqualTo(expectedEncoding);
  }

  // NOTE: The following test is quite heavy, but it tests a good number of parameter combinations.
  // It could be split into 4 separate non-nested tests, one for each parameter (sequence, round,
  // diget, getSender)
  @Test
  public void witeToRlpAndReadFromRlpOnMultipleValues() {
    final int roundNumberOfSteps = 5;
    final int roundStepSize = Integer.MAX_VALUE / roundNumberOfSteps;
    int round = 0;

    final long sequenceNumberOfSteps = 5;
    final long sequenceStepSize = Long.MAX_VALUE / sequenceNumberOfSteps;
    long sequence = 0;

    final int numDigests = 5;

    final int numSenders = 5;

    for (int roundIndex = 0;
        roundIndex < roundNumberOfSteps;
        roundIndex++, round += roundStepSize) {
      for (long sequenceIndex = 0;
          sequenceIndex < sequenceNumberOfSteps;
          sequenceIndex++, sequence += sequenceStepSize) {
        for (int digestIndex = 0; digestIndex < numDigests; digestIndex++) {
          for (int senderIndex = 0; senderIndex < numSenders; senderIndex++) {
            PrivateKey privateKey = PrivateKey.create(UInt256.of(senderIndex + 1).getBytes());
            KeyPair validatorKeys = KeyPair.create(privateKey);
            Address validatorAddress = Util.publicKeyToAddress(validatorKeys.getPublicKey());

            Hash digest = Hash.hash(BytesValue.of(digestIndex));
            ConsensusRoundIdentifier expectedRoundIdentifier =
                new ConsensusRoundIdentifier(sequence, round);
            IbftPrepareMessageDecoded expectedIbftPrepareMessageDecoded =
                new IbftPrepareMessageDecoded(expectedRoundIdentifier, digest, validatorKeys);
            BytesValueRLPOutput rlpOutput = new BytesValueRLPOutput();
            expectedIbftPrepareMessageDecoded.writeTo(rlpOutput);

            RLPInput rlpInput = RLP.input(rlpOutput.encoded());
            IbftPrepareMessageDecoded actualIbftPrepareMessageDecoded1 =
                IbftPrepareMessageDecoded.readFrom(rlpInput);

            ConsensusRoundIdentifier actualConsensusRoundIdentifier =
                actualIbftPrepareMessageDecoded1.getRoundIdentifier();
            Hash actualDigest = actualIbftPrepareMessageDecoded1.getDigest();
            Address actualSender = actualIbftPrepareMessageDecoded1.getSender();

            assertThat(actualConsensusRoundIdentifier)
                .isEqualToComparingFieldByField(expectedRoundIdentifier);
            assertThat(actualDigest).isEqualTo(digest);
            assertThat(actualSender).isEqualTo(validatorAddress);
          }
        }
      }
    }
  }
}
