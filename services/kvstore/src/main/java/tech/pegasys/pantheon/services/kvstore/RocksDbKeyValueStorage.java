/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.services.kvstore;

import static tech.pegasys.pantheon.metrics.MetricCategory.ROCKSDB_STATS;

import tech.pegasys.pantheon.metrics.Counter;
import tech.pegasys.pantheon.metrics.MetricCategory;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.metrics.OperationTimer;
import tech.pegasys.pantheon.metrics.prometheus.PrometheusMetricsSystem;
import tech.pegasys.pantheon.services.util.RocksDbUtil;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import io.prometheus.client.Collector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rocksdb.HistogramData;
import org.rocksdb.HistogramType;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import org.rocksdb.Statistics;
import org.rocksdb.TickerType;
import org.rocksdb.TransactionDB;
import org.rocksdb.TransactionDBOptions;
import org.rocksdb.WriteOptions;

public class RocksDbKeyValueStorage implements KeyValueStorage, Closeable {

  private static final Logger LOG = LogManager.getLogger();

  static final List<String> LABELS = Collections.singletonList("quantile");
  static final List<String> LABEL_50 = Collections.singletonList("0.5");
  static final List<String> LABEL_95 = Collections.singletonList("0.95");
  static final List<String> LABEL_99 = Collections.singletonList("0.99");

