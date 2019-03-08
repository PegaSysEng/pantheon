package tech.pegasys.pantheon.ethereum.permissioning;


import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.permissioning.node.NodePermissioningProvider;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulator;
import tech.pegasys.pantheon.util.enode.EnodeURL;

public class SmartContractPermissioningController implements NodePermissioningProvider {

  private final Address contractAddress;
  private final TransactionSimulator transactionSimulator;

  public SmartContractPermissioningController(
      final Address contractAddress, final TransactionSimulator transactionSimulator) {
    this.contractAddress = contractAddress;
    this.transactionSimulator = transactionSimulator;
  }


  @Override
  public boolean isPermitted(final EnodeURL sourceEnode, final EnodeURL destinationEnode) {
    return true;
  }
}
