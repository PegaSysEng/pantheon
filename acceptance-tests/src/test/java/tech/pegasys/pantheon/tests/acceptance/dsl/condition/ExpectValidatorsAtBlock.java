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
package tech.pegasys.pantheon.tests.acceptance.dsl.condition;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.pantheon.tests.acceptance.dsl.WaitUtils.waitFor;

import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import java.util.Arrays;
import java.util.Optional;

public class ExpectValidatorsAtBlock implements Condition {
  private final String blockParameter;
  private PantheonNode[] validators;

  public ExpectValidatorsAtBlock(final String blockNumber, final PantheonNode... validators) {
    this.blockParameter = blockNumber;
    this.validators = validators;
  }

  @Override
  public void verify(final Node node) {
    waitFor(
        () ->
            assertThat(node.clique().getSigners(Optional.of(blockParameter)).getResult())
                .containsExactlyInAnyOrderElementsOf(
                    Arrays.stream(validators).map(PantheonNode::getAddress).collect(toList())));
  }
}
