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

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.transaction.CallParameter;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.math.BigInteger;
import java.util.Map;

public class CallData extends CallParameter {

  public CallData(final Map<String, Object> callDataMap) {
    super(
        callDataMap.get("from") != null ? (Address) callDataMap.get("from") : null,
        callDataMap.get("to") != null ? (Address) callDataMap.get("to") : null,
        callDataMap.get("gas") != null ? ((BigInteger) callDataMap.get("gas")).longValue() : -1,
        callDataMap.get("gasPrice") != null
            ? Wei.of((BigInteger) callDataMap.get("gasPrice"))
            : null,
        callDataMap.get("value") != null ? Wei.of((BigInteger) callDataMap.get("value")) : null,
        callDataMap.get("data") != null ? (BytesValue) callDataMap.get("data") : null);
  }

  public CallData(
      final Address from,
      final Address to,
      final BigInteger gas,
      final BigInteger gasPrice,
      final Wei value,
      final BytesValue data) {
    super(from, to, gas.longValue(), Wei.of(gasPrice), value, data);
  }
}
