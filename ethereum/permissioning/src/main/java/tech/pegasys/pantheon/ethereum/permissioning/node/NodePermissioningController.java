package tech.pegasys.pantheon.ethereum.permissioning.node;

import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.pantheon.ethereum.permissioning.node.provider.SyncStatusNodePermissioningProvider;
import tech.pegasys.pantheon.util.enode.EnodeURL;

public class NodePermissioningController {

  private static final Logger LOG = LogManager.getLogger();

  private final Optional<SyncStatusNodePermissioningProvider> syncStatusNodePermissioningProvider;
  private Optional<Runnable> peerDiscoveryCallback = Optional.empty();

  public NodePermissioningController(
      final SyncStatusNodePermissioningProvider syncStatusNodePermissioningProvider) {
    this.syncStatusNodePermissioningProvider = Optional.of(syncStatusNodePermissioningProvider);
    this.syncStatusNodePermissioningProvider.get().setHasReachedSyncCallback(this::onReachingSync);
  }

  public NodePermissioningController() {
    this.syncStatusNodePermissioningProvider = Optional.empty();
  }

  public boolean isPermitted(final EnodeURL sourceEnode, final EnodeURL destinationEnode) {
    LOG.trace("Checking node permission: {} -> {}", sourceEnode, destinationEnode);

    return syncStatusNodePermissioningProvider
        .map((provider) -> provider.isPermitted(sourceEnode, destinationEnode))
        .orElse(true);
  }

  public boolean delayPeerDiscovery() {
    return syncStatusNodePermissioningProvider
        .map((provider) -> !provider.hasReachedSync())
        .orElse(false);
  }

  public void peerDiscoveryCallback(final Runnable runnable) {
    this.peerDiscoveryCallback = Optional.of(runnable);
  }

  private void onReachingSync() {
    peerDiscoveryCallback.ifPresent(Runnable::run);
  }
}
