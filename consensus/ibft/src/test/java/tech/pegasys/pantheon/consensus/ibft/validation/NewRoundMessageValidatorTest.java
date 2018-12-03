/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.consensus.ibft.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.blockcreation.ProposerSelector;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.NewRoundPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangeCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.consensus.ibft.validation.RoundChangeMessageValidator.MessageValidatorFactory;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

public class NewRoundMessageValidatorTest {

  private final KeyPair proposerKey = KeyPair.generate();
  private final KeyPair validatorKey = KeyPair.generate();
  private final KeyPair otherValidatorKey = KeyPair.generate();
  private final MessageFactory proposerMessageFactory = new MessageFactory(proposerKey);
  private final MessageFactory validatorMessageFactory = new MessageFactory(validatorKey);
  private final MessageFactory otherValidatorMessageFactory = new MessageFactory(otherValidatorKey);
  private final Address proposerAdress = Util.publicKeyToAddress(proposerKey.getPublicKey());

  private final List<Address> validators = Lists.newArrayList();

  private final Block proposedBlock = mock(Block.class);
  private final ProposerSelector proposerSelector = mock(ProposerSelector.class);
  private final MessageValidatorFactory validatorFactory = mock(MessageValidatorFactory.class);
  private final MessageValidator messageValidator = mock(MessageValidator.class);
  private final ConsensusRoundIdentifier roundIdentifier = new ConsensusRoundIdentifier(2, 4);
  private final RoundChangeCertificate validCert = mock(RoundChangeCertificate.class);

  @Before
  public void setup() {
    validators.add(Util.publicKeyToAddress(proposerKey.getPublicKey()));
    validators.add(Util.publicKeyToAddress(validatorKey.getPublicKey()));
    validators.add(Util.publicKeyToAddress(otherValidatorKey.getPublicKey()));

    when(proposerSelector.selectProposerForRound(any())).thenReturn(proposerAdress);

    when(validatorFactory.createAt(any())).thenReturn(messageValidator);
    when(messageValidator.addSignedProposalPayload(any())).thenReturn(true);
    when(messageValidator.validatePrepareMessage(any())).thenReturn(true);

    when(proposedBlock.getHash()).thenReturn(Hash.fromHexStringLenient("1"));
  }

  @Test
  public void newRoundFromNonProposerFails() {
    final NewRoundMessageValidator validator =
        new NewRoundMessageValidator(
            validators, proposerSelector, validatorFactory, 1, roundIdentifier);

    final SignedData<NewRoundPayload> msg =
        validatorMessageFactory.createSignedNewRoundPayload(
            roundIdentifier,
            new RoundChangeCertificate(Collections.emptyList()),
            proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock));

