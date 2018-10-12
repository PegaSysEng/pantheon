package net.consensys.pantheon.ethereum.vm.blockchain;

import static net.consensys.pantheon.ethereum.vm.BlockchainReferenceTestTools.executeTest;
import static net.consensys.pantheon.ethereum.vm.BlockchainReferenceTestTools.generateTestParametersForConfig;

import net.consensys.pantheon.ethereum.vm.BlockchainReferenceTestCaseSpec;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** The blockchain test operation testing framework entry point. */
@RunWith(Parameterized.class)
public class BlockchainReferenceTest_bcUncleHeaderValidity_uncleBloomNot0_2 {

  private static final String[] TEST_CONFIG_FILE_DIR_PATH = new String[] {"BlockchainTests/bcUncleHeaderValidity/uncleBloomNot0_2.json", "BlockchainTests/bcUncleHeaderValidity/incorrectUncleTimestamp.json", "BlockchainTests/bcUncleHeaderValidity/incorrectUncleNumber1.json", "BlockchainTests/bcUncleHeaderValidity/futureUncleTimestampDifficultyDrop2.json", "BlockchainTests/bcUncleHeaderValidity/futureUncleTimestampDifficultyDrop.json"};

  @Parameters(name = "Name: {0}")
  public static Collection<Object[]> getTestParametersForConfig() {
    return generateTestParametersForConfig(TEST_CONFIG_FILE_DIR_PATH);
  }

  private final String name;
  private final BlockchainReferenceTestCaseSpec spec;

  public BlockchainReferenceTest_bcUncleHeaderValidity_uncleBloomNot0_2(String name, BlockchainReferenceTestCaseSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  @Test
  public void execution() {
    executeTest(spec);
  }
}
