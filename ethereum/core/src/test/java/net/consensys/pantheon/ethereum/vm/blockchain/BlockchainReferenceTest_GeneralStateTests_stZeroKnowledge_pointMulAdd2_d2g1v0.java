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
public class BlockchainReferenceTest_GeneralStateTests_stZeroKnowledge_pointMulAdd2_d2g1v0 {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"BlockchainTests/GeneralStateTests/stZeroKnowledge/pointMulAdd2_d2g1v0.json", "BlockchainTests/GeneralStateTests/stZeroKnowledge/ecmul_7827-6598_9_28000_128_d0g2v0.json", "BlockchainTests/GeneralStateTests/stZeroKnowledge/ecmul_1-3_5616_28000_96_d0g0v0.json", "BlockchainTests/GeneralStateTests/stZeroKnowledge/ecmul_1-3_2_21000_96_d0g3v0.json", "BlockchainTests/GeneralStateTests/stZeroKnowledge/pointAdd_d3g0v0.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final BlockchainReferenceTestCaseSpec spec;

  public BlockchainReferenceTest_GeneralStateTests_stZeroKnowledge_pointMulAdd2_d2g1v0(String name, BlockchainReferenceTestCaseSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
