description: Pantheon Permissions feature
<!--- END of page meta data -->

# Permissions 

A permissioned network is a network where only specified nodes and accounts (participants) can participate.  
Nodes and accounts outside those specified are prevented from participating. 

## Node Whitelisting 

Node whitelisting is specified at the node level by the `nodes-whitelist` in the `permissions_config.toml` file. 
A node with node permissions enabled only communicates with nodes in the `nodes-whitelist`. 

!!! example="Nodes Whitelist in `permissons_config.toml`
    `nodes-whitelist=["enode://6f8a80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0@192.168.0.9:4567","enode://6f8a80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0@192.169.0.9:4568"]`

The network bootnodes must be specified in the nodes whitelist and cannot be removed from the nodes whitelist. 

To add or remove nodes from the nodes whitelist when the node is running, use the JSON-RPC API methods: 
* [perm_addNodesToWhitelist](../Reference/JSON-RPC-API-Methods.md#perm__addnodestowhitelist)
* [perm_removeNodesFromWhitelist](../Reference/JSON-RPC-API-Methods.md#perm_removeNodesFromWhiteList)
* [perm_getNodesWhitelist](../Reference/JSON-RPC-API-Methods.md#perm_getNodesWhiteList)

!!! note
    Because each node has a `permissions_config.toml` file, nodes in the network can have different node whitelists. 
    This means nodes may be participating in the network that are not on the whitelist of other nodes in the network. 
    We recommend each node in the network has the same nodes whitelist. 
    
    On-chain permissioning is under development. On-chain permissioning will use one on-chain 
    node whitelist. 
    
!!! example="Different Node Whitelists" 
    Node 1 Whitelist = [Node 2, Node 3] 
    Node 2 Whitelist = [Node 3, Node 5] 
    Node 5 is participating in the same network as Node 1 even though Node 1 does not have Node 5 on their whitelist. 

## Account Whitelisting 

Accounts whitelisting is specified at the node level by the `accounts-whitelist` in the `permissions_config.toml` file.
A node with account permissions will only accept transactions from accounts in the `accounts-whitelist`. (JSON-RPC, DevP2P, vs block)

!!! example="Accounts Whitelist in `permissons_config.toml`
    `accounts-whitelist=["0x0000000000000000000000000000000000000009"]`
    
Transactions are validated against the accounts whitelist at the following points: 
1. Submitted by JSON-RPC API method `eth_sendRawTransaction` 
1. Received via propagation from another node 
1. Added to a block by a miner 

Once added to a block, the transactions are not validated against the whitelist when received by a node. That is, a node 
can have transactions in the blockchain from accounts that were not on the account whitelist for that node.      
    
!!! example="Different Account Whitelists
    Node 1 Whitelist = [Account A, Account B]
    Node 2 Whitelist = [Account B, Account C]
    Mining Node Whitelist = [Account A, Account B]
    Account A submits a transaction on Node 1. Node 1 validates and propagates the transaction. The Mining Node receives the transaction and 
    validates it is from an account in it's account whitelist and includes the transaction in the block. Node 2 receives   
    and adds the block. Node 2 now has a transaction in the blockchain from an account that is not in it's whitelist.   

!!! note
    Because each node has a `permissions_config.toml` file, nodes in the network can have different accounts whitelists. 
    This means a transaction maybe successfully submitted by one node with the account in it's whitelist but rejected by a 
    node it is propagated to that does not have the account in it's whitelist. 
    We recommend each node in the network has the same nodes whitelist.
        
    On-chain permissioning is under development. On-chain permissioning will use one onchain 
    node whitelist. 

To add or remove accounts from the accounts whitelist when the node is running, use the JSON-RPC API methods: 
* [perm_addAccountsToWhitelist](../Reference/JSON-RPC-API-Methods.md#perm_addAccountsToWhitelist)
* [perm_removeAccountsFromWhitelist](../Reference/JSON-RPC-API-Methods.md#perm_removeAccountsFromWhitelist)
* [perm_getAccountsWhitelist](../Reference/JSON-RPC-API-Methods.md#perm_getAccountsWhitelist)


## permissions_config.toml File 

The `permissions_config.toml` file contains the nodes and accounts whitelists. The `permissions_config.toml` 
must be in the [data directory](../Reference/Pantheon-CLI-Syntax.md#data-path) for the node.

!!! example "permissioning_config.toml"  


