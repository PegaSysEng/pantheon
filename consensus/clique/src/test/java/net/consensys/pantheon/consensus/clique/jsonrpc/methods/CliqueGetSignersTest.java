package net.consensys.pantheon.consensus.clique.jsonrpc.methods;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import net.consensys.pantheon.consensus.common.EpochManager;
import net.consensys.pantheon.ethereum.core.BlockHeader;
import net.consensys.pantheon.ethereum.core.BlockHeaderTestFixture;
import net.consensys.pantheon.ethereum.core.Hash;
import net.consensys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import net.consensys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import net.consensys.pantheon.ethereum.jsonrpc.internal.queries.BlockWithMetadata;
import net.consensys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import net.consensys.pantheon.ethereum.jsonrpc.internal.queries.TransactionWithMetadata;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import net.consensys.pantheon.util.bytes.BytesValue;

import java.util.List;
import java.util.Optional;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CliqueGetSignersTest {
  private CliqueGetSigners method;
  private BlockHeader blockHeader;
  private List<String> validators;

  @Mock private BlockchainQueries blockchainQueries;
  @Mock private BlockWithMetadata<TransactionWithMetadata, Hash> block;
  @Mock private BlockWithMetadata<TransactionWithMetadata, Hash> epochBlock;
  @Mock private EpochManager epochManager;

  @Before
  public void setup() {
    method = new CliqueGetSigners(blockchainQueries, epochManager, new JsonRpcParameter());

    final byte[] genesisBlockExtraData =
        Hex.decode(
            "52657370656374206d7920617574686f7269746168207e452e436172746d616e42eb768f2244c8811c63729a21a3569731535f067ffc57839b00206d1ad20c69a1981b489f772031b279182d99e65703f0076e4812653aab85fca0f00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
    final BytesValue bufferToInject = BytesValue.wrap(genesisBlockExtraData);
    final BlockHeaderTestFixture blockHeaderTestFixture = new BlockHeaderTestFixture();
    blockHeader = blockHeaderTestFixture.extraData(bufferToInject).buildHeader();

    validators =
        asList(
            "0x42eb768f2244c8811c63729a21a3569731535f06",
            "0x7ffc57839b00206d1ad20c69a1981b489f772031",
            "0xb279182d99e65703f0076e4812653aab85fca0f0");
  }

  @Test
  public void returnsMethodName() {
    assertThat(method.getName()).isEqualTo("clique_getSigners");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void returnsValidatorsWhenNoParam() {
    final JsonRpcRequest request = new JsonRpcRequest("2.0", "clique_getSigners", new Object[] {});

    when(blockchainQueries.headBlockNumber()).thenReturn(3065995L);
    when(epochManager.getLastEpochBlock(3065995L)).thenReturn(300000L);
    when(blockchainQueries.blockByNumber(300000L)).thenReturn(Optional.of(epochBlock));
    when(epochBlock.getHeader()).thenReturn(blockHeader);

    final JsonRpcSuccessResponse response = (JsonRpcSuccessResponse) method.response(request);
    assertEquals(validators, response.getResult());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void returnsValidatorsForBlockNumber() {
    final JsonRpcRequest request =
        new JsonRpcRequest("2.0", "clique_getSigners", new Object[] {"0x2EC88B"});

    when(epochManager.getLastEpochBlock(3065995L)).thenReturn(300000L);
    when(blockchainQueries.blockByNumber(300000L)).thenReturn(Optional.of(epochBlock));
    when(epochBlock.getHeader()).thenReturn(blockHeader);

    final JsonRpcSuccessResponse response = (JsonRpcSuccessResponse) method.response(request);
    assertEquals(validators, response.getResult());
  }

  @Test
  public void failsOnInvalidBlockNumber() {
    final JsonRpcRequest request =
        new JsonRpcRequest("2.0", "clique_getSigners", new Object[] {"0x1234"});

    when(epochManager.getLastEpochBlock(4660)).thenReturn(300000L);
    when(blockchainQueries.blockByNumber(300000L)).thenReturn(Optional.empty());

    final JsonRpcErrorResponse response = (JsonRpcErrorResponse) method.response(request);
    assertThat(response.getError().name()).isEqualTo(JsonRpcError.INTERNAL_ERROR.name());
  }
}