  // Tickers - RocksDB equivilant of counters
  static final TickerType[] TICKERS = {
    TickerType.BLOCK_CACHE_ADD, // COUNT: 245424
    TickerType.BLOCK_CACHE_HIT, // COUNT: 345898
    TickerType.BLOCK_CACHE_ADD_FAILURES, // COUNT: 0
    TickerType.BLOCK_CACHE_INDEX_MISS, // COUNT: 0
    TickerType.BLOCK_CACHE_INDEX_HIT, // COUNT: 0
    TickerType.BLOCK_CACHE_INDEX_ADD, // COUNT: 0
    TickerType.BLOCK_CACHE_INDEX_BYTES_INSERT, // COUNT: 0
    TickerType.BLOCK_CACHE_INDEX_BYTES_EVICT, // COUNT: 0
    TickerType.BLOCK_CACHE_FILTER_MISS, // COUNT: 0
    TickerType.BLOCK_CACHE_FILTER_HIT, // COUNT: 0
    TickerType.BLOCK_CACHE_FILTER_ADD, // COUNT: 0
    TickerType.BLOCK_CACHE_FILTER_BYTES_INSERT, // COUNT: 0
    TickerType.BLOCK_CACHE_FILTER_BYTES_EVICT, // COUNT: 0
    TickerType.BLOCK_CACHE_DATA_MISS, // COUNT: 294091
    TickerType.BLOCK_CACHE_DATA_HIT, // COUNT: 345898
    TickerType.BLOCK_CACHE_DATA_ADD, // COUNT: 245424
    TickerType.BLOCK_CACHE_DATA_BYTES_INSERT, // COUNT: 975218465
    TickerType.BLOCK_CACHE_BYTES_READ, // COUNT: 1466339309
    TickerType.BLOCK_CACHE_BYTES_WRITE, // COUNT: 975218465
    TickerType.BLOOM_FILTER_USEFUL, // COUNT: 0
    TickerType.PERSISTENT_CACHE_HIT, // COUNT: 0
    TickerType.PERSISTENT_CACHE_MISS, // COUNT: 0
    TickerType.SIM_BLOCK_CACHE_HIT, // COUNT: 0
    TickerType.SIM_BLOCK_CACHE_MISS, // COUNT: 0
    TickerType.MEMTABLE_HIT, // COUNT: 4298870
    TickerType.MEMTABLE_MISS, // COUNT: 417300
    TickerType.GET_HIT_L0, // COUNT: 7601
    TickerType.GET_HIT_L1, // COUNT: 16751
    TickerType.GET_HIT_L2_AND_UP, // COUNT: 0
    TickerType.COMPACTION_KEY_DROP_NEWER_ENTRY, // COUNT: 1207
    TickerType.COMPACTION_KEY_DROP_OBSOLETE, // COUNT: 0
    TickerType.COMPACTION_KEY_DROP_RANGE_DEL, // COUNT: 0
    TickerType.COMPACTION_KEY_DROP_USER, // COUNT: 0
    TickerType.COMPACTION_RANGE_DEL_DROP_OBSOLETE, // COUNT: 0
    TickerType.NUMBER_KEYS_WRITTEN, // COUNT: 1350902
    TickerType.NUMBER_KEYS_READ, // COUNT: 4716170
    TickerType.NUMBER_KEYS_UPDATED, // COUNT: 0
    TickerType.BYTES_WRITTEN, // COUNT: 246835941
    TickerType.BYTES_READ, // COUNT: 1898880197
    TickerType.NUMBER_DB_SEEK, // COUNT: 0
    TickerType.NUMBER_DB_NEXT, // COUNT: 0
    TickerType.NUMBER_DB_PREV, // COUNT: 0
    TickerType.NUMBER_DB_SEEK_FOUND, // COUNT: 0
    TickerType.NUMBER_DB_NEXT_FOUND, // COUNT: 0
    TickerType.NUMBER_DB_PREV_FOUND, // COUNT: 0
    TickerType.ITER_BYTES_READ, // COUNT: 0
    TickerType.NO_FILE_CLOSES, // COUNT: 0
    TickerType.NO_FILE_OPENS, // COUNT: 6
    TickerType.NO_FILE_ERRORS, // COUNT: 0
    // TickerType.STALL_L0_SLOWDOWN_MICROS, // COUNT: 0
    // TickerType.STALL_MEMTABLE_COMPACTION_MICROS, // COUNT: 0
    // TickerType.STALL_L0_NUM_FILES_MICROS, // COUNT: 0
    TickerType.STALL_MICROS, // COUNT: 0
    TickerType.DB_MUTEX_WAIT_MICROS, // COUNT: 0
    TickerType.RATE_LIMIT_DELAY_MILLIS, // COUNT: 0
    TickerType.NO_ITERATORS, // COUNT: 0
    TickerType.NUMBER_MULTIGET_BYTES_READ, // COUNT: 0
    TickerType.NUMBER_MULTIGET_KEYS_READ, // COUNT: 0
    TickerType.NUMBER_MULTIGET_CALLS, // COUNT: 0
    TickerType.NUMBER_FILTERED_DELETES, // COUNT: 0
    TickerType.NUMBER_MERGE_FAILURES, // COUNT: 0
    TickerType.BLOOM_FILTER_PREFIX_CHECKED, // COUNT: 0
    TickerType.BLOOM_FILTER_PREFIX_USEFUL, // COUNT: 0
    TickerType.NUMBER_OF_RESEEKS_IN_ITERATION, // COUNT: 0
    TickerType.GET_UPDATES_SINCE_CALLS, // COUNT: 0
    TickerType.BLOCK_CACHE_COMPRESSED_MISS, // COUNT: 0
    TickerType.BLOCK_CACHE_COMPRESSED_HIT, // COUNT: 0
    TickerType.BLOCK_CACHE_COMPRESSED_ADD, // COUNT: 0
    TickerType.BLOCK_CACHE_COMPRESSED_ADD_FAILURES, // COUNT: 0
    TickerType.WAL_FILE_SYNCED, // COUNT: 0
    TickerType.WAL_FILE_BYTES, // COUNT: 246835941
    TickerType.WRITE_DONE_BY_SELF, // COUNT: 589346
    TickerType.WRITE_DONE_BY_OTHER, // COUNT: 0
    TickerType.WRITE_TIMEDOUT, // COUNT: 0
    TickerType.WRITE_WITH_WAL, // COUNT: 1178692
    TickerType.COMPACT_READ_BYTES, // COUNT: 112905202
    TickerType.COMPACT_WRITE_BYTES, // COUNT: 111119756
    TickerType.FLUSH_WRITE_BYTES, // COUNT: 116155681
    TickerType.NUMBER_DIRECT_LOAD_TABLE_PROPERTIES, // COUNT: 0
    TickerType.NUMBER_SUPERVERSION_ACQUIRES, // COUNT: 105
    TickerType.NUMBER_SUPERVERSION_RELEASES, // COUNT: 0
    TickerType.NUMBER_SUPERVERSION_CLEANUPS, // COUNT: 0
    TickerType.NUMBER_BLOCK_COMPRESSED, // COUNT: 86007
    TickerType.NUMBER_BLOCK_DECOMPRESSED, // COUNT: 281616
    TickerType.NUMBER_BLOCK_NOT_COMPRESSED, // COUNT: 0
    TickerType.MERGE_OPERATION_TOTAL_TIME, // COUNT: 0
    TickerType.FILTER_OPERATION_TOTAL_TIME, // COUNT: 0
    TickerType.ROW_CACHE_HIT, // COUNT: 0
    TickerType.ROW_CACHE_MISS, // COUNT: 0
    TickerType.READ_AMP_ESTIMATE_USEFUL_BYTES, // COUNT: 0
    TickerType.READ_AMP_TOTAL_READ_BYTES, // COUNT: 0
    TickerType.NUMBER_RATE_LIMITER_DRAINS, // COUNT: 0
    TickerType.NUMBER_ITER_SKIP, // COUNT: 0
    TickerType.NUMBER_MULTIGET_KEYS_FOUND, // COUNT: 0
  };

