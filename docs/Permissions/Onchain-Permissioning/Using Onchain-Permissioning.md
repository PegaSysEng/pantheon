description: Setting up and using onchain Permissioning
<!--- END of page meta data -->

# Using Onchain Permissioning 

Node in permissioned networks are one of: 

* First node in the permissioned network: 
  1. Starts node with permissioning command line options 
  1. Deploys the permissioning contracts
  1. Starts Dapp
  1. Adds itself to nodes whitelist

* Nodes with an admin account:
  1. Starts node with permissioning command line options 
  1. Starts Dapp 

* Nodes participating in network without admin access:
  1. Starts node with permissioning command line options 

## Pre-requisites 

For the first node and nodes with an admin account: 

* [NodeJS](https://nodejs.org/en/) v8.9.4 or later QUESTION: confirm 
* [Yarn](https://yarnpkg.com/en/) v1.15 or later
* Browser with [MetaMask installed](https://metamask.io/)

For the first node to deploy the permissioning contracts: 

* [Truffle](https://truffleframework.com/docs/truffle/getting-started/installation)

## Add Ingress Contracts to Genesis File

!!! tip UPDATE WORDING 
    A permissioned network can have account and nodes permissioning enabled or only account or node permissioning. 
    The following documentation describes starting a network with account and nodes permissioning enabled.  If using 
    only one type of permissioning, leave those parts out. 

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
    
## Onchain Permissioning Command Line Options
   
All nodes participating in a permissioned network must include the command line options: 

* [--permissions-accounts-contract-enabled](../../Reference/Pantheon-CLI-Syntax.md#permissions-accounts-contract-enabled)
to enable onchain accounts permissioning
          
* [--permissions-accounts-contract-address](../../Reference/Pantheon-CLI-Syntax.md#permissions-accounts-contract-address)
set to the address of the Account Ingress contract in the genesis file (`"0x0000000000000000000000000000000000008888"`)

* [--permissions-nodes-contract-enabled](../../Reference/Pantheon-CLI-Syntax.md#permissions-nodes-contract-enabled)
to enable onchain nodes permissioning

* [--permissions-nodes-contract-address](../../Reference/Pantheon-CLI-Syntax.md#permissions-nodes-contract-address)
set to the address of the Node Ingress contract in the genesis file (`"0x0000000000000000000000000000000000009999"`)
      
* [--rpc-http-enabled](../../Reference/Pantheon-CLI-Syntax.md#rpc-http-enabled) to enable JSON-RPC. QUESTION: Only for nodes with admin access? 

## Set Environment Variables 

Create the following environment variables and set to the specified values: 

* `PANTHEON_NODE_PERM_ACCOUNT` - address of account used to interact with the permissioning contracts. 

* `PANTHEON_NODE_PERM_KEY` - private key of the account used to interact with the permissioning contracts.

* `ACCOUNT_INGRESS_CONTRACT_ADDRESS` - address of the Account Ingress contract in the genesis file.  

* `NODE_INGRESS_CONTRACT_ADDRESS` - address of the Node Ingress contract in the genesis file.  

* `PANTHEON_NODE_PERM_ENDPOINT` - required only if your node is not using the default JSON-RPC host and port (`http://127.0.0.1:8545`). 
Set to JSON-RPC host and port. 

!!! note
    If your network is not a [free gas network](../../Configuring-Pantheon/FreeGas.md), the account used to 
    interact with the permissioning contracts must have a balance. 

## Clone Contracts

1. Clone the `permissioning-smart-contracts` repository: 

    ```bash 
    git clone https://github.com/PegaSysEng/permissioning-smart-contracts.git
    ```

1. Change into the `permissioning-smart-contracts` directory and run:  QUESTION: Do you need to do this if you're just running the Dapp? 
   (ie, not deploying the contracts)

    ```bash
    yarn install
    ```

## Deploy Contracts 

!!! important 
    Only the first node deploys the admin and rules contract. Subsequent nodes do not migrate the contracts. 
    
    The node deploying the admin and rules contracts must be a miner (PoW networks) or validator (PoA networks). 

In the `permissioning-smart-contracts` directory, deploy the Admin and Rules contracts: 

```bash
truffle migrate --reset
```

The Admin and Rules contracts are deployed and the Ingress contract updated with the name and version of the contracts. 
The migration logs the addresses of the Admin and Rules contracts. 

!!! important 
    The account that deploys the contracts is automatically an [Admin account](#add-and-remove-admin-accounts). 

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
    Only [Admin accounts](#add-and-remove-admin-accounts) can add or remove nodes from the whitelist. 

    The first node (that is, the node that deployed the permissioning contracts) must add itself to the whitelist before 
    adding other nodes.

## Update Nodes Whitelist <UP TO HERE> 

To add a node to the nodes whitelist: 

1. Enter the [enode URL](../../Configuring-Pantheon/Node-Keys.md#enode-url) of the node to be added or removed. 

To remove a nodes from the nodes whitelist: 

1. Enter the [enode URL](../../Configuring-Pantheon/Node-Keys.md#enode-url) of the node to be added or removed. 

!!! tip
    If you add a running node, the node does not attempt to reconnect to the bootnode and synchronize until 
    peer discovery restarts.  To add a whitelisted node as a peer without waiting for peer discovery to restart, use [`admin_addPeer`](../../Reference/Pantheon-API-Methods.md#admin_addpeer). 

    If the node is added to the whitelist before starting the node, using `admin_addPeer` is not required because
    peer discovery is run on node startup. 
    
## Update Accounts Whitelists <NEED TO ADD ADMIN ACCOUNTS> 

To add a whitelisted account or admin account: 

1. In the _Whitelisted Accounts_ tab of the Permissioning Management Dapp, click the _Add Whitelisted Account_
button. The _Add Whitelisted Account_ window is displayed. 

1. Enter the account address in the _Account Address_ field and click the _Add Whitelisted Account_ button. 

To remove an account from the accounts whitelist: 
    
## Start Other Network Nodes <UPDATE> 

For participating nodes that are not going to add or remove nodes from the whitelist:

1. Start the nodes including the following options: 

    * [--permissions-nodes-contract-enabled](../../Reference/Pantheon-CLI-Syntax.md#permissions-nodes-contract-enabled)
     to enable onhcain permissioning
    
    * [--permissions-nodes-contract-address](../../Reference/Pantheon-CLI-Syntax.md#permissions-nodes-contract-address)
     set to the address of the Ingress contract in the genesis file (`"0x0000000000000000000000000000000000009999"`)

    * [--bootnodes](../../Reference/Pantheon-CLI-Syntax.md#bootnodes) set to the first node (that is, the node that [deployed
     the Admin and Rules contracts](#deploy-admin-and-rules-contracts).      

1. Copy the [enode URL](../../Configuring-Pantheon/Node-Keys.md#enode-url) and [have node added to the whitelist](#add-and-remove-nodes-from-the-whitelist).

For participating nodes that are going to add or remove nodes from the whitelist: 

1. Ensure [prerequisites installed](#pre-requisites).

1. Complete [permissioning setup](#onchain-permissioning-setup).

1. Copy the [enode URL](../../Configuring-Pantheon/Node-Keys.md#enode-url) and [have node added to the whitelist](#add-and-remove-nodes-from-the-whitelist).

1. Have account for node that will interact with permissioning contracts added as an [admin account](#add-and-remove-admin-accounts). 
