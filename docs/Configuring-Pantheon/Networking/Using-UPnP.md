description: Configuring UPnP with Pantheon
<!--- END of page meta data -->

# UPnP

Enable UPnP to quickly allow inbound peer connections without manual router configuration. UPnP is used 
in home or small office environments where a wireless router or modem provides NAT isolation. 

UPnP provides automatic detection that a node is running in a UPnP environment and port forwarding. 

!!! tip 
    UPnP support is often disabled by default in networking firmware. If disabled by default, explicitly
    enable UPnP support. 
    
## Enabling UPnP 

Use the [`--nat-method`](../../Reference/Pantheon-CLI-Syntax.md#nat-method) command line option to enable UPnP.

!!! note
    Enabling UPnP may slow down node startup, especially on networks without a UPnP gateway device.

When UPnP is enabled: 

* Enode advertised to other nodes during discovery is the external IP address and port. 
* External address and port are returned by the [`admin_NodeInfo`](../../Reference/Pantheon-API-Methods.md#admin_nodeinfo)
  JSON-RPC API method for the `enode` and `listenAddr` properties. 
  
While Pantheon is running, UPnP does not support: 

* IP address changes
* Disabling UPnP. To disable UPnP, restart the node without the [`--nat-method`](../../Reference/Pantheon-CLI-Syntax.md#nat-method)
option or set to `NONE`. 