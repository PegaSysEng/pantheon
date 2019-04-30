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
package tech.pegasys.pantheon.ethereum.graphql.internal.request;

import java.math.BigInteger;
import java.util.Map;

public class FilterCriteria extends BlockFilterCriteria {

  private final BigInteger fromBlock;
  private final BigInteger toBlock;

  public FilterCriteria(final Map<String, Object> filterMap) {
    super(filterMap);

    this.fromBlock =
        filterMap.get("fromBlock") != null ? (BigInteger) filterMap.get("fromBlock") : null;

    this.toBlock = filterMap.get("toBlock") != null ? (BigInteger) filterMap.get("toBlock") : null;
  }

  public BigInteger getFromBlock() {
    return fromBlock;
  }

  public BigInteger getToBlock() {
    return toBlock;
  }
}
