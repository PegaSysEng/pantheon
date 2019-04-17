description: Onchain Permissioning
<!--- END of page meta data -->

# Onchain Permissioning 

Onchain permissioning uses smart contracts to store and maintain the node whitelist. Using onchain permissioning
enables all nodes to read the node whitelist from one location. 
                                                                       
!!! note
    Pantheon supports onchain permissioning for nodes. Onchain permissioning for accounts will be available 
    in a future Pantheon release. 

Onchain permissioning uses the following smart contracts: 

* Ingress - a simple contract functioning as a gateway to the Rules contract. The Ingress contract is deployed 
to a static address. 

* Rules - stores the node whitelist and list of admin accounts. 

## Pre-requisites 

For nodes that are going to set up or maintain the nodes whitelist: 

* [NodeJS](https://nodejs.org/en/) v8.9.4 or later 

* [Truffle](https://truffleframework.com/docs/truffle/getting-started/installation)

## Add Ingress Contract to Genesis File

Add the Ingress contract to the genesis file for your network by copying it from [`genesis.json`](https://github.com/PegaSysEng/permissioning-smart-contracts/blob/master/genesis.json) 
in the [`permissioning-smart-contracts` repository](https://github.com/PegaSysEng/permissioning-smart-contracts): 
   
```json
"0x0000000000000000000000000000000000009999": {
      "comment": "Ingress smart contract",
      "balance": "0",
      "code": <stripped>,
      "storage": {
         <stripped>
      }
}
```

!!! important 
    Ensure your genesis file includes a sufficient milestone to support the permissioning contracts. 
    For example, `constantinopleFixBlock`. 
    
## Onchain Permissioning Setup
   
1. Start your Pantheon node with contract permissioning options: 

    * [--permissions-nodes-contract-enabled](../Reference/Pantheon-CLI-Syntax.md#permissions-nodes-contract-enabled)
      to enable onhcain permissioning

    * [--permissions-nodes-contract-address](../Reference/Pantheon-CLI-Syntax.md#permissions-nodes-contract-address)
      set to the address of the Ingress contract in the genesis file (`"0x0000000000000000000000000000000000009999"`)

1. Create the following environment variables and set to the specified values: 

    * `PANTHEON_NODE_PERM_ACCOUNT` - address of account used to interact with the permissioning contracts. 

    * `PANTHEON_NODE_PERM_KEY` - private key of the account used to interact with the permissioning contracts.

    * `INGRESS_CONTRACT_ADDRESS` - address of the Ingress contract in the genesis file.  

    * `PANTHEON_NODE_PERM_ENDPOINT` - required only if your node is not using the default JSON-RPC host and port (`http://127.0.0.1:8545`). 
       Set to JSON-RPC host and port. 

    !!! note
        If your network is not configured as a [free gas network](../Configuring-Pantheon/FreeGas.md), the account used to 
        interact with the permissioning contracts must have a balance. 

1. Clone the `permissioning-smart-contracts` repository: 

    ```bash 
    git clone https://github.com/PegaSysEng/permissioning-smart-contracts.git
    ```

1. Change into the `permissioning-smart-contracts` directory and run: 

    ```bash
    npm install
    ```

## Deploy Rules Contract 

In the `permissioning-smart-contracts` directory, deploy the Rules contract: 

```bash
truffle migrate --reset
```

The Rules contract is deployed and the Ingress contract updated with the name and version of the Rules contract. 

!!! important 
    The account that deploys the Rules contract is automatically an [Admin account](#add-and-remove-admin-accounts). 

## Add and Remove Nodes from the Whitelist 

!!! note  
    Only [Admin accounts](#add-and-remove-admin-accounts) can add or remove nodes from the whitelist. 

To add or remove nodes: 

1. In the `permissioning-smart-contracts` directory, run the Truffle console: 

    ```bash
    truffle console 
    ```

1. Open https://permissioning-tools.pegasys.tech/

1. Enter the [enode URL](../Configuring-Pantheon/Node-Keys.md#enode-url) of the node to be added or removed
and click the *Process* button. 

1. Click the *Copy to clipboard* button for _Add Enode_ or _Remove Enode_. 

1. Paste the copied command into the Truffle Console. 

   When the transaction is included in a block, the transaction receipt is displayed. 

!!! important 
    When starting the network, the first node must add itself to the whitelist.  

Added nodes do not attempt to reconnect until peer discovery restarts.  To add a whitelisted node as a peer
without waiting for peer discovery to restart, use [`admin_addPeer`](../Reference/JSON-RPC-API-Methods.md#admin_addpeer). 

## Display Nodes Whitelist 
 
To display the nodes whitelist, paste the following into the Truffle Console: 

```bash
Rules.deployed().then(function(instance) {instance.getKeyCount().then(function(txCount) {console.log("size of whitelist: " + txCount); var i=txCount; while(i>=0) {instance.getWhitelistKey(i--).then(function(tx) {console.log(tx)})}});});
```

## Start Other Network Nodes 

For participating nodes that are not going to add or remove nodes from the whitelist:

1. Start the nodes with permissioning contract options: 

    * [--permissions-nodes-contract-enabled](../Reference/Pantheon-CLI-Syntax.md#permissions-nodes-contract-enabled)
     to enable onhcain permissioning
    
    * [--permissions-nodes-contract-address](../Reference/Pantheon-CLI-Syntax.md#permissions-nodes-contract-address)
     set to the address of the Ingress contract in the genesis file (`"0x0000000000000000000000000000000000009999"`)

1. Copy the [enode URL](../Configuring-Pantheon/Node-Keys.md#enode-url) and [have node added to the whitelist](#add-and-remove-nodes-from-the-whitelist).

For participating nodes that are going to add or remove nodes from the whitelist: 

1. Ensure [prerequisites installed](#pre-requisites).

1. Complete [permissioning setup](#onchain-permissioning-setup).

1. Copy the [enode URL](../Configuring-Pantheon/Node-Keys.md#enode-url) and [have node added to the whitelist](#add-and-remove-nodes-from-the-whitelist).

1. Have account for node that will interact with permissioning contracts added as an [admin account](#add-and-remove-admin-accounts). 

## Add and Remove Admin Accounts      

The account that deploys the Rules contract is automatically an Admin account. Only Admin accounts can
add or remove nodes from the whitelist.

To add Admin accounts, paste the following commands into Truffle Console:

```bash tab="Truffle Console Command"
Rules.deployed().then(function(instance) {instance.addAdmin("<Admin Account>").then(function(tx) {console.log(tx)});});
```

```bash tab="Example"
Rules.deployed().then(function(instance) {instance.addAdmin("0x627306090abaB3A6e1400e9345bC60c78a8BEf57").then(function(tx) {console.log(tx)});});
```

To remove Admin accounts, paste the following commands into Truffle Console:

```bash tab="Truffle Console Command"
Rules.deployed().then(function(instance) {instance.removeAdmin("<Admin Account>").then(function(tx) {console.log(tx)});});
```

```bash tab="Example"
Rules.deployed().then(function(instance) {instance.removeAdmin("0x627306090abaB3A6e1400e9345bC60c78a8BEf57").then(function(tx) {console.log(tx)});});
```

### Display Admin Accounts 

To display the list of admin accounts, paste the following into the Truffle Console: 

```bash
Rules.deployed().then(function(instance) {instance.getAllAdmins().then(function(tx) {console.log(tx)});});
```