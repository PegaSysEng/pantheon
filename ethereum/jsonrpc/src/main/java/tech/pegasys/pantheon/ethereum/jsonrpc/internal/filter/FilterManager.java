package tech.pegasys.pantheon.ethereum.jsonrpc.internal.filter;

import static com.google.common.base.Preconditions.checkNotNull;

import tech.pegasys.pantheon.ethereum.chain.BlockAddedEvent;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.core.TransactionPool;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.BlockParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.queries.LogWithMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.annotations.VisibleForTesting;

/** Manages JSON-RPC filter events. */
public class FilterManager {

  private final Map<String, BlockFilter> blockFilters = new ConcurrentHashMap<>();

  private final Map<String, PendingTransactionFilter> pendingTransactionFilters =
      new ConcurrentHashMap<>();

  private final Map<String, LogFilter> logFilters = new ConcurrentHashMap<>();

  private final FilterIdGenerator filterIdGenerator;

  private final BlockchainQueries blockchainQueries;

  public FilterManager(
      final BlockchainQueries blockchainQueries,
      final TransactionPool transactionPool,
      final FilterIdGenerator filterIdGenerator) {
    this.filterIdGenerator = filterIdGenerator;
    checkNotNull(blockchainQueries.getBlockchain());
    blockchainQueries.getBlockchain().observeBlockAdded(this::recordBlockEvent);
    transactionPool.addTransactionListener(this::recordPendingTransactionEvent);
    this.blockchainQueries = blockchainQueries;
  }

  /**
   * Installs a new block filter
   *
   * @return the block filter id
   */
  public String installBlockFilter() {
    final String filterId = filterIdGenerator.nextId();
    blockFilters.put(filterId, new BlockFilter());
    return filterId;
  }

  /**
   * Installs a pending transaction filter
   *
   * @return the transaction filter id
   */
  public String installPendingTransactionFilter() {
    final String filterId = filterIdGenerator.nextId();
    pendingTransactionFilters.put(filterId, new PendingTransactionFilter());
    return filterId;
  }

  /**
   * Installs a new log filter
   *
   * @param fromBlock {@link BlockParameter} Integer block number, or latest/pending/earliest.
   * @param toBlock {@link BlockParameter} Integer block number, or latest/pending/earliest.
   * @param logsQuery {@link LogsQuery} Addresses and/or topics to filter by
   * @return the log filter id
   */
  public String installLogFilter(
      final BlockParameter fromBlock, final BlockParameter toBlock, final LogsQuery logsQuery) {
    final String filterId = filterIdGenerator.nextId();
    logFilters.put(filterId, new LogFilter(fromBlock, toBlock, logsQuery));
    return filterId;
  }

  /**
   * Uninstalls the specified filter.
   *
   * @param filterId the id of the filter to remove
   * @return {@code true} if the filter was successfully removed; otherwise {@code false}
   */
  public boolean uninstallFilter(final String filterId) {
    return blockFilters.remove(filterId) != null
        || pendingTransactionFilters.remove(filterId) != null
        || logFilters.remove(filterId) != null;
  }

  public void recordBlockEvent(final BlockAddedEvent event, final Blockchain blockchain) {
    final Hash blockHash = event.getBlock().getHash();
    blockFilters.forEach(
        (filterId, filter) -> {
          synchronized (filter) {
            filter.addBlockHash(blockHash);
          }
        });

    checkBlockchainForMatchingLogsForFilters();
  }

  private void checkBlockchainForMatchingLogsForFilters() {
    logFilters.forEach(
        (filterId, filter) -> {
          final long headBlockNumber = blockchainQueries.headBlockNumber();
          final long toBlockNumber =
              filter.getToBlock().getNumber().orElse(blockchainQueries.headBlockNumber());
          final List<LogWithMetadata> logs =
              blockchainQueries.matchingLogs(headBlockNumber, toBlockNumber, filter.getLogsQuery());
          filter.addLog(logs);
        });
  }

  @VisibleForTesting
  void recordPendingTransactionEvent(final Transaction transaction) {
    if (pendingTransactionFilters.isEmpty()) {
      return;
    }

    pendingTransactionFilters.forEach(
        (filterId, filter) -> {
          synchronized (filter) {
            filter.addTransactionHash(transaction.hash());
          }
        });
  }

  /**
   * Gets the new block hashes that have occurred since the filter was last checked.
   *
   * @param filterId the id of the filter to get the new blocks for
   * @return the new block hashes that have occurred since the filter was last checked
   */
  public List<Hash> blockChanges(final String filterId) {
    final BlockFilter filter = blockFilters.get(filterId);
    if (filter == null) {
      return null;
    }

    final List<Hash> hashes;
    synchronized (filter) {
      hashes = new ArrayList<>(filter.blockHashes());
      filter.clearBlockHashes();
    }
    return hashes;
  }

  /**
   * Gets the pending transactions that have occurred since the filter was last checked.
   *
   * @param filterId the id of the filter to get the pending transactions for
   * @return the new pending transaction hashes that have occurred since the filter was last checked
   */
  public List<Hash> pendingTransactionChanges(final String filterId) {
    final PendingTransactionFilter filter = pendingTransactionFilters.get(filterId);
    if (filter == null) {
      return null;
    }

    final List<Hash> hashes;
    synchronized (filter) {
      hashes = new ArrayList<>(filter.transactionHashes());
      filter.clearTransactionHashes();
    }
    return hashes;
  }

  public List<LogWithMetadata> logsChanges(final String filterId) {
    final LogFilter filter = logFilters.get(filterId);
    if (filter == null) {
      return null;
    }

    List<LogWithMetadata> logs;
    synchronized (filter) {
      logs = new ArrayList<>(filter.logs());
      filter.clearLogs();
    }
    return logs;
  }

  public List<LogWithMetadata> logs(final String filterId) {
    final LogFilter filter = logFilters.get(filterId);
    if (filter == null) {
      return null;
    }

    final long fromBlockNumber =
        filter.getFromBlock().getNumber().orElse(blockchainQueries.headBlockNumber());
    final long toBlockNumber =
        filter.getToBlock().getNumber().orElse(blockchainQueries.headBlockNumber());

    return blockchainQueries.matchingLogs(fromBlockNumber, toBlockNumber, filter.getLogsQuery());
  }

  @VisibleForTesting
  int blockFilterCount() {
    return blockFilters.size();
  }

  @VisibleForTesting
  int pendingTransactionFilterCount() {
    return pendingTransactionFilters.size();
  }

  @VisibleForTesting
  int logFilterCount() {
    return logFilters.size();
  }
}
