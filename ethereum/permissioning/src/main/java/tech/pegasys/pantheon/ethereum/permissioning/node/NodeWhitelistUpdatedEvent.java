package tech.pegasys.pantheon.ethereum.permissioning.node;

import com.google.common.base.Objects;
import java.util.Collections;
import java.util.List;
import tech.pegasys.pantheon.util.enode.EnodeURL;

public class NodeWhitelistUpdatedEvent {

  private final List<EnodeURL> addedNodes;
  private final List<EnodeURL> removedNodes;

  public NodeWhitelistUpdatedEvent(final List<EnodeURL> addedNodes,
      final List<EnodeURL> removedNodes) {
    this.addedNodes = addedNodes != null ? addedNodes : Collections.emptyList();
    this.removedNodes = removedNodes != null ? removedNodes : Collections.emptyList();
  }

  public List<EnodeURL> getAddedNodes() {
    return addedNodes;
  }

  public List<EnodeURL> getRemovedNodes() {
    return removedNodes;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeWhitelistUpdatedEvent that = (NodeWhitelistUpdatedEvent) o;
    return Objects.equal(addedNodes, that.addedNodes) &&
        Objects.equal(removedNodes, that.removedNodes);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(addedNodes, removedNodes);
  }
}
