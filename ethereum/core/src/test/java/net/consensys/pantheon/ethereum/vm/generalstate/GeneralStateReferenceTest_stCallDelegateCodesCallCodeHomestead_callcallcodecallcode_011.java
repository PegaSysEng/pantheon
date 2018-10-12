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
public class GeneralStateReferenceTest_stCallDelegateCodesCallCodeHomestead_callcallcodecallcode_011 {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"GeneralStateTests/stCallDelegateCodesCallCodeHomestead/callcallcodecallcode_011.json", "GeneralStateTests/stCallDelegateCodesCallCodeHomestead/callcallcodecallcode_011_OOGMBefore.json", "GeneralStateTests/stCallDelegateCodesCallCodeHomestead/callcallcodecall_010_OOGMAfter.json", "GeneralStateTests/stCallDelegateCodesCallCodeHomestead/callcodecallcodecallcode_111_OOGE.json", "GeneralStateTests/stCallDelegateCodesCallCodeHomestead/callcodecallcall_100.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final GeneralStateTestCaseEipSpec spec;

  public GeneralStateReferenceTest_stCallDelegateCodesCallCodeHomestead_callcallcodecallcode_011(String name, GeneralStateTestCaseEipSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
