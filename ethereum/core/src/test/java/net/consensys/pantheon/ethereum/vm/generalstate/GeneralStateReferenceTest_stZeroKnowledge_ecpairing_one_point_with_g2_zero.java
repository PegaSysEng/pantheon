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
public class GeneralStateReferenceTest_stZeroKnowledge_ecpairing_one_point_with_g2_zero {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"GeneralStateTests/stZeroKnowledge/ecpairing_one_point_with_g2_zero.json", "GeneralStateTests/stZeroKnowledge/ecmul_1-3_340282366920938463463374607431768211456_21000_80.json", "GeneralStateTests/stZeroKnowledge/ecpairing_one_point_not_in_subgroup.json", "GeneralStateTests/stZeroKnowledge/ecmul_1-3_340282366920938463463374607431768211456_21000_128.json", "GeneralStateTests/stZeroKnowledge/ecmul_1-2_5617_28000_96.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final GeneralStateTestCaseEipSpec spec;

  public GeneralStateReferenceTest_stZeroKnowledge_ecpairing_one_point_with_g2_zero(String name, GeneralStateTestCaseEipSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
