description: Onchain Permissioning
<!--- END of page meta data -->

xyz - need bootnodes to catch up? 

# Onchain Permissioning 

Onchain permissioning uses a smart contract to store and maintain the node whitelist. Using onchain permissioning
enables all nodes to read the node whitelist from one location. 
                                                                       
!!! note
    Pantheon supports onchain permissioning for nodes. Onchain permissioning for accounts will be available 
    in a future Pantheon release. 

Onchain permissioning uses the following smart contracts: 

* Ingress - a simple contract functioning as a gateway to the Rules contract. The Ingress contract does 
not require upgrading and can remain at a static address for the life of the contract. 

* Rules - stores the current whitelist and is used to update the whitelist. 

## Pre-requisites 

* [NodeJS](https://nodejs.org/en/) v8.9.4 or later 

* [Truffle](https://truffleframework.com/docs/truffle/getting-started/installation)

## Onchain Permissioning Setup

To use onchain permissioning: 

1. Add the Ingress contract to the genesis file for your network by copying it from [`genesis.json`](https://github.com/PegaSysEng/permissioning-smart-contracts/blob/master/genesis.json) 
in the `permissioning-smart-contracts` repository. 

   Copy from the contract address: 
   
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

1. Create the following environment variables and set to the specified values: 

* `PANTHEON_NODE_PERM_ACCOUNT` - address of account used to interact with the permissioning contracts. 

* `PANTHEON_NODE_PERM_KEY` - private key of the account used to interact with the permissioning contracts. 

* `PANTHEON_NODE_PERM_ENDPOINT` - Only required if your node is not using the default JSON-RPC host and port (`http://127.0.0.1:8545`). 
Set to JSON-RPC host and port. 

!!! note
    If your network is not configured as a [free gas network](../Configuring-Pantheon/FreeGas.md), the account used to 
    interact with the permissioning contracts will require Ether. 

1. Clone the `permissioning-smart-contracts` repository: 

``bash
git clone https://github.com/PegaSysEng/permissioning-smart-contracts.git
``

1. Change into the `permissioning-smarts-contracts` directory and run: 

```bash
npm install
```

## Deploy Rules Contract 

1. Deploy Rules contract to the network: 

```bash
truffle migrate
```

The Rules contract is deployed and the Ingress contract updated with the name and version of the Rules contract. 

!!! important 
    The account that deploys the Rules contract is automatically an Admin account. Admin accounts can add and 
    remove other accounts as admins. 

## Add and Remove Nodes from the Whitelist 

Only Admin accounts can add or remove nodes from the whitelist. 

1. Run the Truffle console: 
```bash
truffle console 
```

1. Open https://permissioning-tools.pegasys.tech/

1. Enter the [enode URL](../Configuring-Pantheon/Node-Keys.md#enode-url) of the node to be added or removed
and click the *Process* button. 

1. Click the *Copy to clipboard* button for _Add Enode_ or _Remove Enode_. 

1. Paste the copied command into the Truffle Console. 

   When the transaction is included in a block, the transaction receipt is displayed. 
   
1. Use [`admin_addPeer`](../Reference/JSON-RPC-API-Methods.md#admin_addpeer) to add the 

## Display Whitelist 
 
To display the whitelist, paste the following into the Truffle Console: 

```bash
Rules.deployed().then(function(instance) {instance.getKeyCount().then(function(txCount) {console.log("size of whitelist: " + txCount); var i=txCount; while(i>=0) {instance.getWhitelistKey(i--).then(function(tx) {console.log(tx)})}});});
```    
     