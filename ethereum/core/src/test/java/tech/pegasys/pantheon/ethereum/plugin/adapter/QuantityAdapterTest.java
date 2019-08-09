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
package tech.pegasys.pantheon.ethereum.plugin.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.util.uint.UInt256;

import org.junit.Test;

public class QuantityAdapterTest {

  @Test
  public void getValue() {
    final UInt256 uInt256 = UInt256.of(5L);
    assertThat(new QuantityAdapter(uInt256).getValue()).isEqualTo(uInt256.toLong());
  }

  @Test
  public void getByteArray() {
    final UInt256 uInt256 = UInt256.of(5L);
    assertThat(new QuantityAdapter(uInt256).getByteArray())
        .isEqualTo(uInt256.getBytes().extractArray());
  }

  @Test
  public void getHexString() {
    final UInt256 uInt256 = UInt256.of(5L);
    assertThat(new QuantityAdapter(uInt256).getHexString()).isEqualTo(uInt256.toHexString());
  }

  @Test
  public void size() {
    final UInt256 uInt256 = UInt256.of(5L);
    assertThat(new QuantityAdapter(uInt256).size()).isEqualTo(uInt256.getBytes().size());
  }
}
