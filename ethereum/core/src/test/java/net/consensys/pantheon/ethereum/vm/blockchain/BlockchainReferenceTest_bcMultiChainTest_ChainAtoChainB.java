package net.consensys.pantheon.ethereum.vm.blockchain;

import static net.consensys.pantheon.ethereum.vm.BlockchainReferenceTestTools.executeTest;
import static net.consensys.pantheon.ethereum.vm.BlockchainReferenceTestTools.generateTestParametersForConfig;

import net.consensys.pantheon.ethereum.vm.BlockchainReferenceTestCaseSpec;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** The blockchain test operation testing framework entry point. */
@RunWith(Parameterized.class)
public class BlockchainReferenceTest_bcMultiChainTest_ChainAtoChainB {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"BlockchainTests/bcMultiChainTest/ChainAtoChainB.json", "BlockchainTests/bcMultiChainTest/ChainAtoChainB_blockorder2.json", "BlockchainTests/bcMultiChainTest/ChainAtoChainBCallContractFormA.json", "BlockchainTests/bcTotalDifficultyTest/newChainFrom4Block.json", "BlockchainTests/bcTotalDifficultyTest/sideChainWithMoreTransactions.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final BlockchainReferenceTestCaseSpec spec;

  public BlockchainReferenceTest_bcMultiChainTest_ChainAtoChainB(String name, BlockchainReferenceTestCaseSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
