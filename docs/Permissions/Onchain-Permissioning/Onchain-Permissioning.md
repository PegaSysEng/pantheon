description: Onchain Permissioning
<!--- END of page meta data -->

# Onchain Permissioning 

Onchain permissioning uses smart contracts to store and maintain the node and account whitelists. Using onchain permissioning
enables all nodes to read the whitelists from one location.                                        

## Permissioning Contracts

The permissioning smart contracts are provided in the [PegaSysEng/permissioning-smart-contracts](https://github.com/PegaSysEng/permissioning-smart-contracts) repository: 

* Ingress contracts for nodes and accounts - simple contracts functioning as gateways to the Admin, Node Rules, 
 and Account Rules contracts. The Ingress contracts are deployed to static addresses. 

* Node Rules - stores the node whitelist and node whitelist operations (for example, add and remove). 

* Account Rules - stores the account whitelist and account whitelist operations (for example, add and remove). 

* Admin - stores the list of admin accounts and admin list operations (for example, add and remove) There is 
one list of admin accounts for node and accounts.

## Permissioning Management Dapp

The [Permissioning Management Dapp](https://github.com/PegaSysEng/permissioning-smart-contracts) is provided to view 
and maintain the whitelists. 

!!! tip 
    Before v1.2, we provided a [management interface using Truffle](https://docs.pantheon.pegasys.tech/en/1.1.4/Permissions/Onchain-Permissioning/).
    The management interface using Truffle is deprecated and we recommend using the Dapp for an improved user experience. 

### Whitelists 

Permissioning implements three whitelists: 

* [Accounts] that can submit transactions to the network

* [Nodes] that can participate in the network 

* [Admin Accounts] that can update the Accounts and Nodes whitelists 

## Bootnodes

When a node is added to the network, it connects to the bootnodes until it synchronizes to the chain head regardless of
node permissions. Once in sync, the permissioning rules are applied and connections to bootnodes are dropped if not permitted by node 
permissions.  

If a sychronized node loses all peer connections (that is, it has 0 peers), it reconnects to the bootnodes regardless of node 
permissions. When the node has connected to 1 or more non-bootnodes, connections to bootnodes are dropped if not permitted by node 
permissions.   

