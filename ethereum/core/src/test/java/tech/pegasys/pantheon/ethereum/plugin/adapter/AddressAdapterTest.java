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

import tech.pegasys.pantheon.ethereum.core.Address;

import org.junit.Test;

public class AddressAdapterTest {

  @Test
  public void getByteArray() {
    assertThat(new AddressAdapter(Address.SHA256).getByteArray())
        .isEqualTo(Address.SHA256.extractArray());
  }

  @Test
  public void getHexString() {
    assertThat(new AddressAdapter(Address.SHA256).getHexString())
        .isEqualTo(Address.SHA256.toString());
  }

  @Test
  public void size() {
    assertThat(new AddressAdapter(Address.SHA256).size()).isEqualTo(Address.SHA256.size());
  }
}
