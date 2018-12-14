description: Code review guidelines for Pantheon developers
<!--- END of page meta data -->

This reference describes the syntax of the Pantheon Command Line Interface (CLI) options and subcommands.

```bash
pantheon [OPTIONS] [COMMAND]
```

Runs the Pantheon Ethereum full node client.

## Options

### banned-nodeids

```bash tab="Syntax"
--banned-nodeids=<bannedNodeId>[,<bannedNodeId>...]...
```

```bash tab="Example"
--banned-nodeids=enode://c35c3...d615f@1.2.3.4:30303,enode://f42c13...fc456@1.2.3.5:30303
```

List of node IDs with which this node will not peer. The node ID is the public key of the node. You can specify the banned node IDs with or without the `0x` prefix.
  
!!!info
    This option is only available from v0.8.2 or when [building from source](Installation). 
 
### bootnodes
  <dt><code>--bootnodes=&lt;enode://id@host:port&gt;[,&lt;enode://id@host:port&gt;...]...</code></dt>
  
  <dd>List of comma-separated enode URLs for P2P discovery bootstrap. </dd>
  
  <dd>When connecting to mainnet and Rinkeby, the default is a predefined list of enode URLs. Specify bootnodes when <a href="https://github.com/PegaSysEng/pantheon/wiki/Starting-Pantheon#run-a-node-on-ropsten-testnet">connecting to Ropsten</a> or a <a href="https://github.com/PegaSysEng/pantheon/wiki/Testing-Developing-Nodes#bootnodes">private network.</a></dd>
<br>
  <dt><code>--config=&lt;PATH&gt;</code></dt>
  <dd>The path to the TOML configuration file. The default is <code>none</code>. The TOML file is composed of key/value pairs. Each key is the same as the corresponding CLI option name without the leading dashes (<code>--</code>). The config option is not used in the config file. Values must be treated according to TOML specifications for string, numbers, arrays and Booleans.

Example: 
```
# Valid TOML config file
datadir="~/pantheondata" # Path

# Network
bootnodes=["enode://001@123:4567", "enode://002@123:4567", "enode://003@123:4567"]
p2p-listen="1.2.3.4:1234" # IP:port
max-peers=42
rpc-listen="5.6.7.8:5678" # IP:port
ws-listen="9.10.11.12:9101" # IP:port

# Chain
genesis="~/genesis.json" # Path to the custom genesis file

# Mining
miner-enabled=true
miner-coinbase="0x0000000000000000000000000000000000000002"
```
</dd>
<br>

