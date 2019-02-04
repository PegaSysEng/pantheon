# Creating a Private Network using Clique (Proof of Authority) Consensus Protocol

A private network provides a configurable network for testing. 

!!!important
    An Ethereum private network created as described here is isolated but not protected or secure. 
    We recommend running the private network behind a properly configured firewall.

## Prerequisites 

[Pantheon](../Installation/Install-Binaries.md) 

[Curl (or similar web service client)](https://curl.haxx.se/download.html) 

## Steps

To create a private network: 

1. [Create Folders](#1-create-folders)
1. [Get Public Key for Node 1](#2-get-public-key-for-node-1)
1. [Get Address for Node 1](#3-get-address-for-node-1)
1. [Create Genesis File](#4-create-genesis-file)
1. [Start First Node as Bootnode](#5-start-first-node-as-bootnode)
1. [Start Additional Nodes](#6-start-additional-nodes)
1. [Confirm Private Network Working](#6-confirm-private-network-working)

### 1. Create Folders 

Each node requires a data directory for the blockchain data. When the node is started, the node key is saved in this directory. 

Create directories for your private network, each of the three nodes, and a data directory for each node: 

```bash
Clique-Network/
├── Node-1
│   ├── Node-1-data-path
├── Node-2
│   ├── Node-2-data-path
└── Node-3
    ├── Node-3-data-path
```

### 2. Get Public Key for Node 1

To enable nodes to discover each other, a network requires one or more nodes to be bootnodes. 
For this private network, we will use Node 1 as the bootnode. This requires obtaining the public key for the enode URL. 

In the `Node-1` directory, use the [`export-pub-key` subcommand](../Reference/Pantheon-CLI-Syntax.md#export-pub-key) to write 
the [node public key](../Configuring-Pantheon/Node-Keys.md) to the specified file (`publicKeyNode1` in this example):

```bash tab="MacOS"
pantheon --data-path=Node-1-data-path --genesis-file=../privateNetworkGenesis.json public-key export --to=Node-1-data-path/publicKeyNode1
```

```bash tab="Windows"
pantheon --data-path=Node-1-data-path --genesis-file=..\privateNetworkGenesis.json public-key export --to=Node-1-data-path\publicKeyNode1
```

Your node 1 directory now contains: 
```bash
├── Node-1
    ├── Node-1-data-path
        ├── database
        ├── key
        ├── publicKeyNode1
```
      
The `database` directory contains the blockchain data. 

### 3. Get Address for Node 1 

In Clique networks, the address of at least one initial signer must be included in the genesis file. 
For this Clique network, we will use Node 1 as the initial signer. This requires obtaining the address for Node 1. 

To obtain the address for Node 1, do one of the following with the private key in the `key` file in the `Node-1-data-path` directory: 

* Import the private key into [MetaMask](https://metamask.io/) and click the *Copy to clipboard* button 
displayed when hovering over the account name and address. 
* Use the [ethereumjs-util](https://github.com/ethereumjs/ethereumjs-util) library and [node](https://nodejs.org/en/) 
to execute the following where `<private key>` is replaced by the private key for Node 1 without the 0x prefix. 
```js
var ethUtil = require('ethereumjs-util')

// Private key
const privKey = Buffer.from('<private key>', 'hex')

var address = ethUtil.privateToAddress(privKey).toString('hex')

console.log("Address=" + address)
```

### 4. Create Genesis File 

The genesis file defines the genesis block of the blockchain (that is, the start of the blockchain).
The Clique genesis file includes the address of Node 1 as the initial signer in the `extraData` field.    

All nodes in a network must use the same genesis file. 

Copy the following genesis definition to a file called `cliqueGenesis.json` and save it in the `Clique-Network` directory: 

```json
{
  "config":{
    "chainId":1981,
    "clique":{
      "blockperiodseconds":15,
      "epochlength":30000
    }
  },
  "coinbase":"0x0000000000000000000000000000000000000000",
  "difficulty":"0x1",

"extraData":"0x0000000000000000000000000000000000000000000000000000000000000000<Node 1 Address>0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
  "gasLimit":"0xa00000",
  "mixHash":"0x0000000000000000000000000000000000000000000000000000000000000000",
  "nonce":"0x0",
  "timestamp":"0x5c51a607",
  "alloc": {
    "<Node 1 Address>": {
      "privateKey": "<Node 1 Private Key>",
      "balance": "90000000000000000000000"
    }
  },
  "number":"0x0",
  "gasUsed":"0x0",
  "parentHash":"0x0000000000000000000000000000000000000000000000000000000000000000"
}
```

Replace:
 
 * `<Node 1 Address>` with the [address for Node 1](#3-get-address-for-node-1)
 * `<Node 1 Private Key>` with private key in the `key` file in the `Node-1-data-path` directory. 

!!! example "Clique Genesis File"
    
    ```json
    {
      "config":{
        "chainId":1981,
        "clique":{
          "blockperiodseconds":15,
          "epochlength":30000
        }
      },
      "coinbase":"0x0000000000000000000000000000000000000000",
      "difficulty":"0x1",
    
    "extraData":"0x0000000000000000000000000000000000000000000000000000000000000000b9b81ee349c3807e46bc71aa2632203c5b4620340000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      "gasLimit":"0xa00000",
      "mixHash":"0x0000000000000000000000000000000000000000000000000000000000000000",
      "nonce":"0x0",
      "timestamp":"0x5c51a607",
      "alloc": {
        "b9b81ee349c3807e46bc71aa2632203c5b462034": {
          "privateKey": "4ff55c423e4136c1a118721aaaadf0271f823c42b7d1438c6706efe0c001f7fe",
          "balance": "90000000000000000000000"
        }
      },
      "number":"0x0",
      "gasUsed":"0x0",
      "parentHash":"0x0000000000000000000000000000000000000000000000000000000000000000"
    }
    ```

!!! warning
    Do not use the accounts in the genesis file above on mainnet or any public network except for testing.
    The private keys are displayed so the accounts are not secure. 
   


### 5. Start First Node as Bootnode <UP TO HERE>

Start Node-1 specifying:

* No arguments for the [`--bootnodes` option](../Reference/Pantheon-CLI-Syntax.md#bootnodes) because this is your bootnode.
* Mining is enabled and the account to which mining rewards are paid using the [`--miner-enabled`](../Reference/Pantheon-CLI-Syntax.md#miner-enabled) 
and [`--miner-coinbase` options](../Reference/Pantheon-CLI-Syntax.md#miner-coinbase).
* JSON-RPC API is enabled using the [`--rpc-http-enabled` option](../Reference/Pantheon-CLI-Syntax.md#rpc-http-enabled)
* All hosts can access the HTTP JSON-RPC API using the [`--host-whitelist` option](../Reference/Pantheon-CLI-Syntax.md#host-whitelist). 

```bash tab="MacOS"
pantheon --data-path=Node-1-data-path --genesis-file=../privateNetworkGenesis.json --bootnodes --network-id 123 --miner-enabled --miner-coinbase fe3b557e8fb62b89f4916b721be55ceb828dbd73 --rpc-http-enabled --host-whitelist=*      
```

```bash tab="Windows"
pantheon --data-path=Node-1-data-path --genesis-file=..\privateNetworkGenesis.json --bootnodes --network-id 123 --miner-enabled --miner-coinbase fe3b557e8fb62b89f4916b721be55ceb828dbd73 --rpc-http-enabled --host-whitelist=*     
```

!!! info
    The miner coinbase account is one of the accounts defined in the genesis file. 

### 5. Start Additional Nodes 

You need the enode URL for Node-1 to specify Node-1 as the bootnode for Node-2 and Node-3. 

The enode URL is `enode://<id>@<host:port>` where:

* `<id>` is the node public key excluding the initial 0x. The node public key for Node-1 was written to `publicKeyNode1` in [step 3](#3-start-first-node-and-get-node-public-key). 
* `<host:port>` is the host and port the bootnode is listening on for P2P peer discovery. Node-1 is using the default host and port of `127.0.0.1:30303`. 

!!! example
    If the default host and port are used for P2P peer discovery and the node public key exported is: `0xc35c3ec90a8a51fd5703594c6303382f3ae6b2ecb9589bab2c04b3794f2bc3fc2631dabb0c08af795787a6c004d8f532230ae6e9925cbbefb0b28b79295d615f`
    
    The enode URL is:
    `enode://c35c3ec90a8a51fd5703594c6303382f3ae6b2ecb9589bab2c04b3794f2bc3fc2631dabb0c08af795787a6c004d8f532230ae6e9925cbbefb0b28b79295d615f@127.0.0.1:30303` 

Start another terminal, change to the `Node-2` directory and start Node-2 specifying:
 
* Different port to Node-1 for P2P peer discovery using the [`--p2p-port` option](../Reference/Pantheon-CLI-Syntax.md#p2p-port).
* Enode URL for Node-1 using the [`--bootnodes` option](../Reference/Pantheon-CLI-Syntax.md#bootnodes).
* Data directory for Node-2 using the [`--data-path` option](../Reference/Pantheon-CLI-Syntax.md#data-path).
* Genesis file and network ID as for Node-1.  

```bash tab="MacOS"
pantheon --data-path=Node-2-data-path --genesis-file=../privateNetworkGenesis.json --bootnodes="enode://<node public key ex 0x>@127.0.0.1:30303" --network-id 123 --p2p-port=30304      
```

```bash tab="Windows"
pantheon --data-path=Node-2-data-path --genesis-file=..\privateNetworkGenesis.json --bootnodes="enode://<node public key ex 0x>@127.0.0.1:30303" --network-id 123 --p2p-port=30304      
```

Start another terminal, change to the `Node-3` directory and start Node-3 specifying: 
 
 * Different port to Node-1 and Node-2 for P2P peer discovery.
 * Data directory for Node-3 using the [`--data-path` option](../Reference/Pantheon-CLI-Syntax.md#data-path).
 * Bootnode, genesis file, and network ID as for Node-2. 

```bash tab="MacOS"
pantheon --data-path=Node-3-data-path --genesis-file=../privateNetworkGenesis.json --bootnodes="enode://<node public key ex 0x>@127.0.0.1:30303" --network-id 123 --p2p-port30305      
```

```bash tab="Windows"
pantheon --data-path=Node-3-data-path --genesis-file=..\privateNetworkGenesis.json --bootnodes="enode://<node public key ex 0x>@127.0.0.1:30303" --network-id 123 --p2p-port=30305      
```

### 6. Confirm Private Network is Working 

Start another terminal, use curl to call the JSON-RPC API [`net_peerCount`](../Reference/JSON-RPC-API-Methods.md#net_peercount) method and confirm the nodes are functioning as peers: 

```bash
curl -X POST --data '{"jsonrpc":"2.0","method":"net_peerCount","params":[],"id":1}' 127.0.0.1:8545
```

The result confirms Node-1 (the node running the JSON-RPC service) has two peers (Node-2 and Node-3):
```json
{
  "jsonrpc" : "2.0",
  "id" : 1,
  "result" : "0x2"
}
```

## Next Steps 

Import accounts to MetaMask and send transactions as described in the [Private Network Quickstart Tutorial](Private-Network-Quickstart.md#creating-a-transaction-using-metamask)

!!! info 
    Pantheon does not implement [private key management](../Using-Pantheon/Account-Management.md).
    
Send transactions using `eth_sendRawTransaction` to [send ether or, deploy or invoke contracts](../Using-Pantheon/Transactions.md).

Use the [JSON-RPC API](../Reference/Using-JSON-RPC-API.md). 

Start a node with the `--rpc-ws-enabled` option and use the [RPC Pub/Sub API](../Using-Pantheon/RPC-PubSub.md).       

## Stop Nodes

When finished using the private network, stop all nodes using ++ctrl+c++ in each terminal window. 

!!!tip
    To restart the private network in the future, start from [4. Restart First Node as Bootnode](#4-restart-first-node-as-bootnode). 