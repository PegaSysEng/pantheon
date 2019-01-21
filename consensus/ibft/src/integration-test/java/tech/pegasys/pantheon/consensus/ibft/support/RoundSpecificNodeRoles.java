/*
 * Copyright 2019 ConsenSys AG.
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
package tech.pegasys.pantheon.consensus.ibft.support;

import static java.util.Optional.empty;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.payload.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.payload.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;
import tech.pegasys.pantheon.ethereum.core.Hash;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RoundSpecificNodeRoles {

  private final ValidatorPeer proposer;
  private final Collection<ValidatorPeer> peers;
  private final List<ValidatorPeer> nonProposingPeers;

  public RoundSpecificNodeRoles(
      final ValidatorPeer proposer,
      final Collection<ValidatorPeer> peers,
      final List<ValidatorPeer> nonProposingPeers) {
    this.proposer = proposer;
    this.peers = peers;
    this.nonProposingPeers = nonProposingPeers;
  }

  public ValidatorPeer getProposer() {
    return proposer;
  }

  public Collection<ValidatorPeer> getAllPeers() {
    return Collections.unmodifiableCollection(peers);
  }

  public ValidatorPeer getNonProposingPeer(final int index) {
    return nonProposingPeers.get(index);
  }

  public List<SignedData<RoundChangePayload>> nonProposingPeersRoundChange(
      final ConsensusRoundIdentifier targetRound) {
    return nonProposingPeers
        .stream()
        .map(peer -> peer.injectRoundChange(targetRound, empty()))
        .collect(Collectors.toList());
  }

  public void nonProposingPeersPrepare(final ConsensusRoundIdentifier roundId, final Hash hash) {
    nonProposingPeers.forEach(peer -> peer.injectPrepare(roundId, hash));
  }

  public void nonProposingPeerCommit(final ConsensusRoundIdentifier roundId, final Hash hash) {
    nonProposingPeers.forEach(peer -> peer.injectCommit(roundId, hash));
  }

  public Collection<SignedData<PreparePayload>> nonProposingPeersPreparePayload(
      final ConsensusRoundIdentifier preparedRound, final Hash hash) {
    return nonProposingPeers
        .stream()
        .map(role -> role.getMessageFactory().createSignedPreparePayload(preparedRound, hash))
        .collect(Collectors.toList());
  }
}
