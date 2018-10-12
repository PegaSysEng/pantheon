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
public class GeneralStateReferenceTest_stStaticCall_static_contractCreationMakeCallThatAskMoreGasThenTransactionProvided {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"GeneralStateTests/stStaticCall/static_contractCreationMakeCallThatAskMoreGasThenTransactionProvided.json", "GeneralStateTests/stStaticCall/static_callcallcall_000_SuicideMiddle.json", "GeneralStateTests/stStaticCall/static_CallRipemd160_3_prefixed0.json", "GeneralStateTests/stStaticCall/static_Call50000bytesContract50_3.json", "GeneralStateTests/stStaticCall/static_CallRipemd160_5.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final GeneralStateTestCaseEipSpec spec;

  public GeneralStateReferenceTest_stStaticCall_static_contractCreationMakeCallThatAskMoreGasThenTransactionProvided(String name, GeneralStateTestCaseEipSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
