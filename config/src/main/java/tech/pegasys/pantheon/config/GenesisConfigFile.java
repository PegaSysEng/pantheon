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
package tech.pegasys.pantheon.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.vertx.core.json.JsonObject;

public class GenesisConfigFile {

  private final JsonObject configRoot;

  private GenesisConfigFile(final JsonObject config) {
    this.configRoot = config;
  }

  public static GenesisConfigFile fromConfig(final String config) {
    return fromConfig(new JsonObject(config));
  }

  public static GenesisConfigFile fromConfig(final JsonObject config) {
    return new GenesisConfigFile(normalizeKeys(config));
  }

  public GenesisConfigOptions getConfigOptions() {
    return new GenesisConfigOptions(configRoot.getJsonObject("config"));
  }

  /* Converts the {@link JsonObject} describing the Genesis Block to a {@link Map}. This method
   * converts all nested {@link JsonObject} to {@link Map} as well. Also, note that all keys are
   * converted to lowercase for easier lookup since the keys in a 'genesis.json' file are assumed
   * case insensitive.
   */
  @SuppressWarnings("unchecked")
  private static JsonObject normalizeKeys(final JsonObject genesis) {
    final Map<String, Object> normalized = new HashMap<>();
    genesis
        .getMap()
        .forEach(
            (key, value) -> {
              final String normalizedKey = key.toLowerCase(Locale.US);
              if (value instanceof JsonObject) {
                normalized.put(normalizedKey, normalizeKeys((JsonObject) value));
              } else if (value instanceof Map) {
                normalized.put(
                    normalizedKey, normalizeKeys(new JsonObject((Map<String, Object>) value)));
              } else {
                normalized.put(normalizedKey, value);
              }
            });
    return new JsonObject(normalized);
  }
}
