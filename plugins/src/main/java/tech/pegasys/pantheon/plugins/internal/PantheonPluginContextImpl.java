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
package tech.pegasys.pantheon.plugins.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import tech.pegasys.pantheon.plugins.PantheonContext;
import tech.pegasys.pantheon.plugins.PantheonPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

public class PantheonPluginContextImpl implements PantheonContext {

  private enum Lifecycle {
    UNINITIALZED,
    REGISTERING,
    REGISTERED,
    STARTING,
    STARTED,
    STOPPING,
    STOPPED
  }

  private Lifecycle state = Lifecycle.UNINITIALZED;
  private final Map<Class<?>, ? super Object> serviceRegistry = new HashMap<>();
  private ServiceLoader<PantheonPlugin> plugins;

  public void addService(final Class<?> serviceType, final Object service) {
    checkArgument(serviceType.isInterface(), "Services must be Java interfaces.");
    checkArgument(
        serviceType.isInstance(service),
        "The service registered with a type must implement that type");
    serviceRegistry.put(serviceType, service);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Optional<T> getService(final Class<T> serviceType) {
    return (Optional<T>) Optional.ofNullable((Map<Class<T>, T>) serviceRegistry.get(serviceType));
  }

  public void registerPlugins() {
    checkState(state == Lifecycle.UNINITIALZED, "PantheonContext has already beein registered.");
    state = Lifecycle.REGISTERING;
    plugins = ServiceLoader.load(PantheonPlugin.class);
    for (final PantheonPlugin plugin : plugins) {
      plugin.register(this);
    }
    state = Lifecycle.REGISTERED;
  }

  public void startPlugins() {
    checkState(
        state == Lifecycle.REGISTERED,
        "PantheonContext should be in state %s but it was in %s",
        Lifecycle.REGISTERED,
        state);
    state = Lifecycle.STARTING;
    plugins = ServiceLoader.load(PantheonPlugin.class);
    for (final PantheonPlugin plugin : plugins) {
      plugin.start();
    }
    state = Lifecycle.STARTED;
  }

  public void stopPlugins() {
    checkState(
        state == Lifecycle.STARTED,
        "PantheonContext should be in state %s but it was in %s",
        Lifecycle.STARTED,
        state);
    state = Lifecycle.STOPPING;
    plugins = ServiceLoader.load(PantheonPlugin.class);
    for (final PantheonPlugin plugin : plugins) {
      plugin.stop();
    }
    state = Lifecycle.STOPPED;
  }
}
