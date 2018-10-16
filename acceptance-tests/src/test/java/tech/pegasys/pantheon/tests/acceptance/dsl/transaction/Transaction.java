package tech.pegasys.pantheon.tests.acceptance.dsl.transaction;

import org.web3j.protocol.Web3j;

public interface Transaction<T> {

  T execute(final Web3j node);
}
