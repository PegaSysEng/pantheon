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

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

public class UnformattedDataAdapterTest {

  @Test
  public void getByteArray() {
    final BytesValue bytesValue = randomBytes();
    assertThat(new UnformattedDataAdapter(bytesValue).getByteArray())
        .isEqualTo(bytesValue.extractArray());
  }

  @Test
  public void getHexString() {
    final BytesValue bytesValue = randomBytes();
    assertThat(new UnformattedDataAdapter(bytesValue).getHexString())
        .isEqualTo(bytesValue.toString());
  }

  @Test
  public void size() {
    final BytesValue bytesValue = randomBytes();
    assertThat(new UnformattedDataAdapter(bytesValue).size()).isEqualTo(bytesValue.size());
  }

  private BytesValue randomBytes() {
    final byte[] bytes = new byte[Hash.SIZE];
    ThreadLocalRandom.current().nextBytes(bytes);
    return Bytes32.wrap(bytes);
  }
}
