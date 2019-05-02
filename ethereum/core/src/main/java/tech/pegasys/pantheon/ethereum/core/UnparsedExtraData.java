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
package tech.pegasys.pantheon.ethereum.core;

import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Objects;

import com.google.common.base.MoreObjects;

public class UnparsedExtraData implements ExtraData {

  private final BytesValue vanityData;

  public UnparsedExtraData(final BytesValue vanityData) {
    this.vanityData = vanityData;
  }

  @Override
  public BytesValue toBytesValue() {
    return vanityData;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final UnparsedExtraData that = (UnparsedExtraData) o;
    return Objects.equals(vanityData, that.vanityData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vanityData);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("vanityData", vanityData).toString();
  }
}
