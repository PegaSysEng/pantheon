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
package tech.pegasys.pantheon.ethereum.jsonrpc.health;

import io.vertx.ext.web.RoutingContext;

public abstract class HealthService {

  public static final String LIVENESS_PATH = "/liveness";
  public static final String READINESS_PATH = "/readiness";

  public void handleRequest(final RoutingContext routingContext) {
    routingContext
        .response()
        .setStatusCode(isHealthy() ? healthyStatusCode() : nonHealthyStatusCode())
        .end();
  }

  public abstract boolean isHealthy();

  public abstract int healthyStatusCode();

  public abstract int nonHealthyStatusCode();
}