  // Histograms - treated as prometheus summaries
  static final HistogramType[] HISTOGRAMS = {
    HistogramType.DB_GET, // COUNT : 4716170 SUM : 29514961
    HistogramType.DB_WRITE, // COUNT : 589346 SUM : 13142783
    HistogramType.COMPACTION_TIME, // COUNT : 1 SUM : 2169751
    HistogramType.SUBCOMPACTION_SETUP_TIME, // COUNT : 0 SUM : 0
    HistogramType.TABLE_SYNC_MICROS, // COUNT : 4 SUM : 7315
    HistogramType.COMPACTION_OUTFILE_SYNC_MICROS, // COUNT : 2 SUM : 5980
    HistogramType.WAL_FILE_SYNC_MICROS, // COUNT : 0 SUM : 0
    HistogramType.MANIFEST_FILE_SYNC_MICROS, // COUNT : 7 SUM : 799
    HistogramType.TABLE_OPEN_IO_MICROS, // COUNT : 6 SUM : 11660
    HistogramType.DB_MULTIGET, // COUNT : 0 SUM : 0
    HistogramType.READ_BLOCK_COMPACTION_MICROS, // COUNT : 0 SUM : 0
    HistogramType.READ_BLOCK_GET_MICROS, // COUNT : 294091 SUM : 15407755
    HistogramType.WRITE_RAW_BLOCK_MICROS, // COUNT : 101313 SUM : 296580
    HistogramType.STALL_L0_SLOWDOWN_COUNT, // COUNT : 0 SUM : 0
    HistogramType.STALL_MEMTABLE_COMPACTION_COUNT, // COUNT : 0 SUM : 0
    HistogramType.STALL_L0_NUM_FILES_COUNT, // COUNT : 0 SUM : 0
    HistogramType.HARD_RATE_LIMIT_DELAY_COUNT, // COUNT : 0 SUM : 0
    HistogramType.SOFT_RATE_LIMIT_DELAY_COUNT, // COUNT : 0 SUM : 0
    HistogramType.NUM_FILES_IN_SINGLE_COMPACTION, // COUNT : 1 SUM : 4
    HistogramType.DB_SEEK, // COUNT : 0 SUM : 0
    HistogramType.WRITE_STALL, // COUNT : 0 SUM : 0
    HistogramType.SST_READ_MICROS, // COUNT : 294115 SUM : 12603907
    HistogramType.NUM_SUBCOMPACTIONS_SCHEDULED, // COUNT : 0 SUM : 0
    HistogramType.BYTES_PER_READ, // COUNT : 4716170 SUM : 1898880197
    HistogramType.BYTES_PER_WRITE, // COUNT : 589346 SUM : 246835941
    HistogramType.BYTES_PER_MULTIGET, // COUNT : 0 SUM : 0
    HistogramType.BYTES_COMPRESSED, // COUNT : 86007 SUM : 357635819
    HistogramType.BYTES_DECOMPRESSED, // COUNT : 281616 SUM : 1108621213
    HistogramType.COMPRESSION_TIMES_NANOS, // COUNT : 0 SUM : 0
    HistogramType.DECOMPRESSION_TIMES_NANOS, // COUNT : 0 SUM : 0
    HistogramType.READ_NUM_MERGE_OPERANDS, // COUNT : 0 SUM : 0
  };

  private final Options options;
  private final TransactionDBOptions txOptions;
  private final TransactionDB db;
  private final AtomicBoolean closed = new AtomicBoolean(false);

  private final OperationTimer readLatency;
  private final OperationTimer removeLatency;
  private final OperationTimer writeLatency;
  private final OperationTimer commitLatency;
  private final Counter rollbackCount;
  private final Statistics stats;

  public static KeyValueStorage create(
      final RocksDbConfiguration rocksDbConfiguration, final MetricsSystem metricsSystem)
      throws StorageException {
    return new RocksDbKeyValueStorage(rocksDbConfiguration, metricsSystem);
  }

