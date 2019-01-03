# Permissioning 

A permissioned network is a network where only specified nodes and participants (accounts)can participate.  
Nodes and accounts outside those specified are prevented from participating. 

The nodes whitelist defines the nodes that can participate in the network. 

Each node has an accounts whitelist that defines the nodes that can submit transactions to that node. 

Example use-case 

Diagram showing nodes and accounts 





## Create a Permissioned Network

### Node Whitelisting 

Use the [--nodes-whitelist option](../Reference/Pantheon-CLI-Syntax.md#nodes-whitelist) to specify the nodes
that can participate in the permissioned network on startup. You can specify them on the command line or
in a configuration file. <link to information on using configuration file> 

<add examples - command line and configuration file> 

The network bootnodes must be specified in the nodes whitelist.  

To add or remove nodes from the nodes whitelist when the network is running, use the JSON-RPC API methods: 
* [perm_addNodesToWhitelist](../Reference/JSON-RPC-API-Methods.md#perm__addnodestowhitelist)
* [perm_removeNodesFromWhitelist](../Reference/JSON-RPC-API-Methods.md#perm_removeNodesFromWhiteList)
* [perm_getNodesWhitelist](../Reference/JSON-RPC-API-Methods.md#perm_getNodesWhiteList)

!!!important
   Changes to the nodes whitelist using the JSON-RPC API are not persisted across node restarts.

<you'll have to repeat the addition or removal for each node won't you?> <what happens if nodes have different whitelists?> 

Network bootnodes cannot be removed from the nodes whitelist. 

### Account Whitelisting 

Use the [--accounts-whitelist option](../Reference/Pantheon-CLI-Syntax.md#accounts-whitelist) to specify the accounts
that can submit transactions to the node on startup. You can specify them on the command line or
in a configuration file. <link to information on using configuration file> 

<can any account send transactions from a node?> 

To add or remove accounts from the accounts whitelist when the network is running, use the JSON-RPC API methods: 
* [perm_addAccountsToWhitelist](../Reference/JSON-RPC-API-Methods.md#perm_addAccountsToWhitelist)
* [perm_removeAccountsFromWhitelist](../Reference/JSON-RPC-API-Methods.md#perm_removeAccountsFromWhitelist)
* [perm_getAccountsWhitelist](../Reference/JSON-RPC-API-Methods.md#perm_getAccountsWhitelist)

!!!important
   Changes to the nodes whitelist using the JSON-RPC API are not persisted across node restarts.

## Maintain a Permissioned Network 


### Add or Remove Nodes 


### Add or Remove Accounts 
