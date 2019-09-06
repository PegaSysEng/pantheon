/*
 * Copyright 2019 ConsenSys AG.
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
package tech.pegasys.pantheon.plugin.data;

import tech.pegasys.pantheon.plugin.Unstable;

/** A Log entry from a transaction execution. */
@Unstable
public interface LogWithMetadata extends Log {

  /**
   * The index of this log within the entire ordered list of logs associated with the block this log
   * belongs to.
   *
   * @return The block's number.
   */
  int getLogIndex();

  /**
   * The number of the block to which the log belongs.
   *
   * @return The block's number.
   */
  long getBlockNumber();

  /**
   * The hash of the block to which this block belongs.
   *
   * @return The block's hash.
   */
  Hash getBlockHash();

  /**
   * The hash of the transaction that issued this log.
   *
   * @return The transaction's hash.
   */
  Hash getTransactionHash();

  /**
   * The index in the block of the transaction that issued this log.
   *
   * @return An int representing the index.
   */
  int getTransactionIndex();

  /**
   * Whether the log was removed.
   *
   * @return true when the log was removed, due to a chain reorganization. false if its a valid log.
   */
  boolean isRemoved();
}
