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
package tech.pegasys.pantheon.ethereum.vm;

import tech.pegasys.pantheon.config.GenesisConfigOptions;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolScheduleBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import io.vertx.core.json.JsonObject;

public class ReferenceTestProtocolSchedules {

  private static final int CHAIN_ID = 1;

  public static ReferenceTestProtocolSchedules create() {
    final ImmutableMap.Builder<String, ProtocolSchedule<Void>> builder = ImmutableMap.builder();
    builder.put("Frontier", createSchedule(Collections.emptyMap()));
    builder.put("FrontierToHomesteadAt5", createSchedule(ImmutableMap.of("homesteadblock", 5)));
    builder.put("Homestead", createSchedule(ImmutableMap.of("homesteadblock", 0)));
    builder.put(
        "HomesteadToEIP150At5",
        createSchedule(ImmutableMap.of("homesteadblock", 0, "eip150block", 5)));
    builder.put(
        "HomesteadToDaoAt5",
        createSchedule(ImmutableMap.of("homesteadblock", 0, "daoforkblock", 5)));
    builder.put("EIP150", createSchedule(ImmutableMap.of("eip150block", 0)));
    builder.put("EIP158", createSchedule(ImmutableMap.of("eip158block", 0)));
    builder.put(
        "EIP158ToByzantiumAt5",
        createSchedule(ImmutableMap.of("eip158block", 0, "byzantiumblock", 5)));
    builder.put("Byzantium", createSchedule(ImmutableMap.of("byzantiumblock", 0)));
    builder.put("Constantinople", createSchedule(ImmutableMap.of("constantinopleblock", 0)));
    return new ReferenceTestProtocolSchedules(builder.build());
  }

  private final Map<String, ProtocolSchedule<Void>> schedules;

  private ReferenceTestProtocolSchedules(final Map<String, ProtocolSchedule<Void>> schedules) {
    this.schedules = schedules;
  }

  public ProtocolSchedule<Void> getByName(final String name) {
    return schedules.get(name);
  }

  private static ProtocolSchedule<Void> createSchedule(final Map<String, Object> config) {
    return new ProtocolScheduleBuilder<>(
            new GenesisConfigOptions(new JsonObject(config)), Function.identity())
        .createProtocolSchedule(CHAIN_ID);
  }
}
