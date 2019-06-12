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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;

public abstract class HealthService {

  public static final String LIVENESS_PATH = "/liveness";
  public static final String READINESS_PATH = "/readiness";

  private static final int HEALTHY_STATUS_CODE = HttpResponseStatus.OK.code();
  private static final int NON_HEALTHY_STATUS_CODE = HttpResponseStatus.SERVICE_UNAVAILABLE.code();

  public void handleRequest(final RoutingContext routingContext) {
    routingContext
        .response()
        .setStatusCode(isHealthy() ? healthyStatusCode() : nonHealthyStatusCode())
        .end();
  }

  public abstract boolean isHealthy();

  public static HealthService fromHealthCheck(final HealthCheck healthCheck) {
    return new HealthService() {
      @Override
      public boolean isHealthy() {
        return healthCheck.isHealthy();
      }
    };
  }

  public int healthyStatusCode() {
    return HEALTHY_STATUS_CODE;
  }

  public int nonHealthyStatusCode() {
    return NON_HEALTHY_STATUS_CODE;
  }

  @FunctionalInterface
  public interface HealthCheck {
    boolean isHealthy();
  }
}
