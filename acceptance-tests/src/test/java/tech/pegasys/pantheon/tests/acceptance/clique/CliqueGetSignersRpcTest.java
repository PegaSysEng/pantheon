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

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import java.io.IOException;

import org.junit.Test;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock.Block;

public class CliqueGetSignersRpcTest extends AcceptanceTestBase {

  @Test
  public void shouldBeAbleToGetValidatorsForBlockNumber() throws IOException {
    final PantheonNode minerNode1 = cluster.create(pantheonCliqueMinerNode("miner1"));
    final PantheonNode minerNode2 = cluster.create(pantheonCliqueMinerNode("miner2"));
    final PantheonNode minerNode3 = cluster.create(pantheonCliqueMinerNode("miner3"));
    final PantheonNode[] allNodes = {minerNode1, minerNode2, minerNode3};
    cluster.start();

    final Block latest =
        minerNode1.web3().ethGetBlockByNumber(DefaultBlockParameter.valueOf("latest"), false);
    cluster.verify(clique.validatorsAtBlockEqual("0x0", allNodes));
    cluster.verify(clique.validatorsAtBlockEqual("latest", allNodes));
    cluster.verify(clique.validatorsAtBlockEqual(latest.getNumber().toString(), allNodes));
  }

  @Test
  public void shouldBeAbleToGetValidatorsForBlockHash() throws IOException {
    final PantheonNode minerNode1 = cluster.create(pantheonCliqueMinerNode("miner1"));
    final PantheonNode minerNode2 = cluster.create(pantheonCliqueMinerNode("miner2"));
    final PantheonNode[] allNodes = {minerNode1, minerNode2};
    cluster.start();

    final DefaultBlockParameter latest = DefaultBlockParameter.valueOf("latest");
    final Block latestBlock = minerNode1.web3().ethGetBlockByNumber(latest, false);
    final String hash = latestBlock.getHash();
    cluster.verify(clique.validatorsAtBlockHashEqual(hash, allNodes));
  }
}
