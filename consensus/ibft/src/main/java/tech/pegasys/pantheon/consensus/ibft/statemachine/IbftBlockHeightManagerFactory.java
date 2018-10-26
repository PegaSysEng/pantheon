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
package tech.pegasys.pantheon.consensus.ibft.statemachine;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;

import java.util.Collection;

public class IbftBlockHeightManagerFactory {

  private final IbftRoundFactory roundFactory;
  private final IbftImmutableState immutableState;

  public IbftBlockHeightManagerFactory(
      final IbftImmutableState immutableState, final IbftRoundFactory roundFactory) {
    this.roundFactory = roundFactory;
    this.immutableState = immutableState;
  }

  public IbftBlockHeightManager create(final BlockHeader parentHeader) {
    final Collection<Address> validators = immutableState.getValidators();
    return new IbftBlockHeightManager(
        parentHeader,
        immutableState,
        /*
        new RoundChangeManager(
            IbftHelpers.calculateRequiredValidatorQuorum(validators.size()),
            parentHeader,
            immutableState),
            */
        roundFactory);
  }
}
