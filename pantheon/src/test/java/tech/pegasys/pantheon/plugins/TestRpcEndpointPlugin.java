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
package tech.pegasys.pantheon.plugins;

import static com.google.common.base.Preconditions.checkArgument;

import tech.pegasys.pantheon.plugin.PantheonContext;
import tech.pegasys.pantheon.plugin.PantheonPlugin;
import tech.pegasys.pantheon.plugin.services.RpcEndpointService;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.auto.service.AutoService;

@AutoService(PantheonPlugin.class)
public class TestRpcEndpointPlugin implements PantheonPlugin {

  static class Bean {
    final String foo = "bar";
    final String baz = "qux";
    final String value;

    Bean(final String value) {
      this.value = value;
    }

    public String getFoo() {
      return foo;
    }

    public String getBaz() {
      return baz;
    }

    public String getValue() {
      return value;
    }
  }

  private final AtomicReference<String> storage = new AtomicReference<>("InitialValue");

  private String replaceValue(final List<String> strings) {
    checkArgument(strings.size() == 1, "Only one parameter accepted");
    return storage.getAndSet(strings.get(0));
  }

  private String[] replaceValueArray(final List<String> strings) {
    return new String[] {replaceValue(strings)};
  }

  private Bean replaceValueBean(final List<String> strings) {
    return new Bean(replaceValue(strings));
  }

  private long replaceValueLength(final List<String> strings) {
    // The NullPointerException risk here is deliberate and used in acceptance tests.
    return replaceValue(strings).length();
  }

  @Override
  public void register(final PantheonContext context) {
    context
        .getService(RpcEndpointService.class)
        .ifPresent(
            rpcEndpointService -> {
              rpcEndpointService.registerRPCEndpoint(
                  "unitTests", "replaceValue", this::replaceValue);
              rpcEndpointService.registerRPCEndpoint(
                  "unitTests", "replaceValueArray", this::replaceValueArray);
              rpcEndpointService.registerRPCEndpoint(
                  "unitTests", "replaceValueBean", this::replaceValueBean);
              rpcEndpointService.registerRPCEndpoint(
                  "unitTests", "replaceValueLength", this::replaceValueLength);
            });
  }

  @Override
  public void start() {
    // nothing to do
  }

  @Override
  public void stop() {
    // nothing to do
  }
}
