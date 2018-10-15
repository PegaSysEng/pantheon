package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.ethereum.blockcreation.EthHashMiningCoordinator;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.mainnet.EthHashSolverInputs;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.Optional;
import javax.xml.bind.DatatypeConverter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EthGetWorkTest {

  private EthGetWork method;
  private final String JSON_RPC_VERSION = "2.0";
  private final String ETH_METHOD = "eth_getWork";
  private final String hexValue =
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

  @Mock private EthHashMiningCoordinator miningCoordinator;
  @Mock private BlockchainQueries blockChain;

  @Before
  public void setUp() {
    method = new EthGetWork(miningCoordinator, blockChain);
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(ETH_METHOD);
  }

  @Test
  public void shouldReturnCorrectResultOnGenesisDAG() {
    final JsonRpcRequest request = requestWithParams();
    final EthHashSolverInputs values =
        new EthHashSolverInputs(
            UInt256.fromHexString(hexValue), DatatypeConverter.parseHexBinary(hexValue), 0);
    final String[] expectedValue = {
      "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      "0x0000000000000000000000000000000000000000000000000000000000000000",
      "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
    };
    final JsonRpcResponse expectedResponse =
        new JsonRpcSuccessResponse(request.getId(), expectedValue);
    when(miningCoordinator.getWorkDefinition()).thenReturn(Optional.of(values));

    JsonRpcResponse actualResponse = method.response(request);
    assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
  }

  @Test
  public void shouldReturnCorrectResultOnHighBlockSeed() {
    final JsonRpcRequest request = requestWithParams();
    final EthHashSolverInputs values =
        new EthHashSolverInputs(
            UInt256.fromHexString(hexValue), DatatypeConverter.parseHexBinary(hexValue), 30000);
    final String[] expectedValue = {
      "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
    };
    final JsonRpcResponse expectedResponse =
        new JsonRpcSuccessResponse(request.getId(), expectedValue);
    when(miningCoordinator.getWorkDefinition()).thenReturn(Optional.of(values));
    when(blockChain.getBlockHashByNumber(30000))
        .thenReturn(
            Optional.of(
                Hash.fromHexString(
                    "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")));

    JsonRpcResponse actualResponse = method.response(request);
    assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
  }

  @Test
  public void shouldReturnErrorOnNoneMiningNode() {
    final JsonRpcRequest request = requestWithParams();
    final JsonRpcResponse expectedResponse =
        new JsonRpcErrorResponse(request.getId(), JsonRpcError.INTERNAL_ERROR);
    when(miningCoordinator.getWorkDefinition()).thenReturn(Optional.empty());

    JsonRpcResponse actualResponse = method.response(request);
    assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
  }

  private JsonRpcRequest requestWithParams(final Object... params) {
    return new JsonRpcRequest(JSON_RPC_VERSION, ETH_METHOD, params);
  }
}
