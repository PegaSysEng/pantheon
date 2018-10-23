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

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.ExpectProposals;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.ExpectValidators;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.ExpectValidatorsAtBlock;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.ExpectValidatorsAtBlockHash;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import java.util.Map;

public class CliqueConditions {

  public Condition validatorsEquals(final PantheonNode... validators) {
    return new ExpectValidators(validators);
  }

  public Condition validatorsAtBlockEquals(
      final String blockNumber, final PantheonNode... validators) {
    return new ExpectValidatorsAtBlock(blockNumber, validators);
  }

  public Condition validatorsAtBlockHashEquals(
      final String blockHash, final PantheonNode... validators) {
    return new ExpectValidatorsAtBlockHash(Hash.fromHexString(blockHash), validators);
  }

  public Condition proposalsEqual(final Map<PantheonNode, Boolean> proposals) {
    return new ExpectProposals(proposals);
  }
}
