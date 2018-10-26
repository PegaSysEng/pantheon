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
package tech.pegasys.pantheon.tests.acceptance.dsl.node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;

import java.io.IOException;
import java.math.BigInteger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetWork;
import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;

public class Eth {

  private final Web3j web3j;

  public Eth(final Web3j web3) {
    this.web3j = web3;
  }

  public BigInteger blockNumber() throws IOException {
    final EthBlockNumber result = web3j.ethBlockNumber().send();
    validatePositiveResult(result);
    return result.getBlockNumber();
  }

  public String[] getWork() throws IOException {
    final EthGetWork result = web3j.ethGetWork().send();
    validatePositiveResult(result);
    return new String[]{
        result.getCurrentBlockHeaderPowHash(),
        result.getSeedHashForDag(),
        result.getBoundaryCondition()
    };
  }

  public BigInteger getBalance(final Account account) throws IOException {
    final EthGetBalance result = web3j.ethGetBalance(account.getAddress(), LATEST).send();
    validatePositiveResult(result);
    return result.getBalance();
  }

  private void validatePositiveResult(final Response<?> result) {
    assertThat(result).isNotNull();
    assertThat(result.hasError()).isFalse();
  }
}
