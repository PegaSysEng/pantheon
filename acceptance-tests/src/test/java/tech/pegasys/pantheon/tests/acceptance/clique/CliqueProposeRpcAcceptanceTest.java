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

import static tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNodeConfig.pantheonCliqueMinerNode;
import static tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNodeConfig.pantheonCliqueMinerNodeWithValidators;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.clique.ExpectNonceVote.CLIQUE_NONCE_VOTE;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import java.io.IOException;

import org.junit.Test;

public class CliqueProposeRpcAcceptanceTest extends AcceptanceTestBase {

  @Test
  public void shouldAddValidators() throws IOException {
    final String[] initialValidators = {"miner1", "miner2"};
    final PantheonNode minerNode1 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner1", initialValidators));
    final PantheonNode minerNode2 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner2", initialValidators));
    final PantheonNode minerNode3 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner3", initialValidators));
    cluster.start();

    cluster.verify(clique.validatorsEqual(minerNode1, minerNode2));
    minerNode1.execute(cliqueTransactions.proposeAdd(minerNode3));
    minerNode2.execute(cliqueTransactions.proposeAdd(minerNode3));
    cluster.verify(clique.validatorsEqual(minerNode1, minerNode2, minerNode3));
  }

  @Test
  public void shouldRemoveValidators() throws IOException {
    final String[] initialValidators = {"miner1", "miner2", "miner3"};
    final PantheonNode minerNode1 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner1", initialValidators));
    final PantheonNode minerNode2 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner2", initialValidators));
    final PantheonNode minerNode3 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner3", initialValidators));
    cluster.start();

    cluster.verify(clique.validatorsEqual(minerNode1, minerNode2, minerNode3));
    minerNode1.execute(cliqueTransactions.proposeRemove(minerNode3));
    minerNode2.execute(cliqueTransactions.proposeRemove(minerNode3));
    cluster.verify(clique.validatorsEqual(minerNode1, minerNode2));
  }

  @Test
  public void shouldNotAddValidatorWhenInsufficientVotes() throws IOException {
    final String[] initialValidators = {"miner1", "miner2"};
    final PantheonNode minerNode1 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner1", initialValidators));
    final PantheonNode minerNode2 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner2", initialValidators));
    final PantheonNode minerNode3 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner3", initialValidators));
    cluster.start();

    cluster.verify(clique.validatorsEqual(minerNode1, minerNode2));
    minerNode1.execute(cliqueTransactions.proposeAdd(minerNode3));
    cluster.verify(clique.validatorsEqual(minerNode1, minerNode2));
  }

  @Test
  public void shouldNotRemoveValidatorWhenInsufficientVotes() throws IOException {
    final PantheonNode minerNode1 = cluster.create(pantheonCliqueMinerNode("miner1"));
    final PantheonNode minerNode2 = cluster.create(pantheonCliqueMinerNode("miner2"));
    final PantheonNode minerNode3 = cluster.create(pantheonCliqueMinerNode("miner3"));
    cluster.start();

    cluster.verify(clique.validatorsEqual(minerNode1, minerNode2, minerNode3));
    minerNode1.execute(cliqueTransactions.proposeRemove(minerNode3));
    cluster.verify(clique.validatorsEqual(minerNode1, minerNode2, minerNode3));
  }

  @Test
  public void shouldIncludeVoteInBlockHeader() throws IOException {
    final String[] initialValidators = {"miner1", "miner2"};
    final PantheonNode minerNode1 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner1", initialValidators));
    final PantheonNode minerNode2 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner2", initialValidators));
    final PantheonNode minerNode3 =
        cluster.create(pantheonCliqueMinerNodeWithValidators("miner3", initialValidators));
    cluster.start();

    minerNode1.execute(cliqueTransactions.proposeAdd(minerNode3));
    minerNode1.execute(cliqueTransactions.proposeRemove(minerNode2));

    minerNode1.waitUntil(wait.chainHeadHasProgressed(minerNode1, 1));
    minerNode1.verify(blockchain.beneficiaryEquals(minerNode3));
    minerNode1.verify(clique.nonceVoteEquals(CLIQUE_NONCE_VOTE.AUTH));

    minerNode1.waitUntil(wait.chainHeadHasProgressed(minerNode1, 1));
    minerNode1.verify(blockchain.beneficiaryEquals(minerNode2));
    minerNode1.verify(clique.nonceVoteEquals(CLIQUE_NONCE_VOTE.DROP));
  }
}
