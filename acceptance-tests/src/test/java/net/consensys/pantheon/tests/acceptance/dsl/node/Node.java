package net.consensys.pantheon.tests.acceptance.dsl.node;

import net.consensys.pantheon.tests.acceptance.dsl.transaction.Transaction;

public interface Node {

  <T> T execute(Transaction<T> transaction);
}
