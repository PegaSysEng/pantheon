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
package tech.pegasys.pantheon.services;

import tech.pegasys.pantheon.ethereum.eth.sync.BlockBroadcaster;
import tech.pegasys.pantheon.ethereum.eth.transactions.TransactionPool;
import tech.pegasys.pantheon.plugin.services.PantheonEvents;

public class PantheonEventsImpl implements PantheonEvents {
  private final BlockBroadcaster blockBroadcaster;
  private final TransactionPool transactionPool;

  public PantheonEventsImpl(
      final BlockBroadcaster blockBroadcaster, final TransactionPool transactionPool) {
    this.blockBroadcaster = blockBroadcaster;
    this.transactionPool = transactionPool;
  }

  @Override
  public Object addBlockPropagatedListener(final BlockPropagatedListener listener) {
    return blockBroadcaster.subscribePropagateNewBlocks(
        block -> listener.newBlockPropagated(block.getHeader()));
  }

  @Override
  public void removeBlockPropagatedListener(final Object listenerIdentifier) {
    if (listenerIdentifier instanceof Long) {
      blockBroadcaster.unsubscribePropagateNewBlocks((Long) listenerIdentifier);
    }
  }

  @Override
  public Object addTransactionAddedListener(final TransactionAddedListener listener) {
    return transactionPool.subscribePendingTransactions(listener::transactionAdded);
  }

  @Override
  public void removeTransactionAddedListener(final Object listenerIdentifier) {
    if (listenerIdentifier instanceof Long) {
      transactionPool.unsubscribePendingTransactions((Long) listenerIdentifier);
    }
  }

  @Override
  public Object addTransactionDroppedListener(
      final TransactionDroppedListener transactionDroppedListener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeTransactionDroppedListener(final Object listenerIdentifier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object addLogsListener(final LogsListener logsListener) {
    return null;
  }

  @Override
  public void removeLogsListener(final Object listenerIdentifier) {}

  @Override
  public Object addSynchronizerStatusListener(
      final SynchronizerStatusListener synchronizerStatusListener) {
    return null;
  }

  @Override
  public void removeSynchronizerStatusListener(final Object listenerIdentifier) {}
}
