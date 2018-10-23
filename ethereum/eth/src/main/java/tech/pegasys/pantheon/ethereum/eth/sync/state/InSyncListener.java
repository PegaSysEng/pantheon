package tech.pegasys.pantheon.ethereum.eth.sync.state;

@FunctionalInterface
public interface InSyncListener {
  void onSyncStatusChanged(boolean newSyncStatus);
}
