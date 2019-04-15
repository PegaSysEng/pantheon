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
package tech.pegasys.pantheon.ethereum.eth.messages;

import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;

import java.util.HashSet;
import java.util.Set;

public final class LimitedTransactionsMessages {

  private static final int LIMIT = 1048576;

  private final TransactionsMessage transactionsMessage;
  private final Set<Transaction> includedTransactions;

  private static TransactionsMessageLimiter limiter = tmp -> tmp.uncheckedEncodedSize() >= LIMIT;

  public LimitedTransactionsMessages(
      final TransactionsMessage transactionsMessage, final Set<Transaction> includedTransactions) {
    this.transactionsMessage = transactionsMessage;
    this.includedTransactions = includedTransactions;
  }

  public static LimitedTransactionsMessages createLimited(
      final Iterable<Transaction> transactions) {
    final Set<Transaction> includedTransactions = new HashSet<>();
    final BytesValueRLPOutput tmp = new BytesValueRLPOutput();
    tmp.startList();
    for (final Transaction transaction : transactions) {
      transaction.writeTo(tmp);
      includedTransactions.add(transaction);
      if (limiter.isLastTransactionToIncludeWithoutExceedingLimit(tmp)) {
        break;
      }
    }
    tmp.endList();
    return new LimitedTransactionsMessages(
        new TransactionsMessage(tmp.encoded()), includedTransactions);
  }

  public final TransactionsMessage getTransactionsMessage() {
    return transactionsMessage;
  }

  public final Set<Transaction> getIncludedTransactions() {
    return includedTransactions;
  }

  public static void setLimiter(TransactionsMessageLimiter limiter) {
    LimitedTransactionsMessages.limiter = limiter;
  }
}
