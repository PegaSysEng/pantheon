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
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;

import com.google.common.base.Splitter;

public class CorsAllowedOriginsProperty extends AbstractCollection<String> {

  private List<String> domains = new ArrayList<>();

  private CorsAllowedOriginsProperty(final List<String> domains) {
    this.domains = domains;
  }

  public CorsAllowedOriginsProperty() {}

  @Override
  @Nonnull
  public Iterator<String> iterator() {
    return domains.iterator();
  }

  @Override
  public int size() {
    return domains.size();
  }

  @Override
  public boolean add(final String string) {
    for (final String s : Splitter.onPattern("\\s*,\\s*").split(string)) {
      if (string.isEmpty()) {
        throw new IllegalArgumentException("Domain cannot be empty string");
      } else if ("all".equals(s) || "*".equals(s)) {
        if (!domains.isEmpty()) {
          throw new IllegalArgumentException("Value '" + s + "' can't be used with other domains");
        } else {
          domains.add("*");
        }
      } else if ("none".equals(s)) {
        if (!domains.isEmpty()) {
          throw new IllegalArgumentException("Value '" + s + "' can't be used with other domains");
        }
      } else {
        domains.add(s);
        try {
          final StringJoiner stringJoiner = new StringJoiner("|");
          domains.forEach(stringJoiner::add);
          Pattern.compile(stringJoiner.toString());
        } catch (final PatternSyntaxException e) {
          throw new IllegalArgumentException("Domain values result in invalid regex pattern", e);
        }
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
