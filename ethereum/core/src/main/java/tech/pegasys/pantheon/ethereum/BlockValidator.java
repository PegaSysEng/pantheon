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
package tech.pegasys.pantheon.ethereum;

import tech.pegasys.pantheon.ethereum.MainnetBlockValidator.BlockValidationOutputs;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.TransactionReceipt;
import tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode;

import java.util.List;
import java.util.Optional;

public interface BlockValidator<C> {

  Optional<BlockValidationOutputs> validateAndProcessBlock(
      ProtocolContext<C> context,
      Block block,
      HeaderValidationMode headerValidationMode,
      HeaderValidationMode ommerValidationMode);

  boolean fastBlockValidation(
      final ProtocolContext<C> context,
      final Block block,
      final List<TransactionReceipt> receipts,
      final HeaderValidationMode headerValidationMode);
}
