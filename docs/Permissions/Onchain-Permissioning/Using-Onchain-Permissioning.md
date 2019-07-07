description: Setting up and using onchain Permissioning
<!--- END of page meta data -->

# Using Onchain Permissioning 

To start a network with onchain permissioning: 

1. [Install pre-requisites](#pre-requisites) 
1. [Add the ingress contracts to the genesis file](#add-ingress-contracts-to-genesis-file) 
1. For the first node: 
    1. [Set environment variables](#set-environment-variables) 
    2. [Start node with onchain permissioning and the JSON-RPC HTTP service enabled](#onchain-permissioning-command-line-options) 
    3. [Clone the permissioning contracts repository and install dependencies](#clone-contracts-and-install-dependencies) 
    4. [Deploy the permissioning contracts](#deploy-contracts) 
    5. [Start the Permissioning Management Dapp](#start-the-permissioning-management-dapp) 
    6. [Add the first node to the nodes whitelist](#update-nodes-whitelist)
    7. Add subsequent [nodes](#update-nodes-whitelist) and [accounts](#update-accounts-whitelists) to the whitelists 

1. Start subsequent nodes with [onchain permssioning enabled](#onchain-permissioning-command-line-options).  
If nodes are going to maintain whitelists, also enable the [JSON-RPC HTTP service](#onchain-permissioning-command-line-options). 
1. For nodes that are going to maintain whitelists (that is, nodes with an admin account): 
    1. [Clone the permissioning contracts repository and install dependencies](#clone-contracts-and-install-dependencies)
    2. [Start the Permissioning Management Dapp](#start-the-permissioning-management-dapp)

## Pre-requisites 

For the first node and nodes with an admin account: 

* [NodeJS](https://nodejs.org/en/) v8.9.4 or later QUESTION: confirm 
* [Yarn](https://yarnpkg.com/en/) v1.15 or later
* Browser with [MetaMask installed](https://metamask.io/)

For the first node only: 

* [Truffle](https://truffleframework.com/docs/truffle/getting-started/installation)

## Add Ingress Contracts to Genesis File

!!! tip 
    If the network is using only account or nodes permissioning, add only the relevant ingress contract to the
    genesis file. 

Add the Ingress contracts to the genesis file for your network by copying them from [`genesis.json`](https://github.com/PegaSysEng/permissioning-smart-contracts/blob/master/genesis.json) 
in the [`permissioning-smart-contracts` repository](https://github.com/PegaSysEng/permissioning-smart-contracts): 
   
```json

"0x0000000000000000000000000000000000008888": {
      "comment": "Account Ingress smart contract",
      "balance": "0",
      "code": <stripped>,
      "storage": {
         <stripped>
      }
}

"0x0000000000000000000000000000000000009999": {
      "comment": "Node Ingress smart contract",
      "balance": "0",
      "code": <stripped>,
      "storage": {
         <stripped>
      }
}
```

!!! important 
    To support the permissioning contracts, ensure your genesis file includes at least the `constantinopleFixBlock` milestone. 

## Set Environment Variables 

For the first node (that is, the node that will deploy the contracts), create the following environment variables 
and set to the specified values: 

* `PANTHEON_NODE_PERM_ACCOUNT` - address of account used to interact with the permissioning contracts. 

* `PANTHEON_NODE_PERM_KEY` - private key of the account used to interact with the permissioning contracts.

* `ACCOUNT_INGRESS_CONTRACT_ADDRESS` - address of the Account Ingress contract in the genesis file.  

* `NODE_INGRESS_CONTRACT_ADDRESS` - address of the Node Ingress contract in the genesis file.  

* `PANTHEON_NODE_PERM_ENDPOINT` - required only if your node is not using the default JSON-RPC host and port (`http://127.0.0.1:8545`). 
Set to JSON-RPC host and port. 

!!! note
    If your network is not a [free gas network](../../Configuring-Pantheon/FreeGas.md), the account used to 
    interact with the permissioning contracts must have a balance. 

## Onchain Permissioning Command Line Options
   
All nodes participating in a permissioned network must include the command line options to enable account and/or
node permissioning: 

* [--permissions-accounts-contract-enabled](../../Reference/Pantheon-CLI-Syntax.md#permissions-accounts-contract-enabled)
to enable onchain accounts permissioning
          
* [--permissions-accounts-contract-address](../../Reference/Pantheon-CLI-Syntax.md#permissions-accounts-contract-address)
set to the address of the Account Ingress contract in the genesis file (`"0x0000000000000000000000000000000000008888"`)

* [--permissions-nodes-contract-enabled](../../Reference/Pantheon-CLI-Syntax.md#permissions-nodes-contract-enabled)
to enable onchain nodes permissioning

* [--permissions-nodes-contract-address](../../Reference/Pantheon-CLI-Syntax.md#permissions-nodes-contract-address)
set to the address of the Node Ingress contract in the genesis file (`"0x0000000000000000000000000000000000009999"`)
      
For nodes using the Permissioning Management Dapp to maintain whitelists, the [--rpc-http-enabled](../../Reference/Pantheon-CLI-Syntax.md#rpc-http-enabled)
option must be specified to enable the JSON-RPC HTTP service.  

## Clone Contracts and Install Dependencies 

1. Clone the `permissioning-smart-contracts` repository: 

    ```bash 
    git clone https://github.com/PegaSysEng/permissioning-smart-contracts.git
    ```

1. Change into the `permissioning-smart-contracts` directory and run:  

    ```bash
    yarn install
    ```

## Deploy Contracts 

!!! important 
    Only the first node deploys the admin and rules contracts. Subsequent nodes do not migrate the contracts. 
    
    The node deploying the admin and rules contracts must be a miner (PoW networks) or validator (PoA networks). 

In the `permissioning-smart-contracts` directory, deploy the Admin and Rules contracts: 

```bash
truffle migrate --reset
```

The Admin and Rules contracts are deployed and the Ingress contract updated with the name and version of the contracts. 
The migration logs the addresses of the Admin and Rules contracts. 

!!! important 
    The account that deploys the contracts is automatically an [admin account](#update-accounts-or-admin-accounts-whitelists). 

## Start the Permissioning Management Dapp

1. In the `permissioning-smart-contracts` directory: 

    ```bash
    yarn start
    ```

    The Dapp is displayed at [http://localhost:3000](http://localhost:3000). 

1. Ensure MetaMask is connected to your local node (by default `http://localhost:8545`). 

    A MeteMask notification is displayed requesting permission for Pantheon Permissioning to 
   connect to your account. 

1. Click the _Connect_ button. 

    The Dapp is displayed with the account specified by the `PANTHEON_NODE_PERM_ACCOUNT` environment variable 
   in the _Whitelisted Accounts_ and _Admin Accounts_ tabs. 

!!! note  
    Only [admin accounts](#update-accounts-or-admin-accounts-whitelists) can add or remove nodes from the whitelist. 

    The first node (that is, the node that deployed the permissioning contracts) must add itself to the whitelist before 
    adding other nodes.

## Update Nodes Whitelist  

To add a node to the nodes whitelist: 

1. In the _Whitelisted Nodes_ tab of the Permissioning Management Dapp, click the _Add Whitelisted Nodes_
   button. The add node window is displayed.

2. Enter the [enode URL](../../Configuring-Pantheon/Node-Keys.md#enode-url) of the node to be added and click 
the _Add Whitelisted Node_ button. 

To remove a node from the nodes whitelist: 

1. In the _Whitelisted Nodes_ tab of the Permissioning Management Dapp, hover over the row of the node to remove. 
A trash can is displayed. 

1. Click on the trash can.  

!!! tip
    If you add a running node, the node does not attempt to reconnect to the bootnode and synchronize until 
    peer discovery restarts.  To add a whitelisted node as a peer without waiting for peer discovery to restart, use [`admin_addPeer`](../../Reference/Pantheon-API-Methods.md#admin_addpeer). 

    If the node is added to the whitelist before starting the node, using `admin_addPeer` is not required because
    peer discovery is run on node startup. 
    
## Update Accounts or Admin Accounts Whitelists 

To add an account to the accounts whitelist or as an admin account: 

1. In the _Whitelisted Accounts_ or _Admin Accounts_ tab of the Permissioning Management Dapp, click the _Add Whitelisted Account_
or _Add Admin Account_ button. The add account window is displayed. 

1. Enter the account address in the _Account Address_ field and click the _Add Whitelisted Account_ or _Add Admin Account_ button. 

To remove an account from the accounts whitelist or as an admin account: 

1. In the _Whitelisted Accounts_ or _Admin Accounts_ tab of the Permissioning Management Dapp, hover over the 
row of the account to be removed. A trash can is displayed. 

1. Click on the trash can.     
