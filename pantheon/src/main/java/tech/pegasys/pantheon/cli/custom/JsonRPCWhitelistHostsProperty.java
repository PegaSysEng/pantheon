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
package tech.pegasys.pantheon.cli.custom;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;

import com.google.common.base.Splitter;

public class JsonRPCWhitelistHostsProperty extends AbstractCollection<String> {
  private List<String> hostnamesWhitelist = new ArrayList<>();

  private JsonRPCWhitelistHostsProperty(final List<String> hostnamesWhitelist) {
    this.hostnamesWhitelist = hostnamesWhitelist;
  }

  public JsonRPCWhitelistHostsProperty() {}

  @Override
  @Nonnull
  public Iterator<String> iterator() {
    return hostnamesWhitelist.iterator();
  }

  @Override
  public int size() {
    return hostnamesWhitelist.size();
  }

  @Override
  public boolean add(final String string) {
    for (String s : Splitter.onPattern("\\s*,\\s*").split(string)) {
      if (string.isEmpty()) {
        throw new IllegalArgumentException("Hostname cannot be empty string");
      } else if ("all".equals(s) || "*".equals(s)) {
        if (!hostnamesWhitelist.isEmpty()) {
          throw new IllegalArgumentException(
              "Value '" + s + "' can't be used with other hostnames");
        } else {
          hostnamesWhitelist.add("*");
        }
      } else {
        hostnamesWhitelist.add(s);
      }
    }
    return true;
  }

  @Override
  public boolean addAll(final Collection<? extends String> collection) {
    collection.forEach(this::add);
    return true;
  }
}
