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
public class GeneralStateReferenceTest_stZeroCallsTest_ZeroValue_TransactionCALLwithData_ToNonZeroBalance {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"GeneralStateTests/stZeroCallsTest/ZeroValue_TransactionCALLwithData_ToNonZeroBalance.json", "GeneralStateTests/stZeroCallsTest/ZeroValue_CALLCODE_ToOneStorageKey.json", "GeneralStateTests/stZeroCallsTest/ZeroValue_TransactionCALL_ToOneStorageKey.json", "GeneralStateTests/stZeroCallsTest/ZeroValue_TransactionCALLwithData_ToEmpty.json", "GeneralStateTests/stZeroCallsTest/ZeroValue_DELEGATECALL_ToOneStorageKey.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final GeneralStateTestCaseEipSpec spec;

  public GeneralStateReferenceTest_stZeroCallsTest_ZeroValue_TransactionCALLwithData_ToNonZeroBalance(String name, GeneralStateTestCaseEipSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
