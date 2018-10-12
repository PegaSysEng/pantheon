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
public class GeneralStateReferenceTest_stCreateTest_CREATE_AcreateB_BSuicide_BStore {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"GeneralStateTests/stCreateTest/CREATE_AcreateB_BSuicide_BStore.json", "GeneralStateTests/stCreateTest/CREATE_EContractCreateNEContractInInit_Tr.json", "GeneralStateTests/stCreateTest/CREATE_EmptyContractAndCallIt_0wei.json", "GeneralStateTests/stCreateTest/CREATE_EmptyContract.json", "GeneralStateTests/stCreateTest/CREATE_EmptyContractAndCallIt_1wei.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final GeneralStateTestCaseEipSpec spec;

  public GeneralStateReferenceTest_stCreateTest_CREATE_AcreateB_BSuicide_BStore(String name, GeneralStateTestCaseEipSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
