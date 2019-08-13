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
package tech.pegasys.pantheon.ethereum.privacy;

import tech.pegasys.pantheon.enclave.Enclave;

// What does this represent?
// An enclave?
public class EnclaveUserDetails {
  private String enclavePublicKey;
  private Enclave enclave;

  public EnclaveUserDetails(final String enclavePublicKey, final Enclave enclave) {
    this.enclavePublicKey = enclavePublicKey;
    this.enclave = enclave;
  }

  public String getEnclavePublicKey() {
    return enclavePublicKey;
  }

  public void setEnclavePublicKey(final String enclavePublicKey) {
    this.enclavePublicKey = enclavePublicKey;
  }

  public Enclave getEnclave() {
    return enclave;
  }

  public void setEnclave(final Enclave enclave) {
    this.enclave = enclave;
  }
}
