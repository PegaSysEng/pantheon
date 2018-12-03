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
package tech.pegasys.pantheon.consensus.ibft.statemachine;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.IbftBlockHashing;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraData;
import tech.pegasys.pantheon.consensus.ibft.blockcreation.IbftBlockCreator;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.CommitMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.PrepareMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.ProposalMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.RoundChangeMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.CommitPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.consensus.ibft.network.IbftNetworkPeers;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.chain.MinedBlockObserver;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderBuilder;
import tech.pegasys.pantheon.ethereum.core.BlockImporter;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.mainnet.BlockHeaderValidator;
import tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ScheduleBasedBlockHashFunction;
import tech.pegasys.pantheon.util.Subscribers;

import java.time.Clock;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IbftRound {

  private static final Logger LOG = LogManager.getLogger();

  private final Address localAddress;
  private Subscribers<MinedBlockObserver> observers;
  private final IbftRoundContext roundContext;
  private final IbftNetworkPeers networkPeers;
  private final IbftBlockCreator blockCreator;
  private final MessageFactory messageFactory;
  private final BlockHeaderValidator<IbftContext> headerValidator;
  private final ProtocolContext<IbftContext> protocolContext;
  private final ProtocolSchedule<IbftContext> protocolSchedule;
  private final BlockImporter<IbftContext> blockImporter;
  private final KeyPair nodeKeys;

  public IbftRound(
      final IbftRoundContext roundContext,
      final IbftNetworkPeers networkPeers,
      final IbftBlockCreator blockCreator,
      final MessageFactory messageFactory,
      final BlockHeaderValidator<IbftContext> headerValidator,
      final ProtocolSchedule<IbftContext> protocolSchedule,
      final ProtocolContext<IbftContext> protocolContext,
      final BlockImporter<IbftContext> blockImporter,
      final KeyPair nodeKeys,
      final Subscribers<MinedBlockObserver> observers) {
    this.roundContext = roundContext;
    this.networkPeers = networkPeers;
    this.blockCreator = blockCreator;
    this.messageFactory = messageFactory;
    this.protocolSchedule = protocolSchedule;
    this.headerValidator = headerValidator;
    this.protocolContext = protocolContext;
    this.blockImporter = blockImporter;
    this.nodeKeys = nodeKeys;
    this.localAddress = Util.publicKeyToAddress(nodeKeys.getPublicKey());
    this.observers = observers;
  }

  public ConsensusRoundIdentifier getRoundIdentifier() {
    return roundContext.getRoundIdentifier();
  }

  public void createAndSendProposalMessage() {
    LOG.error("Creating proposed block.");
    final Clock clk = Clock.systemUTC();
    final Block block = blockCreator.createBlock(clk.millis() / 1000);

    signAndSendProposedBlock(block);
    updateContextWithProposedBlock(
        messageFactory.createSignedProposalPayload(roundContext.getRoundIdentifier(), block));
  }

  public void handleProposalMessage(final SignedData<ProposalPayload> msg) {
    LOG.error("Received a Proposal message.");
    if (isValidPrePrepare(msg)) {
      final Block block = msg.getPayload().getBlock();
      updateContextWithProposedBlock(msg);
      LOG.error("Sending prepare message.");
      signAndSendPrepareMessage(block);
      peerIsPrepared(
          messageFactory.createSignedPreparePayload(
              roundContext.getRoundIdentifier(), block.getHash()));
    }
  }

  public void handlePrepareMessage(final SignedData<PreparePayload> msg) {
    // if (isValidPrepareMessage(msg)) {}
    LOG.error("Received a prepare message.");
    peerIsPrepared(msg);
  }

  public void handleCommitMessage(final SignedData<CommitPayload> msg) {
    LOG.error("Received a commit message.");
    peerIsCommitted(msg.getSender(), msg.getPayload().getCommitSeal());
  }

  public Optional<PreparedCertificate> createPrepareCertificate() {

    return roundContext.constructPreparedCertificate();
  }

  private void updateContextWithProposedBlock(final SignedData<ProposalPayload> msg) {
    roundContext.setProposedBlock(msg);
    peerIsCommitted(localAddress, createCommitSeal(roundContext.getProposedBlock().get()));
  }

  private void peerIsPrepared(final SignedData<PreparePayload> msg) {
    roundContext.addPreparedPeer(msg);

    if (roundContext.shouldSendCommitMessage()) {
      roundContext.incrementState();
      LOG.error("Sending commit message.");
      signAndSendCommitMessage(roundContext.getProposedBlock().get());
    }
  }

  private void peerIsCommitted(final Address committedPeer, final Signature commitSeal) {
    roundContext.addCommitSeal(committedPeer, commitSeal);
    if (roundContext.shouldImportBlock()) {
      roundContext.incrementState();

      final Block blockToImport = createSealedBlock();

      LOG.error("Importing block to chain.");
      boolean result =
          blockImporter.importBlock(protocolContext, blockToImport, HeaderValidationMode.FULL);
      if (!result) {
        LOG.error("Failed to import block to chain - no idea why.");
      }
      notifyNewBlockListeners(blockToImport);
    }
  }

  private Block createSealedBlock() {
    final Block initialBlock = roundContext.getProposedBlock().get();
    final BlockHeader initialHeader = initialBlock.getHeader();
    final IbftExtraData initialExtraData = IbftExtraData.decode(initialHeader.getExtraData());

    final IbftExtraData sealedExtraData =
        new IbftExtraData(
            initialExtraData.getVanityData(),
            roundContext.getCommitSeals(),
            initialExtraData.getVote(),
            initialExtraData.getRound(),
            initialExtraData.getValidators());

    final BlockHeader sealedHeader =
        BlockHeaderBuilder.fromHeader(initialHeader)
            .extraData(sealedExtraData.encode())
            .blockHashFunction(ScheduleBasedBlockHashFunction.create(protocolSchedule))
            .buildBlockHeader();

    return new Block(sealedHeader, initialBlock.getBody());
  }

  private boolean isValidPrePrepare(final SignedData<ProposalPayload> msg) {
    final ConsensusRoundIdentifier roundIdentifier = roundContext.getRoundIdentifier();
    boolean isRoundZero = roundIdentifier.getRoundNumber() == 0;

    final ConsensusRoundIdentifier msgRoundIdentifier = msg.getPayload().getRoundIdentifier();

    final boolean sequenceAndRoundMatch = roundIdentifier.equals(msgRoundIdentifier);
    final BlockHeader blockHeader = msg.getPayload().getBlock().getHeader();
    final boolean isValidHeader =
        headerValidator.validateHeader(blockHeader, protocolContext, HeaderValidationMode.LIGHT);

    return isRoundZero && sequenceAndRoundMatch && isValidHeader;
  }

  private boolean isValidPrepareMessage(final SignedData<PreparePayload> msg) {
    return false;
  }

  private Signature createCommitSeal(final Block block) {
    final BlockHeader proposedHeader = block.getHeader();
    final IbftExtraData extraData = IbftExtraData.decode(proposedHeader.getExtraData());
    final Hash commitHash =
        IbftBlockHashing.calculateDataHashForCommittedSeal(proposedHeader, extraData);
    return SECP256K1.sign(commitHash, nodeKeys);
  }

  private void signAndSendProposedBlock(final Block block) {
    final SignedData<ProposalPayload> ibftSignedPrePrepareMessageData =
        messageFactory.createSignedProposalPayload(roundContext.getRoundIdentifier(), block);

    final ProposalMessage message = ProposalMessage.create(ibftSignedPrePrepareMessageData);

    networkPeers.multicastToValidators(message);
  }

  private void signAndSendPrepareMessage(final Block block) {
    final Hash hash = block.getHash();
    final SignedData<PreparePayload> prepareMessageData =
        messageFactory.createSignedPreparePayload(roundContext.getRoundIdentifier(), hash);
    final PrepareMessage message = PrepareMessage.create(prepareMessageData);
    networkPeers.multicastToValidators(message);
  }

  private void signAndSendCommitMessage(final Block block) {
    final Hash digest = block.getHash();
    final Signature commitSeal = createCommitSeal(block);
    final SignedData<CommitPayload> signedCommitMsg =
        messageFactory.createSignedCommitPayload(
            roundContext.getRoundIdentifier(), digest, commitSeal);
    final CommitMessage ibftCommitMessage = CommitMessage.create(signedCommitMsg);
    networkPeers.multicastToValidators(ibftCommitMessage);
  }

  private void notifyNewBlockListeners(final Block block) {
    observers.forEach(obs -> obs.blockMined(block));
  }

  private void signAndSendRoundChangeMessaage(final Optional<PreparedCertificate> certificate) {
    final SignedData<RoundChangePayload> signedRoundChange =
        messageFactory.createSignedRoundChangePayload(
            roundContext.getRoundIdentifier(), certificate);
    final RoundChangeMessage ibftRoundChange = RoundChangeMessage.create(signedRoundChange);
    networkPeers.multicastToValidators(ibftRoundChange);
  }
}
