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
public class BlockchainReferenceTest_GeneralStateTests_stPreCompiledContracts_modexp_9_37111_37111_35000_d0g1v0 {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"BlockchainTests/GeneralStateTests/stPreCompiledContracts/modexp_9_37111_37111_35000_d0g1v0.json", "BlockchainTests/GeneralStateTests/stPreCompiledContracts/modexp_39936_1_55201_25000_d0g3v0.json", "BlockchainTests/GeneralStateTests/stPreCompiledContracts/modexp_0_1_0_20500_d0g2v0.json", "BlockchainTests/GeneralStateTests/stPreCompiledContracts/modexp_49_2401_2401_22000_d0g1v0.json", "BlockchainTests/GeneralStateTests/stPreCompiledContracts/modexp_1_0_0_155000_d0g0v0.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final BlockchainReferenceTestCaseSpec spec;

  public BlockchainReferenceTest_GeneralStateTests_stPreCompiledContracts_modexp_9_37111_37111_35000_d0g1v0(String name, BlockchainReferenceTestCaseSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
