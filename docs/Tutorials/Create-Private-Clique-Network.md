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
1. [Get Public Key for Node-1](#2-get-public-key-for-node-1)
1. [Get Address for Node-1](#3-get-address-for-node-1)
1. [Create Genesis File](#4-create-genesis-file)
1. [Delete Database Directory](#5-delete-database-directory)
1. [Start First Node as Bootnode](#6-start-first-node-as-bootnode)
1. [Start Node-2](#7-start-node-2)
1. [Start Node-3](#8-start-node-3)
1. [Confirm Private Network Working](#9-confirm-private-network-is-working)

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

### 2. Get Public Key for Node-1

To enable nodes to discover each other, a network requires one or more nodes to be bootnodes. 
For this private network, we will use Node-1 as the bootnode. This requires obtaining the public key for the enode URL. 

In the `Node-1` directory, use the [`export-pub-key`](../Reference/Pantheon-CLI-Syntax.md#export-pub-key) subcommand to write 
the [node public key](../Configuring-Pantheon/Node-Keys.md) to the specified file (`publicKeyNode1` in this example):

```bash tab="MacOS"
pantheon --data-path=Node-1-data-path public-key export --to=Node-1-data-path/publicKeyNode1
```

```bash tab="Windows"
pantheon --data-path=Node-1-data-path public-key export --to=Node-1-data-path\publicKeyNode1
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

### 3. Get Address for Node-1 

In Clique networks, the address of at least one initial signer must be included in the genesis file. 
For this Clique network, we will use Node-1 as the initial signer. This requires obtaining the address for Node-1. 

To obtain the address for Node-1, do one of the following with the private key in the `key` file in the `Node-1-data-path` directory: 

* Import the private key into [MetaMask](https://metamask.io/) and click the **Copy to clipboard** button 
displayed when hovering over the account name and address. 
* Use the [ethereumjs-util](https://github.com/ethereumjs/ethereumjs-util) library and [node](https://nodejs.org/en/) 
to execute the following where `<private key>` is replaced by the private key for Node-1 without the 0x prefix. 
```js
var ethUtil = require('ethereumjs-util')
const privKey = Buffer.from('<private key>', 'hex')
var address = ethUtil.privateToAddress(privKey).toString('hex')
console.log("Address=" + address)
```

### 4. Create Genesis File 

The genesis file defines the genesis block of the blockchain (that is, the start of the blockchain).
The [Clique genesis file](../Consensus-Protocols/Clique.md#genesis-file) includes the address of Node-1 as the initial signer in the `extraData` field.    

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
 
 * `<Node 1 Address>` with the [address for Node-1](#3-get-address-for-node-1) in `extraData` and `alloc`. 
 * `<Node 1 Private Key>` with the private key in the `key` file in the `Node-1-data-path` directory excluding the 0x prefix. 

!!! example
    
    ```json
    {
      "config":{
        "chainId":123,
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

### 5. Delete Database Directory

Delete the `database` directory created when [getting the public key for Node-1](#2-get-public-key-for-node-1).
The node cannot be started with the Clique genesis file while the previously generated data is in the `database` directory. 

### 6. Start First Node as Bootnode

Start Node-1 specifying:

* No arguments for the [`--bootnodes`](../Reference/Pantheon-CLI-Syntax.md#bootnodes) option because this is your bootnode
* JSON-RPC API is enabled using the [`--rpc-http-enabled`](../Reference/Pantheon-CLI-Syntax.md#rpc-http-enabled) option
* ETH,NET, and CLIQUE APIs are enabled using the [`--rpc-http-api`](../Reference/Pantheon-CLI-Syntax.md#rpc-http-api) option
* All hosts can access the HTTP JSON-RPC API using the [`--host-whitelist`](../Reference/Pantheon-CLI-Syntax.md#host-whitelist) option
* All domains can access the node using the HTTP JSON-RPC API using the [`--rpc-http-cors-origins`](../Reference/Pantheon-CLI-Syntax.md#rpc-http-cors-origins) option 

```bash tab="MacOS"
pantheon --data-path=Node-1-data-path --genesis-file=../cliqueGenesis.json --bootnodes --network-id 123 --rpc-http-enabled --rpc-http-api=ETH,NET,CLIQUE --host-whitelist=* --rpc-http-cors-origins="all"      
```

```bash tab="Windows"
pantheon --data-path=Node-1-data-path --genesis-file=..\cliqueGenesis.json --bootnodes --network-id 123 --rpc-http-enabled --rpc-http-api=ETH,NET,CLIQUE --host-whitelist=* --rpc-http-cors-origins="all"    
```

### 6. Start Node-2 

You need the enode URL for Node-1 to specify Node-1 as a bootnode. 

The enode URL is `enode://<id>@<host:port>` where:

* `<id>` is the node public key excluding the initial 0x. The node public key for Node-1 was written to `publicKeyNode1` in [step 2](#2-get-public-key-for-node-1). 
* `<host:port>` is the host and port the bootnode is listening on for P2P peer discovery. Node-1 is using the default host and port of `127.0.0.1:30303`. 

!!! example
    If the default host and port are used for P2P peer discovery and the node public key exported is: `0xc35c3ec90a8a51fd5703594c6303382f3ae6b2ecb9589bab2c04b3794f2bc3fc2631dabb0c08af795787a6c004d8f532230ae6e9925cbbefb0b28b79295d615f`
    
    The enode URL is:
    `enode://c35c3ec90a8a51fd5703594c6303382f3ae6b2ecb9589bab2c04b3794f2bc3fc2631dabb0c08af795787a6c004d8f532230ae6e9925cbbefb0b28b79295d615f@127.0.0.1:30303` 

Start another terminal, change to the `Node-2` directory and start Node-2 specifying:
 
* Different port to Node-1 for P2P peer discovery using the [`--p2p-port`](../Reference/Pantheon-CLI-Syntax.md#p2p-port) option.
* Different port to Node-1 for HTTP JSON-RPC using the [`--rpc-http-port`](../Reference/Pantheon-CLI-Syntax.md#rpc-http-port) option.
* Enode URL for Node-1 using the [`--bootnodes`](../Reference/Pantheon-CLI-Syntax.md#bootnodes) option.
* Data directory for Node-2 using the [`--data-path`](../Reference/Pantheon-CLI-Syntax.md#data-path) option.
* Other options as for [Node-1](#5-start-first-node-as-bootnode).  

```bash tab="MacOS"
pantheon --data-path=Node-2-data-path --genesis-file=../cliqueGenesis.json --bootnodes="enode://<node public key ex 0x>@127.0.0.1:30303" --network-id 123 --p2p-port=30304 --rpc-http-enabled --rpc-http-api=ETH,NET,CLIQUE --host-whitelist=* --rpc-http-cors-origins="all" --rpc-http-port=8546     
```

```bash tab="Windows"
pantheon --data-path=Node-2-data-path --genesis-file=..\cliqueGenesis.json --bootnodes="enode://<node public key ex 0x>@127.0.0.1:30303" --network-id 123 --p2p-port=30304 --rpc-http-enabled --rpc-http-api=ETH,NET,CLIQUE --host-whitelist=* --rpc-http-cors-origins="all" --rpc-http-port=8546     
```

### 7. Start Node-3

Start another terminal, change to the `Node-3` directory and start Node-3 specifying: 
 
 * Different port to Node-1 and Node-2 for P2P peer discovery using the [`--p2p-port`](../Reference/Pantheon-CLI-Syntax.md#p2p-port) option.
 * Different port to Node-1 and Node-2 for HTTP JSON-RPC using the [`--rpc-http-port`](../Reference/Pantheon-CLI-Syntax.md#rpc-http-port) option.
 * Data directory for Node-3 using the [`--data-path`](../Reference/Pantheon-CLI-Syntax.md#data-path) option.
 * Bootnode as for [Node-2](#6-start-node-2).
 * Other options as for [Node-1](#5-start-first-node-as-bootnode). 

```bash tab="MacOS"
pantheon --data-path=Node-3-data-path --genesis-file=../cliqueGenesis.json --bootnodes="enode://<node public key ex 0x>@127.0.0.1:30303" --network-id 123 --p2p-port=30305 --rpc-http-enabled --rpc-http-api=ETH,NET,CLIQUE --host-whitelist=* --rpc-http-cors-origins="all" --rpc-http-port=8547    
```

```bash tab="Windows"
pantheon --data-path=Node-3-data-path --genesis-file=..\cliqueGenesis.json --bootnodes="enode://<node public key ex 0x>@127.0.0.1:30303" --network-id 123 --p2p-port=30305 --rpc-http-enabled --rpc-http-api=ETH,NET,CLIQUE --host-whitelist=* --rpc-http-cors-origins="all" --rpc-http-port=8547    
```

### 8. Confirm Private Network is Working 

Start another terminal, use curl to call the JSON-RPC API [`net_peerCount`](../Reference/JSON-RPC-API-Methods.md#net_peercount) method and confirm the nodes are functioning as peers: 

```bash
curl -X POST --data '{"jsonrpc":"2.0","method":"net_peerCount","params":[],"id":1}' 127.0.0.1:8545
```

The result confirms Node-1 has two peers (Node-2 and Node-3):
```json
{
  "jsonrpc" : "2.0",
  "id" : 1,
  "result" : "0x2"
}
```

## Next Steps 

Look at the logs displayed to confirm Node-1 is producing blocks and Node-2 and Node-3 are importing blocks. 

Use the [Clique API to add](../Consensus-Protocols/Clique.md#adding-and-removing-signers) Node-2 or Node-3 as a signer. 

!!! note
    To add Node-2 or Node-3 as a signer you need the [node address as when specifying Node-1](#2-get-address-for-node-1) as the initial signer. 

## Stop Nodes

When finished using the private network, stop all nodes using ++ctrl+c++ in each terminal window. 

!!!tip
    To restart the Clique network in the future, start from [5. Start First Node as Bootnode](#5-start-first-node-as-bootnode). 