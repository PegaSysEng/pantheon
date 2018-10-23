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
package tech.pegasys.pantheon.tests.acceptance.clique;

import static tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNodeConfig.pantheonCliqueMinerNodeWithValidators;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.clique.CliqueConditions;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import java.io.IOException;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

public class CliqueProposalRpcAcceptanceTest extends AcceptanceTestBase {
  private final CliqueConditions clique = new CliqueConditions();

  @Test
  public void shouldReturnProposals() throws IOException {
    String[] initialValidators = {"miner1", "miner2"};
    final PantheonNode minerNode1 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner1", initialValidators));
    final PantheonNode minerNode2 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner2", initialValidators));
    final PantheonNode minerNode3 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner3", initialValidators));
    cluster.start(minerNode1, minerNode2, minerNode3);

    cluster.verify(clique.proposalsEqual(ImmutableMap.of()));
    minerNode1.clique().propose(minerNode3.getAddress(), true);
    minerNode1.clique().propose(minerNode2.getAddress(), false);
    minerNode2.clique().propose(minerNode3.getAddress(), false);
    minerNode1.verify(clique.proposalsEqual(ImmutableMap.of(minerNode3, true, minerNode2, false)));
    minerNode2.verify(clique.proposalsEqual(ImmutableMap.of(minerNode3, false)));
    minerNode3.verify(clique.proposalsEqual(ImmutableMap.of()));
  }
}
