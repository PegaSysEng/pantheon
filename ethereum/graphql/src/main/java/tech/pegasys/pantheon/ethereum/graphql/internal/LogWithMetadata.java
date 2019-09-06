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
package tech.pegasys.pantheon.ethereum.graphql.internal;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.LogTopic;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.List;

import com.google.common.base.MoreObjects;

public class LogWithMetadata implements tech.pegasys.pantheon.plugin.data.LogWithMetadata {

  private final int logIndex;
  private final long blockNumber;
  private final Hash blockHash;
  private final Hash transactionHash;
  private final int transactionIndex;
  private final Address address;
  private final BytesValue data;
  private final List<LogTopic> topics;
  private final boolean removed;

  LogWithMetadata(
      final int logIndex,
      final long blockNumber,
      final Hash blockHash,
      final Hash transactionHash,
      final int transactionIndex,
      final Address address,
      final BytesValue data,
      final List<LogTopic> topics,
      final boolean removed) {
    this.logIndex = logIndex;
    this.blockNumber = blockNumber;
    this.blockHash = blockHash;
    this.transactionHash = transactionHash;
    this.transactionIndex = transactionIndex;
    this.address = address;
    this.data = data;
    this.topics = topics;
    this.removed = removed;
  }

  @Override
  public int getLogIndex() {
    return logIndex;
  }

  @Override
  public long getBlockNumber() {
    return blockNumber;
  }

  @Override
  public Hash getBlockHash() {
    return blockHash;
  }

  @Override
  public Hash getTransactionHash() {
    return transactionHash;
  }

  @Override
  public int getTransactionIndex() {
    return transactionIndex;
  }

  @Override
  public Address getLogger() {
    return address;
  }

  @Override
  public BytesValue getData() {
    return data;
  }

  @Override
  public List<LogTopic> getTopics() {
    return topics;
  }

  @Override
  public boolean isRemoved() {
    return removed;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("logIndex", logIndex)
        .add("blockNumber", blockNumber)
        .add("blockHash", blockHash)
        .add("transactionHash", transactionHash)
        .add("transactionIndex", transactionIndex)
        .add("address", address)
        .add("data", data)
        .add("topics", topics)
        .add("removed", removed)
        .toString();
  }
}
