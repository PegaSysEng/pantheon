package tech.pegasys.pantheon.config;

import io.vertx.core.json.JsonObject;
import java.util.OptionalLong;

public class ConfigUtil {
  public static OptionalLong getOptionalLong(final JsonObject jsonObject, final String key) {
    return jsonObject.containsKey(key)
        ? OptionalLong.of(jsonObject.getLong(key))
        : OptionalLong.empty();
  }

}
