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
package tech.pegasys.pantheon.tests.acceptance.dsl.transaction.ibft2;

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

public class IbftTwoTransactions {
  public static final String LATEST = "latest";

  public IbftTwoPropose createRemoveProposal(final PantheonNode node) {
    return propose(node.getAddress().toString(), false);
  }

  public IbftTwoPropose createAddProposal(final PantheonNode node) {
    return propose(node.getAddress().toString(), true);
  }

  private IbftTwoPropose propose(final String address, final boolean auth) {
    return new IbftTwoPropose(address, auth);
  }

  public IbftTwoProposals createProposals() {
    return new IbftTwoProposals();
  }

  public IbftTwoGetValidators createGetValidators(final String blockNumber) {
    return new IbftTwoGetValidators(blockNumber);
  }

  public IbftTwoGetValidatorsAtHash createGetValidatorsAtHash(final Hash blockHash) {
    return new IbftTwoGetValidatorsAtHash(blockHash);
  }

  public IbftTwoDiscard createDiscardProposal(final PantheonNode node) {
    return new IbftTwoDiscard(node.getAddress().toString());
  }
}
