package tech.pegasys.pantheon.ethereum.permissioning.node;

import tech.pegasys.pantheon.util.enode.EnodeURL;

public interface NodePermissioningProvider {

  boolean isPermitted(final EnodeURL sourceEnode, final EnodeURL destinationEnode);
}