> **Note:** This option is not used when running Pantheon from the [Docker image](Run-Docker-Image#custom-configuration-file). 
  <dt><code>--datadir=&lt;PATH></code></dt>
  <dd>The path to the Pantheon data directory. The default location is the <code>/build/distributions/pantheon-<version></code> directory in the Pantheon installation directory.</dd>

> **Note:** This option is not used when running Pantheon from the [Docker image](Run-Docker-Image#persisting-data). 
<br>

### dev-mode
  <dt><code>--dev-mode</code></dt>
  <dd>
  Set this option to <code>true</code> to run in development mode. For example, specify this option to perform CPU mining more easily in a private test network. In development mode, a custom genesis configuration specifies the chain ID. When using this option, also set the <code>--network-id</code> option to the network you use for development. Default is <code>false</code>.
  </dd>
  
> **Note:** The `--dev-mode` option overrides the `--genesis` option. If both are specified, the development mode configuration is used.  
<br>
  <dt><code>--genesis=&lt;PATH></code></dt>
  <dd>The path to the genesis file. The default is the embedded genesis file for the Ethereum mainnet. When using this option, it is recommended to also set the <code>--network-id</code> option.</dd>

> **Note:** This option is not used when running Pantheon from the [Docker image](Run-Docker-Image#custom-genesis-file). 

> **Note:** The `--genesis` option is overridden by the `--dev-mode` option. If both are specified, the specified genesis file is ignored and the development mode configuration used. 
<br>


 <dt><code>--host-whitelist=&lt;hostname></code></dt>

<dd>Comma-separated list of hostnames to allow access to the HTTP JSON-RPC API. Default is <code>localhost</code>. </dd>

To allow all hostnames, use `*` or `all`. We don't recommend this for production code.

> **Note:** This option is only available from v0.8.3 or when [building from source](Installation). Earlier versions allow access by all hostnames. 

  <dt><code>--max-peers=&lt;INTEGER></code></dt>
  <dd>Specifies the maximum P2P peer connections that can be established. The default is 25.</dd>
<br>
  <dt><code>--max-trailing-peers=&lt;INTEGER></code></dt>
  <dd>Specifies the maximum P2P peer connections for peers that are trailing behind the local chain head. The default is unlimited but the number of trailing peers cannot exceed the value specified by <code>--max-peers</code>.</dd>
<br>
  <dt><code>--miner-coinbase=&lt;Ethereum account address&gt;</code></dt>
  <dd>Account to which mining rewards are paid. You must specify a valid coinbase when you enable mining using the <code>--miner-enabled</code> option or the <code>miner_start</code> JSON RPC-API method.</dd>

> **Note:** This option is ignored in networks using the [Clique Proof-of-Authority (PoA) consensus protocol](https://github.com/PegaSysEng/pantheon/wiki/Proof-of-Authority). 
<br>

  <dt><code>--miner-enabled</code></dt>
  <dd>Enables mining when the node is starts. Default is <code>false</code>.
  
  </dd>
<br>
  <dt><code>--miner-extraData=&lt;Extra data&gt;</code></dt>
  <dd>A hex string representing the 32 bytes to be included in the extra data field of a mined block. The default is 0x.</dd>
<br>
  <dt><code>--miner-minTransactionGasPriceWei=&lt;minTransactionGasPrice&gt;</code></dt>
  <dd>The minimum price that a transaction offers for it to be included in a mined block The default is 1000.</dd>

<br>
  <dt><code>--network-id=&lt;INTEGER></code></dt>
  <dd>P2P network identifier. The default is set to mainnet with value <code>1</code>.</dd>

<br>
  <dt><code>--no-discovery</code></dt>
  <dd>Disables P2P peer discovery. The default is <code>false</code>.</dd>

<br>
  <dt><code>--node-private-key=&lt;PATH&gt;</code></dt>
  <dd><code>&lt;PATH&gt;</code> is the path of the private key file of the node. The default is the key file in the data directory. If no key file exists, a key file containing the generated private key is created; otherwise, the existing key file specifies the node private key.
</dd>

 > **Note** The private key is not encrypted.
  
 >**Note** This option is only available from v0.8.2 or when [building from source](Installation). 

<br>
  <dt><code>--nodes-whitelist[=&lt;enode://id@host:port>[,&lt;enode://id@host:port>...]...]</code></dt>
  <dd>Comma-separated enode URLs for permissioned networks. If you specify an empty list or <code>--nodes-whitelist</code> without a list, your node will not connect to any other nodes.
</dd>

 >**Note** This option is only available from v0.8.3 or when [building from source](Installation). 

> **Note:** Permissioning is not currently supported. Support for permissioning is in active development.

<br>
  <dt><code>--ottoman</code></dt>
  <dd>Synchronize against the Ottoman test network. This is only useful if you are using an IBFT genesis file.  The default is <code>false</code>.</dd>

 > **Note:** IBFT is not currently supported. Support for IBFT is in active development. 
<br>
  <dt><code>--p2p-listen=&lt;HOST:PORT></code></dt>
  <dd>Specifies the host and port on which P2P peer discovery listens. The default is 127.0.0.1:30303.</dd>

> **Note** This option is not used when running Pantheon from the [Docker image](Run-Docker-Image#exposing-ports). 
<br>
  <dt><code>--rinkeby</code></dt>
  <dd>Uses the Rinkeby test network. Default is <code>false</code>.</dd>
  
  <br>
  <br>
  <dt><code>--ropsten</code></dt>
  <dd>Uses the Ropsten test network. Default is <code>false</code>.</dd>

> **Note** This option is only available only from v0.8.2 or when [building from source](Installation). For v0.8.1, refer [here](Starting-Pantheon#run-a-node-on-ropsten-testnet). 
<br>
  <dt><code>--rpc-enabled</code></dt>
  <dd>Set to <code>true</code> to enable the JSON-RPC service (RPC over HTTP). The default is <code>false</code>.</dd>
<br>
  <dt><code>--rpc-listen=&lt;HOST:PORT></code></dt>
  <dd>Specifies the host and port on which JSON-RPC listens. The default is 127.0.0.1:8545.</dd>

> **Note:** This option is not used when running Pantheon from the [Docker image](Run-Docker-Image#exposing-ports). 
<br>
  <dt><code>--rpc-api=&lt;api name>[,<api name>...]...</code></dt>
  <dd>Comma-separated APIs to enable on the JSON-RPC channel. When you use this option, the <code>--rpc-enabled</code> option must also be specified. The available API options are: <code>ETH</code>, <code>NET</code>, <code>WEB3</code>, <code>CLIQUE</code>, <code>IBFT</code>, <code>DEBUG</code>, and <code>MINER</code>. The default is: <code>ETH</code>, <code>NET</code>, <code>WEB3</code>, <code>CLIQUE</code>, <code>IBFT</code>.</dd>

> **Note:** IBFT is not currently supported. Support for IBFT is in active development. 
<br>
  <dt><code>--rpc-cors-origins=&lt;rpcCorsAllowedOrigins></code></dt>
  <dd>
Specifies domain URLs for CORS validation. Domain URLs must be enclosed in double quotes and comma-separated.

Listed domains will be allowed access to node data (whitelisted). If your client interacts with Pantheon using a browser app (such as Remix using a direct connection or a block explorer), you must whitelist the client domains. For example, the following allows Remix to interact with your Pantheon node without using MetaMask:

`--rpc-cors-origins "http://remix.ethereum.org"`

**Note:** MetaMask runs as native code so does not require CORS validation. If Remix is connecting to the node through MetaMask, it also does not require CORS validation. 

The default value is `"none"`. If you don't whitelist any domains, you won't be able to use webapps to interact with your Pantheon node.

You can whitelist one or more domains with a comma-separated list. For example:

`--rpc-cors-origins "http://foo.com, https://bar.io"`

For development purposes, you can use `"all"` to accept requests from any domain, but we don't recommend this for production code.
</dd>


<br>
  <dt><code>--ws-enabled</code></dt>
  <dd>Set to <code>true</code> to enable the WS-RPC (WebSockets) service. The default is <code>false</code>.</dd>
<br>
  <dt><code>--ws-api=&lt;api name>[,<api name>...]...
</code></dt>
  <dd>Comma-separated APIs to enable on Websockets channel. When you use this option, the <code>--ws-enabled</code> option must also be specified. The available API options are: <code>ETH</code>, <code>NET</code>, <code>WEB3</code>, <code>CLIQUE</code>, <code>IBFT</code>, <code>DEBUG</code>, and <code>MINER</code>. The default is: <code>ETH</code>, <code>NET</code>, <code>WEB3</code>, <code>CLIQUE</code>, <code>IBFT</code>.</dd>

> **Note:** IBFT is not currently supported. Support for IBFT is in active development. 
<br>
  <dt><code> --ws-listen=&lt;HOST:PORT></code></dt>
  <dd>Host and port for WS-RPC (Websocket) to listen on. The default is 127.0.0.1:8546.</dd>

> **Note:** This option is not used when running Pantheon from the [Docker image](Run-Docker-Image#exposing-ports). 
<br>
  <dt><code>-h, --help</code></dt>
  <dd>Show the help message and exit.</dd>
<br>
<dt><code>-l, --logging=&lt;LEVEL></code></dt>
  <dd>Sets the logging verbosity. Log levels are <code>OFF</code>, <code>FATAL</code>, <code>WARN</code>, <code>INFO</code>, <code>DEBUG</code>, <code>TRACE</code>, <code>ALL</code>. Default is <code>INFO</code>.</dd>
<br>
  <dt><code>-V, --version</code></dt>
  <dd>Print version information and exit.</dd>
</dl>

***
## Commands

Pantheon subcommands are: 

* `import`

   Imports blocks from the specified file into the blockchain database:

   `pantheon import <block-file>`
   
* `export-pub-key`

    Exports node public key to the specified file. 
    
    `pantheon export-pub-key <key-file>`

<br>

### 

***
Pantheon is licensed under Apache License 2.0.
