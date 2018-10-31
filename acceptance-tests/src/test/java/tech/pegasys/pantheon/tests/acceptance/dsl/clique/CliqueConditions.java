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
package tech.pegasys.pantheon.tests.acceptance.dsl.clique;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.ExpectNonceVote;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.ExpectNonceVote.CLIQUE_NONCE_VOTE;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.ExpectProposals;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.ExpectValidators;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.ExpectValidatorsAtBlock;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.ExpectValidatorsAtBlockHash;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eth.EthTransactions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

public class CliqueConditions {
  private final EthTransactions eth;

  public CliqueConditions(final EthTransactions eth) {
    this.eth = eth;
  }

  public Condition validatorsEqual(final PantheonNode... validators) {
    return new ExpectValidators(validatorAddresses(validators));
  }

  public Condition validatorsAtBlockEqual(
      final String blockNumber, final PantheonNode... validators) {
    return new ExpectValidatorsAtBlock(blockNumber, validatorAddresses(validators));
  }

  public Condition validatorsAtBlockHashEqual(
      final String blockHash, final PantheonNode... validators) {
    return new ExpectValidatorsAtBlockHash(
        Hash.fromHexString(blockHash), validatorAddresses(validators));
  }

  public ProposalsConfig proposalsEqual() {
    return new ProposalsConfig();
  }

  public Condition noProposals() {
    return new ExpectProposals(ImmutableMap.of());
  }

  public Condition nonceVoteEquals(final CLIQUE_NONCE_VOTE clique_nonce_vote) {
    return new ExpectNonceVote(eth, clique_nonce_vote);
  }

  private Address[] validatorAddresses(final PantheonNode[] validators) {
    return Arrays.stream(validators).map(PantheonNode::getAddress).sorted().toArray(Address[]::new);
  }

  public static class ProposalsConfig {
    private final Map<PantheonNode, Boolean> proposals = new HashMap<>();

    public ProposalsConfig addProposal(final PantheonNode node) {
      proposals.put(node, true);
      return this;
    }

    public ProposalsConfig removeProposal(final PantheonNode node) {
      proposals.put(node, false);
      return this;
    }

    public Condition build() {
      final Map<Address, Boolean> proposalsAsAddress =
          this.proposals
              .entrySet()
              .stream()
              .collect(Collectors.toMap(p -> p.getKey().getAddress(), Entry::getValue));
      return new ExpectProposals(proposalsAsAddress);
    }
  }
}
