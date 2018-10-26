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
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibft.blockcreation.IbftBlockCreator;
import tech.pegasys.pantheon.consensus.ibft.blockcreation.IbftBlockCreatorFactory;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.network.IbftNetworkPeers;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.chain.MinedBlockObserver;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.mainnet.BlockHeaderValidator;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.util.Subscribers;

public class IbftRoundFactory {

  private final IbftImmutableState immutableState;

  private final IbftBlockCreatorFactory blockCreatorFactory;
  private final IbftNetworkPeers networkPeers;
  private final MessageFactory messageFactory;
  private final BlockHeaderValidator<IbftContext> ibftContextBlockHeaderValidator;
  private final ProtocolContext<IbftContext> protocolContext;
  private final ProtocolSchedule<IbftContext> protocolSchedule;
  private final KeyPair nodeKeys;
  private final Subscribers<MinedBlockObserver> minedBlockObservers = new Subscribers<>();

  public IbftRoundFactory(
      final IbftImmutableState immutableState,
      final BlockHeaderValidator<IbftContext> ibftContextBlockHeaderValidator,
      final ProtocolContext<IbftContext> protocolContext,
      final ProtocolSchedule<IbftContext> protocolSchedule) {
    this.immutableState = immutableState;
    this.blockCreatorFactory = immutableState.getBlockCreatorFactory();
    this.networkPeers = immutableState.getPeers();
    this.messageFactory = immutableState.getMessageFactory();
    this.ibftContextBlockHeaderValidator = ibftContextBlockHeaderValidator;
    this.protocolContext = protocolContext;
    this.protocolSchedule = protocolSchedule;
    this.nodeKeys = immutableState.getNodeKeys();
  }

  public IbftRound createNewRound(final BlockHeader parentHeader, final int round) {
    long nextBlockHeight = parentHeader.getNumber() + 1;
    final ConsensusRoundIdentifier roundIdentifier =
        new ConsensusRoundIdentifier(nextBlockHeight, round);
    final IbftBlockCreator blockCreator = blockCreatorFactory.create(parentHeader, round);

    final IbftRoundContext roundContext =
        new IbftRoundContext(
            roundIdentifier, protocolContext.getConsensusState().getVoteTally().getValidators());

    return new IbftRound(
        roundContext,
        networkPeers,
        blockCreator,
        messageFactory,
        ibftContextBlockHeaderValidator,
        protocolSchedule,
        protocolContext,
        protocolSchedule.getByBlockNumber(nextBlockHeight).getBlockImporter(),
        nodeKeys,
        minedBlockObservers);
  }
}
