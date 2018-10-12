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
public class BlockchainReferenceTest_GeneralStateTests_stPreCompiledContracts_modexp_d10g3v0 {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"BlockchainTests/GeneralStateTests/stPreCompiledContracts/modexp_d10g3v0.json", "BlockchainTests/GeneralStateTests/stPreCompiledContracts/modexp_37120_37111_1_1000000_d0g3v0.json", "BlockchainTests/GeneralStateTests/stPreCompiledContracts/modexp_9_37111_37111_22000_d0g3v0.json", "BlockchainTests/GeneralStateTests/stPreCompiledContracts/modexp_3_28948_11579_20500_d0g1v0.json", "BlockchainTests/GeneralStateTests/stPreCompiledContracts/modexp_d27g0v0.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final BlockchainReferenceTestCaseSpec spec;

  public BlockchainReferenceTest_GeneralStateTests_stPreCompiledContracts_modexp_d10g3v0(String name, BlockchainReferenceTestCaseSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
