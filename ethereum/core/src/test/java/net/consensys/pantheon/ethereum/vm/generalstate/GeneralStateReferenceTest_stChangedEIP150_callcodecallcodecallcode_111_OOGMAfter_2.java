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
public class GeneralStateReferenceTest_stChangedEIP150_callcodecallcodecallcode_111_OOGMAfter_2 {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"GeneralStateTests/stChangedEIP150/callcodecallcodecallcode_111_OOGMAfter_2.json", "GeneralStateTests/stChangedEIP150/callcallcallcode_001_OOGMAfter_3.json", "GeneralStateTests/stChangedEIP150/callcallcodecallcode_011_OOGMAfter_2.json", "GeneralStateTests/stChangedEIP150/callcall_00_OOGE_2.json", "GeneralStateTests/stChangedEIP150/callcodecallcallcode_101_OOGMAfter_2.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final GeneralStateTestCaseEipSpec spec;

  public GeneralStateReferenceTest_stChangedEIP150_callcodecallcodecallcode_111_OOGMAfter_2(String name, GeneralStateTestCaseEipSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