    assertThat(validator.validateNewRoundMessage(msg)).isFalse();
  }

  @Test
  public void newRoundTargettingRoundZeroFails() {
    final NewRoundMessageValidator validator =
        new NewRoundMessageValidator(
            validators, proposerSelector, validatorFactory, 1, roundIdentifier);

    final ConsensusRoundIdentifier zerothRound =
        new ConsensusRoundIdentifier(roundIdentifier.getSequenceNumber(), 0);

    final SignedData<NewRoundPayload> msg =
        proposerMessageFactory.createSignedNewRoundPayload(
            zerothRound,
            new RoundChangeCertificate(Collections.emptyList()),
            proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock));

    assertThat(validator.validateNewRoundMessage(msg)).isFalse();
  }

  @Test
  public void newRoundTargettingDifferentSequenceNumberFails() {
    final NewRoundMessageValidator validator =
        new NewRoundMessageValidator(
            validators, proposerSelector, validatorFactory, 1, roundIdentifier);

    final ConsensusRoundIdentifier futureSequence =
        new ConsensusRoundIdentifier(
            roundIdentifier.getSequenceNumber() + 1, roundIdentifier.getRoundNumber());

    final SignedData<NewRoundPayload> msg =
        proposerMessageFactory.createSignedNewRoundPayload(
            futureSequence,
            new RoundChangeCertificate(Collections.emptyList()),
            proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock));

    assertThat(validator.validateNewRoundMessage(msg)).isFalse();
  }

  @Test
  public void newRoundContainingProposalFromDifferentValidatorFails() {
    final NewRoundMessageValidator validator =
        new NewRoundMessageValidator(
            validators, proposerSelector, validatorFactory, 1, roundIdentifier);

    final SignedData<NewRoundPayload> msg =
        proposerMessageFactory.createSignedNewRoundPayload(
            roundIdentifier,
            new RoundChangeCertificate(Collections.emptyList()),
            validatorMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock));

    assertThat(validator.validateNewRoundMessage(msg)).isFalse();
  }

  @Test
  public void newRoundWithEmptyRoundChangeCertificateFails() {
    final NewRoundMessageValidator validator =
        new NewRoundMessageValidator(
            validators, proposerSelector, validatorFactory, 1, roundIdentifier);

    when(validCert.getRoundChangePayloads()).thenReturn(Collections.emptyList());
    final SignedData<NewRoundPayload> msg =
        proposerMessageFactory.createSignedNewRoundPayload(
            roundIdentifier,
            new RoundChangeCertificate(Collections.emptyList()),
            proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock));

    assertThat(validator.validateNewRoundMessage(msg)).isFalse();
  }

  @Test
  public void newRoundWithMismatchOfRoundIdentifierToPrepareMessageFails() {
    final NewRoundMessageValidator validator =
        new NewRoundMessageValidator(
            validators, proposerSelector, validatorFactory, 1, roundIdentifier);

    final ConsensusRoundIdentifier preprepareRoundIdentifier =
        new ConsensusRoundIdentifier(
            roundIdentifier.getSequenceNumber(), roundIdentifier.getRoundNumber() + 1);
    final SignedData<NewRoundPayload> msg =
        proposerMessageFactory.createSignedNewRoundPayload(
            roundIdentifier,
            new RoundChangeCertificate(Collections.emptyList()),
            proposerMessageFactory.createSignedProposalPayload(
                preprepareRoundIdentifier, proposedBlock));

    assertThat(validator.validateNewRoundMessage(msg)).isFalse();
  }

  @Test
  public void newRoundWithMultipleRoundChangeMessagesAllWithoutPrepareCertificateIsSuccessful() {
    final NewRoundMessageValidator validator =
        new NewRoundMessageValidator(
            validators, proposerSelector, validatorFactory, 1, roundIdentifier);

    final SignedData<NewRoundPayload> msg =
        proposerMessageFactory.createSignedNewRoundPayload(
            roundIdentifier,
            new RoundChangeCertificate(
                Lists.newArrayList(
                    proposerMessageFactory.createSignedRoundChangePayload(
                        roundIdentifier, Optional.empty()))),
            proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock));

    assertThat(validator.validateNewRoundMessage(msg)).isTrue();
  }

  @Test
  public void newRoundWithProposalNotMatchingLatestRoundChangeFails() {
    final NewRoundMessageValidator validator =
        new NewRoundMessageValidator(
            validators, proposerSelector, validatorFactory, 1, roundIdentifier);

    final ConsensusRoundIdentifier prevPreparedRound =
        new ConsensusRoundIdentifier(
            roundIdentifier.getSequenceNumber(), roundIdentifier.getRoundNumber() - 1);
    final Block prevProposedBlock = mock(Block.class);
    when(prevProposedBlock.getHash()).thenReturn(Hash.fromHexStringLenient("2"));

    final SignedData<NewRoundPayload> msg =
        proposerMessageFactory.createSignedNewRoundPayload(
            roundIdentifier,
            new RoundChangeCertificate(
                Lists.newArrayList(
                    proposerMessageFactory.createSignedRoundChangePayload(
                        roundIdentifier,
                        Optional.of(
                            new PreparedCertificate(
                                proposerMessageFactory.createSignedProposalPayload(
                                    prevPreparedRound, prevProposedBlock),
                                Collections.emptyList()))))),
            proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock));

    assertThat(validator.validateNewRoundMessage(msg)).isFalse();
  }

  @Test
  public void roundChangeMessagesDoNotAllTargetRoundOfNewRoundMsgFails() {
    final NewRoundMessageValidator validator =
        new NewRoundMessageValidator(
            validators, proposerSelector, validatorFactory, 1, roundIdentifier);

    final SignedData<ProposalPayload> proposal =
        proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock);

    final ConsensusRoundIdentifier preparedRound =
        new ConsensusRoundIdentifier(
            roundIdentifier.getSequenceNumber(), roundIdentifier.getRoundNumber() - 1);
    final SignedData<ProposalPayload> prevProposal =
        proposerMessageFactory.createSignedProposalPayload(preparedRound, proposedBlock);
    final Optional<PreparedCertificate> preparedCert =
        Optional.of(
            new PreparedCertificate(
                prevProposal,
                Lists.newArrayList(
                    validatorMessageFactory.createSignedPreparePayload(
                        preparedRound, proposedBlock.getHash()),
                    otherValidatorMessageFactory.createSignedPreparePayload(
                        preparedRound, proposedBlock.getHash()))));

    final ConsensusRoundIdentifier otherPreparedRound =
        new ConsensusRoundIdentifier(
            roundIdentifier.getSequenceNumber(), roundIdentifier.getRoundNumber() - 2);
    final SignedData<ProposalPayload> otherProposal =
        proposerMessageFactory.createSignedProposalPayload(otherPreparedRound, proposedBlock);
    final Optional<PreparedCertificate> otherPreparedCert =
        Optional.of(
            new PreparedCertificate(
                otherProposal,
                Lists.newArrayList(
                    validatorMessageFactory.createSignedPreparePayload(
                        otherPreparedRound, proposedBlock.getHash()),
                    otherValidatorMessageFactory.createSignedPreparePayload(
                        otherPreparedRound, proposedBlock.getHash()))));

    final SignedData<NewRoundPayload> msg =
        proposerMessageFactory.createSignedNewRoundPayload(
            roundIdentifier,
            new RoundChangeCertificate(
                Lists.newArrayList(
                    proposerMessageFactory.createSignedRoundChangePayload(
                        roundIdentifier, preparedCert),
                    validatorMessageFactory.createSignedRoundChangePayload(
                        otherPreparedRound, otherPreparedCert))),
            proposal);
    proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock);

    assertThat(validator.validateNewRoundMessage(msg)).isFalse();
  }

  @Test
  public void invalidEmbeddedRoundChangeMessageFails() {
    final NewRoundMessageValidator validator =
        new NewRoundMessageValidator(
            validators, proposerSelector, validatorFactory, 1, roundIdentifier);

    final SignedData<ProposalPayload> proposal =
        proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock);

    final Optional<PreparedCertificate> preparedCert =
        Optional.of(
            new PreparedCertificate(
                proposal,
                Lists.newArrayList(
                    validatorMessageFactory.createSignedPreparePayload(
                        roundIdentifier, proposedBlock.getHash()),
                    otherValidatorMessageFactory.createSignedPreparePayload(
                        roundIdentifier, proposedBlock.getHash()))));

    final SignedData<NewRoundPayload> msg =
        proposerMessageFactory.createSignedNewRoundPayload(
            roundIdentifier,
            new RoundChangeCertificate(
                Lists.newArrayList(
                    proposerMessageFactory.createSignedRoundChangePayload(
                        roundIdentifier, preparedCert))),
            proposal);
    proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock);

    assertThat(validator.validateNewRoundMessage(msg)).isFalse();
  }

  @Test
  public void latestPreparedCertificateContainsBlockOtherThanProposalFails() {
    final NewRoundMessageValidator validator =
        new NewRoundMessageValidator(
            validators, proposerSelector, validatorFactory, 1, roundIdentifier);

    final SignedData<ProposalPayload> proposal =
        proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock);

    final Block differentBlock = mock(Block.class);
    when(differentBlock.getHash()).thenReturn(Hash.fromHexStringLenient("3"));
    final ConsensusRoundIdentifier preparedRound =
        new ConsensusRoundIdentifier(
            roundIdentifier.getSequenceNumber(), roundIdentifier.getRoundNumber() - 1);
    final SignedData<ProposalPayload> differentProposal =
        proposerMessageFactory.createSignedProposalPayload(preparedRound, differentBlock);

    final Optional<PreparedCertificate> preparedCert =
        Optional.of(
            new PreparedCertificate(
                differentProposal,
                Lists.newArrayList(
                    validatorMessageFactory.createSignedPreparePayload(
                        roundIdentifier, differentBlock.getHash()),
                    otherValidatorMessageFactory.createSignedPreparePayload(
                        roundIdentifier, differentBlock.getHash()))));

    final SignedData<NewRoundPayload> msg =
        proposerMessageFactory.createSignedNewRoundPayload(
            roundIdentifier,
            new RoundChangeCertificate(
                Lists.newArrayList(
                    proposerMessageFactory.createSignedRoundChangePayload(
                        roundIdentifier, preparedCert))),
            proposal);
    proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock);

    assertThat(validator.validateNewRoundMessage(msg)).isFalse();
  }

  @Test
  public void lastestPreparedCertificateMatchesNewRoundProposalIsSuccessful() {
    final NewRoundMessageValidator validator =
        new NewRoundMessageValidator(
            validators, proposerSelector, validatorFactory, 1, roundIdentifier);

    final SignedData<ProposalPayload> proposal =
        proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock);

    final ConsensusRoundIdentifier preparedRound =
        new ConsensusRoundIdentifier(
            roundIdentifier.getSequenceNumber(), roundIdentifier.getRoundNumber() - 1);
    final SignedData<ProposalPayload> differentProposal =
        proposerMessageFactory.createSignedProposalPayload(preparedRound, proposedBlock);

    final Optional<PreparedCertificate> preparedCert =
        Optional.of(
            new PreparedCertificate(
                differentProposal,
                Lists.newArrayList(
                    validatorMessageFactory.createSignedPreparePayload(
                        roundIdentifier, proposedBlock.getHash()),
                    otherValidatorMessageFactory.createSignedPreparePayload(
                        roundIdentifier, proposedBlock.getHash()))));

    // An earlier PrepareCert is added to ensure the path to find the latest PrepareCert
    // is correctly followed.
    final ConsensusRoundIdentifier earlierPreparedRound =
        new ConsensusRoundIdentifier(
            roundIdentifier.getSequenceNumber(), roundIdentifier.getRoundNumber() - 2);
    final SignedData<ProposalPayload> earlierProposal =
        proposerMessageFactory.createSignedProposalPayload(earlierPreparedRound, proposedBlock);
    final Optional<PreparedCertificate> earlierPreparedCert =
        Optional.of(
            new PreparedCertificate(
                earlierProposal,
                Lists.newArrayList(
                    validatorMessageFactory.createSignedPreparePayload(
                        earlierPreparedRound, proposedBlock.getHash()),
                    otherValidatorMessageFactory.createSignedPreparePayload(
                        earlierPreparedRound, proposedBlock.getHash()))));

    final SignedData<NewRoundPayload> msg =
        proposerMessageFactory.createSignedNewRoundPayload(
            roundIdentifier,
            new RoundChangeCertificate(
                Lists.newArrayList(
                    proposerMessageFactory.createSignedRoundChangePayload(
                        roundIdentifier, earlierPreparedCert),
                    proposerMessageFactory.createSignedRoundChangePayload(
                        roundIdentifier, preparedCert))),
            proposal);
    proposerMessageFactory.createSignedProposalPayload(roundIdentifier, proposedBlock);

    assertThat(validator.validateNewRoundMessage(msg)).isTrue();
  }
}
