package tech.pegasys.pantheon.tests.acceptance.dsl.node;

import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;

import java.math.BigInteger;

public interface Node {

  <T> T execute(Transaction<T> transaction);

  BigInteger getAccountBalance(Account account);
}