  private RocksDbKeyValueStorage(
      final RocksDbConfiguration rocksDbConfiguration, final MetricsSystem metricsSystem) {
    RocksDbUtil.loadNativeLibrary();
    try {
      stats = new Statistics();
      options =
          new Options()
              .setCreateIfMissing(true)
              .setMaxOpenFiles(rocksDbConfiguration.getMaxOpenFiles())
              .setTableFormatConfig(rocksDbConfiguration.getBlockBasedTableConfig())
              .setStatistics(stats);

      txOptions = new TransactionDBOptions();
      db = TransactionDB.open(options, txOptions, rocksDbConfiguration.getDatabaseDir().toString());

      readLatency =
          metricsSystem.createTimer(
              MetricCategory.ROCKSDB, "read_latency_seconds", "Latency for read from RocksDB.");
      removeLatency =
          metricsSystem.createTimer(
              MetricCategory.ROCKSDB,
              "remove_latency_seconds",
              "Latency of remove requests from RocksDB.");
      writeLatency =
          metricsSystem.createTimer(
              MetricCategory.ROCKSDB, "write_latency_seconds", "Latency for write to RocksDB.");
      commitLatency =
          metricsSystem.createTimer(
              MetricCategory.ROCKSDB, "commit_latency_seconds", "Latency for commits to RocksDB.");

      for (final TickerType ticker : TICKERS) {
        final String promCounterName = ticker.name().toLowerCase();
        metricsSystem.createLongGauge(
            ROCKSDB_STATS,
            promCounterName,
            "RocksDB reported statistics for " + ticker.name(),
            () -> stats.getTickerCount(ticker));
      }

//      if (metricsSystem instanceof PrometheusMetricsSystem) {
//        for (final HistogramType histogram : HISTOGRAMS) {
//          ((PrometheusMetricsSystem) metricsSystem)
//              .addCollector(ROCKSDB_STATS, histogramToCollector(histogram));
//        }
//      }

      metricsSystem.createLongGauge(
          MetricCategory.ROCKSDB,
          "rocks_db_table_readers_memory_bytes",
          "Estimated memory used for RocksDB index and filter blocks in bytes",
          () -> {
            try {
              return db.getLongProperty("rocksdb.estimate-table-readers-mem");
            } catch (final RocksDBException e) {
              LOG.debug("Failed to get RocksDB metric", e);
              return 0L;
            }
          });

      rollbackCount =
          metricsSystem.createCounter(
              MetricCategory.ROCKSDB,
              "rollback_count",
              "Number of RocksDB transactions rolled back.");
    } catch (final RocksDBException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public Optional<BytesValue> get(final BytesValue key) throws StorageException {
    throwIfClosed();

    try (final OperationTimer.TimingContext ignored = readLatency.startTimer()) {
      return Optional.ofNullable(db.get(key.getArrayUnsafe())).map(BytesValue::wrap);
    } catch (final RocksDBException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public Transaction startTransaction() throws StorageException {
    throwIfClosed();
    final WriteOptions options = new WriteOptions();
    return new RocksDbTransaction(db.beginTransaction(options), options);
  }

  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      txOptions.close();
      options.close();
      db.close();
    }
  }

  private void throwIfClosed() {
    if (closed.get()) {
      LOG.error("Attempting to use a closed RocksDbKeyValueStorage");
      throw new IllegalStateException("Storage has been closed");
    }
  }

  private class RocksDbTransaction extends AbstractTransaction {
    private final org.rocksdb.Transaction innerTx;
    private final WriteOptions options;

    RocksDbTransaction(final org.rocksdb.Transaction innerTx, final WriteOptions options) {
      this.innerTx = innerTx;
      this.options = options;
    }

    @Override
    protected void doPut(final BytesValue key, final BytesValue value) {
      try (final OperationTimer.TimingContext ignored = writeLatency.startTimer()) {
        innerTx.put(key.getArrayUnsafe(), value.getArrayUnsafe());
      } catch (final RocksDBException e) {
        throw new StorageException(e);
      }
    }

    @Override
    protected void doRemove(final BytesValue key) {
      try (final OperationTimer.TimingContext ignored = removeLatency.startTimer()) {
        innerTx.delete(key.getArrayUnsafe());
      } catch (final RocksDBException e) {
        throw new StorageException(e);
      }
    }

    @Override
    protected void doCommit() throws StorageException {
      try (final OperationTimer.TimingContext ignored = commitLatency.startTimer()) {
        innerTx.commit();
      } catch (final RocksDBException e) {
        throw new StorageException(e);
      } finally {
        close();
      }
    }

    @Override
    protected void doRollback() {
      try {
        innerTx.rollback();
        rollbackCount.inc();
      } catch (final RocksDBException e) {
        throw new StorageException(e);
      } finally {
        close();
      }
    }

    private void close() {
      innerTx.close();
      options.close();
    }
  }

  private Collector histogramToCollector(final HistogramType histogram) {
    return new Collector() {
      final String metricName =
          PrometheusMetricsSystem.convertToPrometheusName(
              ROCKSDB_STATS, histogram.name().toLowerCase());

      @Override
      public List<MetricFamilySamples> collect() {
        final HistogramData data = stats.getHistogramData(histogram);
        return Collections.singletonList(
            new MetricFamilySamples(
                metricName,
                Type.SUMMARY,
                "RocksDB histogram for " + metricName,
                Arrays.asList(
                    new MetricFamilySamples.Sample(metricName, LABELS, LABEL_50, data.getMedian()),
                    new MetricFamilySamples.Sample(
                        metricName, LABELS, LABEL_95, data.getPercentile95()),
                    new MetricFamilySamples.Sample(
                        metricName, LABELS, LABEL_99, data.getPercentile99()))));
      }
    };
  }
}
