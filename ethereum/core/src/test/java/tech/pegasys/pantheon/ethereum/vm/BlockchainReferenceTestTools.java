package tech.pegasys.pantheon.ethereum.vm;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.chain.MutableBlockchain;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockImporter;
import tech.pegasys.pantheon.ethereum.core.MutableWorldState;
import tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSpec;
import tech.pegasys.pantheon.ethereum.rlp.RLPException;
import tech.pegasys.pantheon.testutil.JsonTestParameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;

public class BlockchainReferenceTestTools {
  private static final ReferenceTestProtocolSchedules REFERENCE_TEST_PROTOCOL_SCHEDULES =
      ReferenceTestProtocolSchedules.create();

  private static final List<String> NETWORKS_TO_RUN;

  static {
    final String networks =
        System.getProperty(
            "test.ethereum.blockchain.eips",
            "FrontierToHomesteadAt5,HomesteadToEIP150At5,HomesteadToDaoAt5,EIP158ToByzantiumAt5,"
                + "Frontier,Homestead,EIP150,EIP158,Byzantium,Constantinople");
    NETWORKS_TO_RUN = Arrays.asList(networks.split(","));
  }

  private static final JsonTestParameters<?, ?> params =
      JsonTestParameters.create(BlockchainReferenceTestCaseSpec.class)
          .generator(
              (testName, spec, collector) -> {
                final String eip = spec.getNetwork();
                collector.add(testName + "[" + eip + "]", spec, NETWORKS_TO_RUN.contains(eip));
              });

