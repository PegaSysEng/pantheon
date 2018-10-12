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
public class BlockchainReferenceTest_GeneralStateTests_stStaticCall_static_callcodecallcodecall_110_d0g0v0 {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"BlockchainTests/GeneralStateTests/stStaticCall/static_callcodecallcodecall_110_d0g0v0.json", "BlockchainTests/GeneralStateTests/stStaticCall/static_CheckOpcodes4_d0g0v1.json", "BlockchainTests/GeneralStateTests/stStaticCall/static_callToStaticOpCodeCheck_d0g0v0.json", "BlockchainTests/GeneralStateTests/stStaticCall/static_CallIdentity_3_d0g0v0.json", "BlockchainTests/GeneralStateTests/stStaticCall/static_CallEcrecoverV_prefixed0_d0g0v0.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final BlockchainReferenceTestCaseSpec spec;

  public BlockchainReferenceTest_GeneralStateTests_stStaticCall_static_callcodecallcodecall_110_d0g0v0(String name, BlockchainReferenceTestCaseSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
