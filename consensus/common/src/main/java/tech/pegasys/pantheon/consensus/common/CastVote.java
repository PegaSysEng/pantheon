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
package tech.pegasys.pantheon.consensus.common;

import tech.pegasys.pantheon.ethereum.core.Address;

public class CastVote {

  private final ValidatorVotePolarity votePolarity;
  private final Address proposer;
  private final Address recipient;

  public CastVote(
      final ValidatorVotePolarity votePolarity, final Address proposer, final Address recipient) {
    this.votePolarity = votePolarity;
    this.proposer = proposer;
    this.recipient = recipient;
  }

  public ValidatorVotePolarity getVotePolarity() {
    return votePolarity;
  }

  public Address getProposer() {
    return proposer;
  }

  public Address getRecipient() {
    return recipient;
  }
}
