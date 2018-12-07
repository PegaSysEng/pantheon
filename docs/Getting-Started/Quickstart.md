# Pantheon Quickstart

This tutorial shows you how to install Pantheon and invoke the Pantheon CLI to run a single node in default mode:

* The node runs on the Ethereum mainnet and uses the default embedded mainnet genesis file.
* The node does not perform mining.
* P2P peer discovery is enabled on the default P2P port. There are a maximum of 25 P2P peer connections.
* Listen on the default RPC over HTTP host/port.
* Use full synchronization mode.

During the tutorial, you'll import a test block file to provide the initial blocks for the node to use, then run the node. You'll then use `curl` to make a call using Pantheon JSON-RPC API; this will make a request to verify that the node is running.


## Installation

Install Java, Git, and Pantheon for your platform as described in [Installation](Installation), then proceed through the following steps.


## Run Pantheon

In this tutorial, you'll build and run Pantheon in one terminal (bash) window and run `curl` commands to make JSON-RPC requests in another terminal window. To do this, either open two separate instances of `Terminal`, or run a utility such as [tmux](https://github.com/tmux/tmux/wiki) to run a split screen terminal.

In the first terminal, go to the `~/pantheon` folder:
```bash
cd pantheon
```

Build Pantheon as described in [Installation](Installation).

To run `pantheon`, go to the distribution folder:
```bash
cd build/distributions/
```

Expand the distribution archive:
```bash
tar -xzf pantheon-<version>.tar.gz
```

Move to the expanded folder and run `pantheon` using the script to display command help:
```bash
cd pantheon-<version>/
bin/pantheon --help
```

Import initial blocks for the node to work with from a blocks file. You can use the provided sample blocks file as shown in the following command:
```bash
bin/pantheon import ~/pantheon/testutil/src/main/resources/1000.blocks
```

When the import completes successfully, you should see messages containing the following:
```bash
ImportSubCommand | Runs import sub command with blocksImportPath

KeyPairUtil | Generated new key <key> and stored it to ~/pantheon/build/distributions/pantheon-<version>/key
```
For more information on using the `pantheon import` subcommand, see [Pantheon CLI Syntax](Pantheon-CLI-Syntax).

After the import successfully completes, run Pantheon with only the `--rpc-enabled` option:
```
bin/pantheon --rpc-enabled
```
This runs Pantheon in default mode, meaning it runs in mainnet with the default genesis file, and peer discovery is enabled.

The `--rpc-enabled` option starts the JSON-RPC service.  

The terminal output will indicate that the node is synchronizing.

## Test the Pantheon Node

#### Make JSON-RPC Requests using curl

While the node is running, make a request on it using curl and the JSON-RPC API.

Switch to the bottom window using `Ctrl+b o` and run the following `curl` command:
```
curl -X POST --data '{"jsonrpc":"2.0","method":"eth_syncing","params":[],"id":1}' 127.0.0.1:8545
```

Note the endpoint IP and default port. You should see a result similar to the following:
```
{
  "jsonrpc" : "2.0",
  "id" : 1,
  "result" : {
    "startingBlock" : "0x3e7",
    "currentBlock" : "0x3e7",
    "highestBlock" : "0x0"
  }
}
```

You can now call other [Pantheon JSON-RPC commands](JSON-RPC-API-Methods).

## Close the Terminal Session

After you're finished running the demo, you can stop the Pantheon client by returning to the terminal window that runs the node and entering `Ctrl+c`.
