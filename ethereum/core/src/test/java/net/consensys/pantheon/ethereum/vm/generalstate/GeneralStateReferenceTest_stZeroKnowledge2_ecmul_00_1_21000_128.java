package net.consensys.pantheon.ethereum.vm.generalstate;

import static net.consensys.pantheon.ethereum.vm.GeneralStateReferenceTestTools.executeTest;
import static net.consensys.pantheon.ethereum.vm.GeneralStateReferenceTestTools.generateTestParametersForConfig;

import net.consensys.pantheon.ethereum.vm.GeneralStateTestCaseEipSpec;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** The general state test operation testing framework entry point. */
@RunWith(Parameterized.class)
public class GeneralStateReferenceTest_stZeroKnowledge2_ecmul_00_1_21000_128 {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"GeneralStateTests/stZeroKnowledge2/ecmul_0-0_1_21000_128.json", "GeneralStateTests/stZeroKnowledge2/ecmul_0-0_340282366920938463463374607431768211456_28000_96.json", "GeneralStateTests/stZeroKnowledge2/ecmul_0-0_0_28000_40.json", "GeneralStateTests/stZeroKnowledge2/ecadd_0-0_0-0_21000_0.json", "GeneralStateTests/stZeroKnowledge2/ecmul_0-0_5616_21000_96.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final GeneralStateTestCaseEipSpec spec;

  public GeneralStateReferenceTest_stZeroKnowledge2_ecmul_00_1_21000_128(String name, GeneralStateTestCaseEipSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
