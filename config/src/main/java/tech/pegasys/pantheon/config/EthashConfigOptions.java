package tech.pegasys.pantheon.config;

import io.vertx.core.json.JsonObject;
import java.util.OptionalLong;

public class EthashConfigOptions {

  public static final EthashConfigOptions DEFAULT = new EthashConfigOptions(new JsonObject());;
  private final JsonObject ethashConfigRoot;

  EthashConfigOptions(final JsonObject ethashConfigRoot) {
    this.ethashConfigRoot = ethashConfigRoot;
  }

  public OptionalLong getFixedDifficulty(){
    return ConfigUtil.getOptionalLong(ethashConfigRoot, "fixeddifficulty");
  }

}