  static {
    if (NETWORKS_TO_RUN.isEmpty()) {
      params.blacklistAll();
    }

    // TODO: Determine and implement cross-chain validation prevention.
    params.blacklist("ChainAtoChainB_BlockHash_(Frontier|Homestead|EIP150|EIP158|Byzantium)");
    // Known bad test.
    params.blacklist("RevertPrecompiledTouch_d0g0v0_(EIP158|Byzantium)");

    // Consumes a huge amount of memory
    params.blacklist("static_Call1MB1024Calldepth_d1g0v0_Byzantium");

    // Pantheon is incorrectly rejecting Uncle block timestamps in the future
    params.blacklist("futureUncleTimestampDifficultyDrop2");
    params.blacklist("futureUncleTimestampDifficultyDrop");

    // Needs investigation
    params.blacklist("RevertInCreateInInit_d0g0v0_Byzantium");
    params.blacklist("RevertInCreateInInit_d0g0v0_Constantinople");

    // Constantinople failures to investigate
    params.blacklist("badOpcodes_d115g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024PreCalls_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBombPreCall_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024BalanceTooLow_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Callcode1024BalanceTooLow_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Callcode1024OOG_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Callcode1024BalanceTooLow_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024BalanceTooLow_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024PreCalls_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2InitCodes_d4g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2InitCodes_d5g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionSelfdestructedRevert_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionSelfdestructedRevert_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionStorage_d2g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CREATE2_Suicide_d10g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CREATE2_Suicide_d11g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CREATE2_Suicide_d9g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CREATE2_Suicide_d5g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CreateMessageReverted_d0g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2callPrecompiles_d4g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2callPrecompiles_d5g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Create2OnDepth1023_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionSelfdestructedRevert_d2g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("RevertDepthCreate2OOG_d1g1v1_Constantinople\\[Constantinople\\]");
    params.blacklist("create2InitCodes_d8g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2callPrecompiles_d2g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2callPrecompiles_d3g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("RevertDepthCreateAddressCollision_d1g1v1_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionStorage_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionStorage_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("RevertDepthCreateAddressCollision_d1g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist("RevertInCreateInInitCreate2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("call_outsize_then_create2_successful_then_returndatasize_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Create2OOGafterInitCodeReturndata2_d0g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Create2Recursive_d0g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist("returndatasize_following_successful_create_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2InitCodes_d6g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2InitCodes_d7g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2InitCodes_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2SmartInitCode_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2SmartInitCode_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2checkFieldsInInitcode_d4g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CREATE2_Suicide_d7g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2callPrecompiles_d6g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2callPrecompiles_d7g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2checkFieldsInInitcode_d5g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionCode_d2g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Create2Recursive_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2checkFieldsInInitcode_d6g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionBalance_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionSelfdestructedOOG_d2g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionBalance_d2g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionNonce_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionSelfdestructedOOG_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("RevertDepthCreate2OOG_d1g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionBalance_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionCode_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2noCash_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2checkFieldsInInitcode_d2g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionNonce_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("RevertOpcodeInCreateReturns_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2callPrecompiles_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2callPrecompiles_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionNonce_d2g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Create2OnDepth1024_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionCode2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionCode2_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionSelfdestructed2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionSelfdestructed2_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CREATE2_Suicide_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionSelfdestructed_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionSelfdestructed_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CREATE2_Suicide_d3g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CreateMessageRevertedOOGInInit_d0g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist("call_then_create2_successful_then_returndatasize_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionSelfdestructed_d2g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionBalance_d3g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionSelfdestructedOOG_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CREATE2_Suicide_d8g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Create2OOGafterInitCode_d0g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist("returndatacopy_0_0_following_successful_create_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2checkFieldsInInitcode_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024BalanceTooLow_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024PreCalls_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBombPreCall_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Delegatecall1024OOG_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Delegatecall1024_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024OOG_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionCode_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1MB1024Calldepth_d0g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest459BC_Constantinople\\[Constantinople\\]");
    params.blacklist("LoopCallsThenRevert_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("LoopCallsDepthThenRevert2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("LoopCallsDepthThenRevert_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("LoopCallsDepthThenRevert3_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("LoopDelegateCallsDepthThenRevert_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("LoopCallsThenRevert_d0g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024OOG_d0g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist("static_Call1024PreCalls2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("ABAcalls2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBombLog2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBomb0_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBombLog_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBomb2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBomb3_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("ABAcalls3_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBomb1_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBomb0_OOG_atMaxCallDepth_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("ChainAtoChainB_BlockHash_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest140BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest168BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest490BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest590BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest79BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest141BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest165BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest21BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest570BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest99BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest203BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest239BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest272BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest431BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest540BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest128BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest235BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest256BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest35BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest56BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest193BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest224BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest255BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest479BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest551BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest240BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest319BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest403BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest427BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest76BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest432BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest486BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest598BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest631BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest71BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest44BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest595BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest606BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest614BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest622BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest152BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest394BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest400BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest463BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest86BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest218BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest277BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest314BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest434BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest492BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest186BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest317BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest331BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest374BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest529BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest181BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest373BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest453BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest522BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest530BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest113BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest253BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest330BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest557BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest561BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest182BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest344BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest515BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest568BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest93BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest132BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest289BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest40BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest593BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest68BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest109BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest284BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest613BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest65BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest8BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest234BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest34BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest392BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest617BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest61BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest127BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest390BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest591BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest623BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest634BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest213BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest258BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest262BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest70BC_Constantinople\\[Constantinople\\]");
    params.blacklist("randomStatetest91BC_Constantinople\\[Constantinople\\]");
    params.blacklist("blockhashNonConstArg_Constantinople\\[Constantinople\\]");
    params.blacklist("suicideStorageCheckVCreate2_Constantinople\\[Constantinople\\]");
    params.blacklist("blockhashTests_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionwithSelfdestructSameBlock_Constantinople\\[Constantinople\\]");
    params.blacklist("BLOCKHASH_Bounds_Constantinople\\[Constantinople\\]");
    params.blacklist("BadStateRootTxBC_Constantinople\\[Constantinople\\]");
  }

  public static Collection<Object[]> generateTestParametersForConfig(final String[] filePath) {
    return params.generate(filePath);
  }

  public static void executeTest(final BlockchainReferenceTestCaseSpec spec) {
    final MutableWorldState worldState =
        spec.getWorldStateArchive().getMutable(spec.getGenesisBlockHeader().getStateRoot());
    final BlockHeader genesisBlockHeader = spec.getGenesisBlockHeader();
    assertThat(worldState.rootHash()).isEqualTo(genesisBlockHeader.getStateRoot());

    final ProtocolSchedule<Void> schedule =
        REFERENCE_TEST_PROTOCOL_SCHEDULES.getByName(spec.getNetwork());

    final MutableBlockchain blockchain = spec.getBlockchain();
    final ProtocolContext<Void> context = spec.getProtocolContext();

    for (final BlockchainReferenceTestCaseSpec.CandidateBlock candidateBlock :
        spec.getCandidateBlocks()) {
      if (!candidateBlock.isExecutable()) {
        return;
      }

      try {
        final Block block = candidateBlock.getBlock();

        final ProtocolSpec<Void> protocolSpec =
            schedule.getByBlockNumber(block.getHeader().getNumber());
        final BlockImporter<Void> blockImporter = protocolSpec.getBlockImporter();
        final HeaderValidationMode validationMode =
            "NoProof".equalsIgnoreCase(spec.getSealEngine())
                ? HeaderValidationMode.LIGHT
                : HeaderValidationMode.FULL;
        final boolean imported =
            blockImporter.importBlock(context, block, validationMode, validationMode);

        assertThat(imported).isEqualTo(candidateBlock.isValid());
      } catch (final RLPException e) {
        Assert.assertFalse(candidateBlock.isValid());
      }
    }

    assertThat(blockchain.getChainHeadHash()).isEqualTo(spec.getLastBlockHash());
  }
}
