description: Configuring Pantheon at the network level compared to the node level 
<!--- END of page meta data -->

# Network vs Node Configuration 

Pantheon is configured at the network level and the node level. 

Network wide settings are specified in the genesis file.  For example, `difficulty` for Proof of Work 
networks or `epochlength` for IBFT 2.0 networks. 

Node settings are specified on the command line or in the [node configuration file](Using-Configuration-File.md). 
For example, the JSON-RPC API methods to enable or the data directory for the node. 