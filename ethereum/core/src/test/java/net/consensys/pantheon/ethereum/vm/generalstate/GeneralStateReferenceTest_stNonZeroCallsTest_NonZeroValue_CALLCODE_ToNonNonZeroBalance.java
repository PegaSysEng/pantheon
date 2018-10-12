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
public class GeneralStateReferenceTest_stNonZeroCallsTest_NonZeroValue_CALLCODE_ToNonNonZeroBalance {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"GeneralStateTests/stNonZeroCallsTest/NonZeroValue_CALLCODE_ToNonNonZeroBalance.json", "GeneralStateTests/stNonZeroCallsTest/NonZeroValue_CALL_ToEmpty.json", "GeneralStateTests/stNonZeroCallsTest/NonZeroValue_DELEGATECALL_ToEmpty.json", "GeneralStateTests/stNonZeroCallsTest/NonZeroValue_CALL_ToOneStorageKey.json", "GeneralStateTests/stNonZeroCallsTest/NonZeroValue_TransactionCALL_ToEmpty.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final GeneralStateTestCaseEipSpec spec;

  public GeneralStateReferenceTest_stNonZeroCallsTest_NonZeroValue_CALLCODE_ToNonNonZeroBalance(String name, GeneralStateTestCaseEipSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
