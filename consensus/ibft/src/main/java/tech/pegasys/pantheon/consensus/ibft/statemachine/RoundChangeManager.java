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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.Set;
import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.IbftHelpers;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangeCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.consensus.ibft.validation.RoundChangeMessageValidator;
import tech.pegasys.pantheon.consensus.ibft.validation.RoundChangeMessageValidator.MessageValidatorFactory;
import tech.pegasys.pantheon.ethereum.core.Address;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for handling all RoundChange messages received for a given block height
 * (theoretically, RoundChange messages for a older height should have been previously discarded,
 * and messages for a future round should have been buffered).
 *
 * <p>If enough RoundChange messages all targeting a given round are received (and this node is the
 * proposer for said round) - a newRound message is sent, and a new round should be started by the
 * controlling class.
 */
public class RoundChangeManager {

  public static class RoundChangeStatus {
    private final int quorumSize;

    @VisibleForTesting
    final Map<Address, SignedData<RoundChangePayload>> receivedMessages = Maps.newHashMap();

    private boolean actioned = false;

    public RoundChangeStatus(final int quorumSize) {
      this.quorumSize = quorumSize;
    }

    public void addMessage(final SignedData<RoundChangePayload> msg) {
      if (!actioned) {
        receivedMessages.put(msg.getSender(), msg);
      }
    }

    public boolean roundChangeReady() {
      return receivedMessages.size() >= quorumSize && !actioned;
    }

    public RoundChangeCertificate createRoundChangeCertificate() {
      if (roundChangeReady()) {
        actioned = true;
        return new RoundChangeCertificate(receivedMessages.values());
      } else {
        throw new IllegalStateException("Unable to create RoundChangeCertificate at this time.");
      }
    }
  }

  private static final Logger LOG = LogManager.getLogger();
  @VisibleForTesting
  final Map<ConsensusRoundIdentifier, RoundChangeStatus> roundChangeCache =
      Maps.newHashMap();
  private final Collection<Address> validators;
  private final int quorumSize;
  private final long sequenceNumber;
  private final MessageValidatorFactory messageValidityFactory;

  public RoundChangeManager(
      final long sequenceNumber,
      final Collection<Address> validators,
      final MessageValidatorFactory messageValidityFactory) {
    this.validators = validators;
    this.messageValidityFactory = messageValidityFactory;
    this.sequenceNumber = sequenceNumber;
    this.quorumSize = IbftHelpers.calculateRequiredValidatorQuorum(validators.size());
  }

  public Optional<RoundChangeCertificate> appendRoundChangeMessage(
      final SignedData<RoundChangePayload> msg) {
    // validate the received msg
    final ConsensusRoundIdentifier msgTargetRound = msg.getPayload().getRoundChangeIdentifier();
    final RoundChangeMessageValidator messageValidator =
        new RoundChangeMessageValidator(
            messageValidityFactory, validators, quorumSize, sequenceNumber);
    if (!messageValidator.validateMessage(msg)) {
      LOG.info("RoundChange message was invalid.");
      return Optional.empty();
    }
    if (!roundChangeCache.containsKey(msgTargetRound)) {
      roundChangeCache.put(msgTargetRound, new RoundChangeStatus(quorumSize));
    }
    final RoundChangeStatus roundChangeStatus = roundChangeCache.get(msgTargetRound);
    roundChangeStatus.addMessage(msg);
    if (roundChangeStatus.roundChangeReady()) {
      return Optional.of(roundChangeStatus.createRoundChangeCertificate());
    }
    return Optional.empty();
  }

  /* Called when a given round has completed/timed-out such that msgs cached can be removed */
  public void discardCompletedRound(final ConsensusRoundIdentifier completedRoundIdentifier) {
    List<ConsensusRoundIdentifier> cacheKeysToRemove = Lists.newArrayList();
    for (final ConsensusRoundIdentifier msgRoundId : roundChangeCache.keySet()) {
      if (isAnEarlierOrEqualRound(msgRoundId, completedRoundIdentifier)) {
        cacheKeysToRemove.add(msgRoundId);
      }
    }
    for (final ConsensusRoundIdentifier msgRoundId : cacheKeysToRemove) {
      roundChangeCache.remove(msgRoundId);
    }
  }

  private boolean isAnEarlierOrEqualRound(
      final ConsensusRoundIdentifier left, final ConsensusRoundIdentifier right) {
    return left.getRoundNumber() <= right.getRoundNumber();
  }
}
