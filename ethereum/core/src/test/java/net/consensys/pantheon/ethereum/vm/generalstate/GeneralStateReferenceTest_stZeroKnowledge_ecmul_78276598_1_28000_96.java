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
public class GeneralStateReferenceTest_stZeroKnowledge_ecmul_78276598_1_28000_96 {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"GeneralStateTests/stZeroKnowledge/ecmul_7827-6598_1_28000_96.json", "GeneralStateTests/stZeroKnowledge/ecpairing_perturb_zeropoint_by_curve_order.json", "GeneralStateTests/stZeroKnowledge/ecmul_1-3_5616_28000_96.json", "GeneralStateTests/stZeroKnowledge/ecmul_1-3_340282366920938463463374607431768211456_21000_96.json", "GeneralStateTests/stZeroKnowledge/ecmul_7827-6598_9_21000_128.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final GeneralStateTestCaseEipSpec spec;

  public GeneralStateReferenceTest_stZeroKnowledge_ecmul_78276598_1_28000_96(String name, GeneralStateTestCaseEipSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
