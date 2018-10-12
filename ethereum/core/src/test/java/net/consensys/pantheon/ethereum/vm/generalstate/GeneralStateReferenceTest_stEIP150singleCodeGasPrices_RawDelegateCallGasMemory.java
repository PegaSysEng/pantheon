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
public class GeneralStateReferenceTest_stEIP150singleCodeGasPrices_RawDelegateCallGasMemory {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"GeneralStateTests/stEIP150singleCodeGasPrices/RawDelegateCallGasMemory.json", "GeneralStateTests/stEIP150singleCodeGasPrices/RawCallCodeGasMemoryAsk.json", "GeneralStateTests/stZeroKnowledge2/ecmul_0-0_340282366920938463463374607431768211456_28000_80.json", "GeneralStateTests/stZeroKnowledge2/ecadd_6-9_19274124-124124_25000_128.json", "GeneralStateTests/stZeroKnowledge2/ecmul_0-0_0_21000_64.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final GeneralStateTestCaseEipSpec spec;

  public GeneralStateReferenceTest_stEIP150singleCodeGasPrices_RawDelegateCallGasMemory(String name, GeneralStateTestCaseEipSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
