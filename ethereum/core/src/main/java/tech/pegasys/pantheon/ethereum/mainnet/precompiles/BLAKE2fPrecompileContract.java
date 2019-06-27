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
package tech.pegasys.pantheon.ethereum.mainnet.precompiles;

import tech.pegasys.pantheon.ethereum.core.Gas;
import tech.pegasys.pantheon.ethereum.mainnet.AbstractPrecompiledContract;
import tech.pegasys.pantheon.ethereum.vm.GasCalculator;
import tech.pegasys.pantheon.ethereum.vm.MessageFrame;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public class BLAKE2fPrecompileContract extends AbstractPrecompiledContract {
  public BLAKE2fPrecompileContract(final GasCalculator gasCalculator) {
    super("BLAKE2b", gasCalculator);
  }

  @Override
  public Gas gasRequirement(final BytesValue input) {
    return null;
  }

  @Override
  public BytesValue compute(final BytesValue input, final MessageFrame messageFrame) {
    if (input.size() != 213) {
      throw new IllegalArgumentException(
          "input length for Blake2 F precompile should be exactly 213 bytes");
    }

    return null;
  }
}
