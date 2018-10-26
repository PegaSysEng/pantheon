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
import tech.pegasys.pantheon.consensus.ibft.IbftHelpers;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Block;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

// Data items used to define how a round will operate
public class IbftRoundContext {

  private final ConsensusRoundIdentifier roundIdentifier;
  private final Collection<Address> validators;
  private final long quorumSize;

  private Optional<SignedData<ProposalPayload>> preprepareMessage = Optional.empty();
  private IbftStates currentState = IbftStates.AWAITING_PROPOSAL;

  private final Map<Address, Signature> commitSeals = Maps.newHashMap();
  // Must track the actual Prepare message, not just the sender, as these may need to be reused
  // to send out in a PrepareCertificate.
  private final Set<SignedData<PreparePayload>> preparedPeers = Sets.newHashSet();

  public IbftRoundContext(
      final ConsensusRoundIdentifier roundIdentifier, final Collection<Address> validators) {
    this.roundIdentifier = roundIdentifier;
    this.validators = validators;
    this.quorumSize = IbftHelpers.calculateRequiredValidatorQuorum(validators.size());
  }

  public void setProposedBlock(final SignedData<ProposalPayload> msg) {
    if (preprepareMessage.isPresent()) {
      throw new RuntimeException("Block has already been provided");
    }

    if (!currentState.equals(IbftStates.AWAITING_PROPOSAL)) {
      throw new RuntimeException("Not awaiting a proposal.");
    }
    preprepareMessage = Optional.of(msg);
    incrementState();
  }

  public boolean shouldSendCommitMessage() {
    // NOTE: The quorumSize for Prepare messages is 1 less than the quorum size as the proposer
    // does not supply a prepare message
    return (preparedPeers.size() >= (quorumSize - 1))
        && getProposedBlock().isPresent()
        && currentState.equals(IbftStates.PREPARING);
  }

  public boolean shouldImportBlock() {
    return (commitSeals.size() >= quorumSize)
        && getProposedBlock().isPresent()
        && currentState.equals(IbftStates.READY);
  }

  public ConsensusRoundIdentifier getRoundIdentifier() {
    return roundIdentifier;
  }

  public void addPreparedPeer(final SignedData<PreparePayload> prepareMsg) {
    this.preparedPeers.add(prepareMsg);
  }

  public void addCommitSeal(final Address committedPeer, final Signature commitSeal) {
    this.commitSeals.put(committedPeer, commitSeal);
  }

  public Optional<Block> getProposedBlock() {
    return preprepareMessage.map(p -> p.getPayload().getBlock());
  }

  public void incrementState() {
    switch (currentState) {
      case AWAITING_PROPOSAL:
        currentState = IbftStates.PREPARING;
        break;
      case PREPARING:
        currentState = IbftStates.READY;
        break;
      case READY:
        currentState = IbftStates.COMPLETED;
        break;
      default:
        throw new RuntimeException("Illegal currentState");
    }
  }

  public Collection<Signature> getCommitSeals() {
    return commitSeals.values();
  }

  public Optional<PreparedCertificate> constructPreparedCertificate() {
    return preprepareMessage
        .map(p -> Optional.of(new PreparedCertificate(p, preparedPeers)))
        .orElse(Optional.empty());
  }
}
