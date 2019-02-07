package tech.pegasys.pantheon.ethereum.storage.keyvalue;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.ethereum.chain.TransactionLocation;
import tech.pegasys.pantheon.ethereum.core.BlockBody;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.TransactionReceipt;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockHashFunction;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.KeyValueStoragePrefixedKeyBlockchainStorage.Updater;
import tech.pegasys.pantheon.services.kvstore.InMemoryKeyValueStorage;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.junit.Test;

public class KeyValueStoragePrefixedKeyBlockchainStorageTest {

  private final KeyValueStoragePrefixedKeyBlockchainStorage storage =
      new KeyValueStoragePrefixedKeyBlockchainStorage(
          new InMemoryKeyValueStorage(), MainnetBlockHashFunction::createHash);
  private static final Hash BLOCK_HASH = Hash.fromHexStringLenient("0x12345");

  @Test
  public void shouldStoreChainHeadHash() {
    assertRoundTripOptional(BLOCK_HASH, Updater::setChainHead, storage::getChainHead);
  }

  @Test
  public void shouldStoreFastSyncHead() {
    assertRoundTripOptional(BLOCK_HASH, Updater::setFastSyncHead, storage::getFastSyncHead);
  }

  @Test
  public void shouldStoreBlockHeader() {
    final BlockHeader header = new BlockHeaderTestFixture().buildHeader();
    final Hash hash = header.getHash();
    assertRoundTripOptional(
        header,
        (updater, value) -> updater.putBlockHeader(hash, value),
        () -> storage.getBlockHeader(hash));
  }

  @Test
  public void shouldStoreBlockBody() {
    final BlockBody body = new BlockBody(emptyList(), emptyList());
    final Hash hash = BLOCK_HASH;
    assertRoundTripOptional(
        body,
        (updater, value) -> updater.putBlockBody(hash, value),
        () -> storage.getBlockBody(hash));
  }

  @Test
  public void shouldStoreTransactionReceipts() {
    final List<TransactionReceipt> receipts =
        singletonList(
            new TransactionReceipt(Hash.fromHexStringLenient("0x56432"), 100, emptyList()));
    assertRoundTripOptional(
        receipts,
        (updater, value) -> updater.putTransactionReceipts(BLOCK_HASH, value),
        () -> storage.getTransactionReceipts(BLOCK_HASH));
  }

  @Test
  public void shouldStoreBlockHash() {
    final long blockNumber = 29823;
    assertRoundTripOptional(
        BLOCK_HASH,
        (updater, value) -> updater.putBlockHash(blockNumber, value),
        () -> storage.getBlockHash(blockNumber));
  }

  @Test
  public void shouldStoreTotalDifficulty() {
    final UInt256 td = UInt256.of(9283923823L);
    assertRoundTripOptional(
        td,
        (updater, value) -> updater.putTotalDifficulty(BLOCK_HASH, td),
        () -> storage.getTotalDifficulty(BLOCK_HASH));
  }

  @Test
  public void shouldStoreTransactionLocation() {
    final TransactionLocation transactionLocation = new TransactionLocation(BLOCK_HASH, 4);
    final Hash transactionHash = Hash.fromHexStringLenient("0x4888222");
    assertRoundTripOptional(
        transactionLocation,
        (updater, value) -> updater.putTransactionLocation(transactionHash, transactionLocation),
        () -> storage.getTransactionLocation(transactionHash));
  }

  @Test
  public void shouldStoreForkHeads() {
    final Collection<Hash> forkHeads =
        asList(
            BLOCK_HASH,
            Hash.fromHexStringLenient("0x348482"),
            Hash.fromHexStringLenient("0x8885644"));
    final Updater updater = storage.updater();
    updater.setForkHeads(forkHeads);
    assertThat(storage.getForkHeads()).isEmpty();
    updater.commit();
    assertThat(storage.getForkHeads()).isEqualTo(forkHeads);
  }

  private <T> void assertRoundTripOptional(
      final T value, final BiConsumer<Updater, T> setter, final Supplier<Optional<T>> getter) {
    final Updater updater = storage.updater();
    setter.accept(updater, value);
    assertThat(getter.get()).isEmpty();
    updater.commit();
    assertThat(getter.get()).contains(value);
  }
}
