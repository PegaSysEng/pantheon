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
package tech.pegasys.pantheon.ethereum.eth.manager;

import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer.DisconnectCallback;
import tech.pegasys.pantheon.ethereum.eth.manager.RequestManager.ResponseStream;
import tech.pegasys.pantheon.ethereum.eth.manager.exceptions.NoAvailablePeersException;
import tech.pegasys.pantheon.ethereum.eth.manager.exceptions.PeerDisconnectedException;
import tech.pegasys.pantheon.ethereum.p2p.api.PeerConnection;
import tech.pegasys.pantheon.ethereum.p2p.api.PeerConnection.PeerNotConnected;
import tech.pegasys.pantheon.metrics.MetricCategory;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.util.Subscribers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EthPeers {
  public static final Comparator<EthPeer> TOTAL_DIFFICULTY =
      Comparator.comparing(
          ((final EthPeer p) -> p.chainState().getBestBlock().getTotalDifficulty()));

  public static final Comparator<EthPeer> CHAIN_HEIGHT =
      Comparator.comparing(((final EthPeer p) -> p.chainState().getEstimatedHeight()));

  public static final Comparator<EthPeer> BEST_CHAIN = TOTAL_DIFFICULTY.thenComparing(CHAIN_HEIGHT);

  public static final Comparator<EthPeer> LEAST_TO_MOST_BUSY =
      Comparator.comparing(EthPeer::outstandingRequests);

  private final int maxOutstandingRequests = 5;
  private final Map<PeerConnection, EthPeer> connections = new ConcurrentHashMap<>();
  private final String protocolName;
  private final Subscribers<ConnectCallback> connectCallbacks = new Subscribers<>();
  private final Subscribers<DisconnectCallback> disconnectCallbacks = new Subscribers<>();
  private final Collection<QueuedTask> pendingRequests = new ArrayList<>();

  public EthPeers(final String protocolName, final MetricsSystem metricsSystem) {
    this.protocolName = protocolName;
    metricsSystem.createIntegerGauge(MetricCategory.PEERS,
        "pending_peer_requests_current",
        "Number of peer requests currently pending because peers are busy",
        pendingRequests::size);
  }

  void registerConnection(final PeerConnection peerConnection) {
    final EthPeer peer = new EthPeer(peerConnection, protocolName, this::invokeConnectionCallbacks);
    connections.putIfAbsent(peerConnection, peer);
  }

  void registerDisconnect(final PeerConnection connection) {
    final EthPeer peer = connections.remove(connection);
    if (peer != null) {
      disconnectCallbacks.forEach(callback -> callback.onDisconnect(peer));
      peer.handleDisconnect();
    }
  }

  public EthPeer peer(final PeerConnection peerConnection) {
    return connections.get(peerConnection);
  }

  public CompletableFuture<ResponseStream> executePeerRequest(
      final PeerRequest request, final long minimumBlockNumber) {
    final QueuedTask queuedTask = new QueuedTask(request, minimumBlockNumber);
    synchronized (this) {
      if (!queuedTask.attemptExecution()) {
        pendingRequests.add(queuedTask);
      }
    }
    return queuedTask.result;
  }

  public void dispatchMessage(final EthPeer peer, final EthMessage ethMessage) {
    peer.dispatch(ethMessage);
    if (peer.outstandingRequests() < maxOutstandingRequests) {
      synchronized (this) {
        pendingRequests.removeIf(QueuedTask::attemptExecution);
      }
    }
  }

  public long subscribeConnect(final ConnectCallback callback) {
    return connectCallbacks.subscribe(callback);
  }

  public void unsubscribeConnect(final long id) {
    connectCallbacks.unsubscribe(id);
  }

  public long subscribeDisconnect(final DisconnectCallback callback) {
    return disconnectCallbacks.subscribe(callback);
  }

  public int peerCount() {
    return connections.size();
  }

  public Stream<EthPeer> availablePeers() {
    return connections.values().stream().filter(EthPeer::readyForRequests);
  }

  public Optional<EthPeer> bestPeer() {
    return availablePeers().max(BEST_CHAIN);
  }

  @FunctionalInterface
  public interface ConnectCallback {
    void onPeerConnected(EthPeer newPeer);
  }

  @Override
  public String toString() {
    final String connectionsList =
        connections.values().stream().map(EthPeer::toString).collect(Collectors.joining(","));
    return "EthPeers{connections=" + connectionsList + '}';
  }

  private void invokeConnectionCallbacks(final EthPeer peer) {
    connectCallbacks.forEach(cb -> cb.onPeerConnected(peer));
  }

  private class QueuedTask {
    private final PeerRequest request;
    private final CompletableFuture<ResponseStream> result = new CompletableFuture<>();
    private final long minimumBlockNumber;

    private QueuedTask(final PeerRequest request, final long minimumBlockNumber) {
      this.request = request;
      this.minimumBlockNumber = minimumBlockNumber;
    }

    public boolean attemptExecution() {
      if (!result.isDone()) {
        final Optional<EthPeer> leastBusySuitablePeer =
            availablePeers()
                .filter(peer -> peer.chainState().getEstimatedHeight() >= minimumBlockNumber)
                .min(LEAST_TO_MOST_BUSY);
        if (!leastBusySuitablePeer.isPresent()) {
          // No peers have the required height.
          result.completeExceptionally(new NoAvailablePeersException());
        } else {
          // At least one peer has the required height, but we not be able to use it if it's busy
          final Optional<EthPeer> selectedPeer =
              leastBusySuitablePeer.filter(
                  peer -> peer.outstandingRequests() < maxOutstandingRequests);
          selectedPeer.ifPresent(
              peer -> {
                try {
                  final ResponseStream responseStream = request.sendRequest(peer);
                  result.complete(responseStream);
                } catch (final PeerNotConnected e) {
                  result.completeExceptionally(new PeerDisconnectedException(peer));
                }
              });
          return selectedPeer.isPresent();
        }
      }
      return false;
    }
  }

  public interface PeerRequest {
    ResponseStream sendRequest(EthPeer peer) throws PeerNotConnected;
  }
}
