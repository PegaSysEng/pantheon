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
public class GeneralStateReferenceTest_stEIP150singleCodeGasPrices_RawCallCodeGasValueTransferAsk {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"GeneralStateTests/stEIP150singleCodeGasPrices/RawCallCodeGasValueTransferAsk.json", "GeneralStateTests/stEIP150singleCodeGasPrices/RawCallCodeGasMemory.json", "GeneralStateTests/stEIP150singleCodeGasPrices/RawCallMemoryGas.json", "GeneralStateTests/stEIP150singleCodeGasPrices/RawCallGasValueTransferMemoryAsk.json", "GeneralStateTests/stEIP150singleCodeGasPrices/RawBalanceGas.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final GeneralStateTestCaseEipSpec spec;

  public GeneralStateReferenceTest_stEIP150singleCodeGasPrices_RawCallCodeGasValueTransferAsk(String name, GeneralStateTestCaseEipSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
