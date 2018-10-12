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
public class BlockchainReferenceTest_GeneralStateTests_stPreCompiledContracts2_CALLCODESha256_3_d0g0v0 {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"BlockchainTests/GeneralStateTests/stPreCompiledContracts2/CALLCODESha256_3_d0g0v0.json", "BlockchainTests/GeneralStateTests/stPreCompiledContracts2/CALLCODERipemd160_4_d0g0v0.json", "BlockchainTests/GeneralStateTests/stPreCompiledContracts2/CALLCODEEcrecover0_gas3000_d0g0v0.json", "BlockchainTests/GeneralStateTests/stPreCompiledContracts2/modexp_0_0_0_35000_d0g2v0.json", "BlockchainTests/GeneralStateTests/stPreCompiledContracts2/CallIdentity_4_gas18_d0g0v0.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final BlockchainReferenceTestCaseSpec spec;

  public BlockchainReferenceTest_GeneralStateTests_stPreCompiledContracts2_CALLCODESha256_3_d0g0v0(String name, BlockchainReferenceTestCaseSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
