package tech.pegasys.pantheon.tests.acceptance.dsl.node;

import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;

public interface Node {

  <T> T execute(Transaction<T> transaction);
}
