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
public class GeneralStateReferenceTest_stCallCodes_callcodecallcodecallcode_111_SuicideMiddle {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"GeneralStateTests/stCallCodes/callcodecallcodecallcode_111_SuicideMiddle.json", "GeneralStateTests/stCallCodes/callcodecallcall_100_OOGMBefore.json", "GeneralStateTests/stCallCodes/callcallcodecall_010_SuicideMiddle.json", "GeneralStateTests/stCallCodes/callcodecallcode_11_OOGE.json", "GeneralStateTests/stCallCodes/callcodecallcodecallcode_111_SuicideEnd.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final GeneralStateTestCaseEipSpec spec;

  public GeneralStateReferenceTest_stCallCodes_callcodecallcodecallcode_111_SuicideMiddle(String name, GeneralStateTestCaseEipSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
