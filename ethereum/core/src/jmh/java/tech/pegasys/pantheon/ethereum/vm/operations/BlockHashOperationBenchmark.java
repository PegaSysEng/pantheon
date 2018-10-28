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
package tech.pegasys.pantheon.ethereum.vm.operations;

import static java.util.Collections.emptyList;

import tech.pegasys.pantheon.ethereum.chain.MutableBlockchain;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockBody;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.ExecutionContextTestFixture;
import tech.pegasys.pantheon.ethereum.core.MessageFrameTestFixture;
import tech.pegasys.pantheon.ethereum.mainnet.ConstantinopleGasCalculator;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetProtocolSchedule;
import tech.pegasys.pantheon.ethereum.vm.MessageFrame;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.uint.UInt256;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class BlockHashOperationBenchmark {

  private BlockHashOperation operation;
  private MessageFrame frame;

  @Param({"1", "125", "255"})
  public long blockNumber;

  @Setup(Level.Trial)
  public void prepare() {
    operation = new BlockHashOperation(new ConstantinopleGasCalculator());
    final ExecutionContextTestFixture executionContext =
        new ExecutionContextTestFixture(MainnetProtocolSchedule.create());
    final MutableBlockchain blockchain = executionContext.getBlockchain();

    for (int i = 1; i < 256; i++) {
      blockchain.appendBlock(
          new Block(
              new BlockHeaderTestFixture()
                  .parentHash(blockchain.getChainHeadHash())
                  .number(i)
                  .difficulty(UInt256.ONE)
                  .buildHeader(),
              new BlockBody(emptyList(), emptyList())),
          emptyList());
    }

    frame =
        new MessageFrameTestFixture()
            .executionContextTestFixture(executionContext)
            .blockHeader(
                new BlockHeaderTestFixture()
                    .parentHash(blockchain.getChainHeadHash())
                    .number(blockchain.getChainHeadBlockNumber() + 1)
                    .difficulty(UInt256.ONE)
                    .buildHeader())
            .build();
  }

  @Benchmark
  public Bytes32 executeOperation() {
    frame.pushStackItem(UInt256.of(blockNumber).getBytes());
    operation.execute(frame);
    return frame.popStackItem();
  }
}
