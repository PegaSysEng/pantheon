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
package tech.pegasys.pantheon.ethereum.graphql.internal.methods;

import tech.pegasys.pantheon.ethereum.graphql.internal.request.CallData;
import tech.pegasys.pantheon.ethereum.graphql.internal.results.Block;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulator;

import java.math.BigInteger;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class BlockEstimateGasFetcher implements DataFetcher<BigInteger>, GraphQLMethod {
  private TransactionSimulator transactionSimulator;

  //    private static final Logger LOG = LogManager.getLogger();

  public BlockEstimateGasFetcher(final TransactionSimulator transactionSimulator) {
    this.transactionSimulator = transactionSimulator;
  }

  @Override
  public GraphQLMethodType getType() {
    return GraphQLMethodType.ESTIMATE_GAS;
  }

  @Override
  public BigInteger get(final DataFetchingEnvironment environment) {
    final long blockNumber = ((Block) environment.getSource()).getNumber().longValue();
    final BigInteger gasLimit = ((Block) environment.getSource()).getGasLimit();
    if (environment.containsArgument("data")) {
      CallData callData = new CallData(environment.getArgument("data"));
      return runCall(overrideGasLimitAndPrice(callData, gasLimit), blockNumber);
    }
    return null;
  }

  protected BigInteger runCall(final CallData callData, final long blockNumber) {
    return transactionSimulator
        .process(callData, blockNumber)
        .map(
            result ->
                result
                    .getValidationResult()
                    .either(
                        (() -> BigInteger.valueOf(result.getGasEstimate())),
                        reason -> BigInteger.ZERO))
        .orElse(BigInteger.ZERO);
  }

  private CallData overrideGasLimitAndPrice(CallData callData, BigInteger gasLimit) {
    return new CallData(
            callData.getFrom(),
            callData.getTo(),
            gasLimit,
            BigInteger.ZERO,
            callData.getValue(),
            callData.getPayload());
  }
}
