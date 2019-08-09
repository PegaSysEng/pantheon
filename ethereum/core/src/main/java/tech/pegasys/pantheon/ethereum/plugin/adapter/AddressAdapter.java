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

import tech.pegasys.pantheon.ethereum.core.Address;

public class AddressAdapter implements tech.pegasys.pantheon.plugin.data.Address {
  private final Address address;

  public AddressAdapter(final Address address) {
    this.address = address;
  }

  @Override
  public byte[] getByteArray() {
    return address.extractArray();
  }

  @Override
  public String getHexString() {
    return address.toString();
  }

  @Override
  public int size() {
    return address.size();
  }
}
