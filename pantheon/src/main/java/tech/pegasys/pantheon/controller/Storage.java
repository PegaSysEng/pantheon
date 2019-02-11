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
package tech.pegasys.pantheon.controller;

import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.chain.BlockchainStorage;
import tech.pegasys.pantheon.ethereum.chain.DefaultMutableBlockchain;
import tech.pegasys.pantheon.ethereum.chain.GenesisState;
import tech.pegasys.pantheon.ethereum.chain.MutableBlockchain;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.storage.StorageProvider;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateArchive;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateStorage;
import tech.pegasys.pantheon.metrics.MetricsSystem;

import java.util.function.BiFunction;

public class Storage<T> {

  private final ProtocolContext<T> protocolContext;
  private final WorldStateStorage worldStateStorage;

  private Storage(
      final ProtocolContext<T> protocolContext, final WorldStateStorage worldStateStorage) {
    this.protocolContext = protocolContext;
    this.worldStateStorage = worldStateStorage;
  }

  public static <T> Storage<T> initStorage(
      final StorageProvider storageProvider,
      final GenesisState genesisState,
      final ProtocolSchedule<T> protocolSchedule,
      final MetricsSystem metricsSystem,
      final BiFunction<Blockchain, WorldStateArchive, T> consensusContextFactory) {
    final BlockchainStorage blockchainStorage =
        storageProvider.createBlockchainStorage(protocolSchedule);
    final WorldStateStorage worldStateStorage = storageProvider.createWorldStateStorage();

    final MutableBlockchain blockchain =
        new DefaultMutableBlockchain(genesisState.getBlock(), blockchainStorage, metricsSystem);

    final WorldStateArchive worldStateArchive = new WorldStateArchive(worldStateStorage);
    genesisState.writeStateTo(worldStateArchive.getMutable());

    return new Storage<>(
        new ProtocolContext<>(
            blockchain,
            worldStateArchive,
            consensusContextFactory.apply(blockchain, worldStateArchive)),
        worldStateStorage);
  }

  public ProtocolContext<T> getProtocolContext() {
    return protocolContext;
  }

  public WorldStateStorage getWorldStateStorage() {
    return worldStateStorage;
  }
}
