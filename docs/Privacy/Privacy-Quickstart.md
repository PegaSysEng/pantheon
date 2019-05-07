description: Pantheon private network quickstart tutorial
<!--- END of page meta data -->

# Private Network with Privacy Enabled Quickstart Tutorial

The Private Network with Privacy Enabled Quickstart runs a private network of Pantheon and Orion nodes managed by Docker Compose.
It is an expanded version of the [Private Network Quickstart](../Tutorials/Private-Network-Quickstart.md). 

You can use the [Block Explorer](../Tutorials/Private-Network-Quickstart.md#block-explorer), 
make [JSON-RPC requests](../Tutorials/Private-Network-Quickstart.md#run-json-rpc-requests), and 
create [transactions using Metamask](../Tutorials/Private-Network-Quickstart.md#creating-a-transaction-using-metamask)
as described in the [Private Network Quickstart tutorial](../Tutorials/Private-Network-Quickstart.md). 
This tutorial describes how to use the examples provided in the EEAJS library to [create and send private transactions](#send-private-transactions-and-read-values). 

!!! important 
    The quickstart runs a private network suitable for education or demonstration purposes. 
    The quickstart is not intended for running production networks. 

## Prerequisites

To run this tutorial, you must have the following installed:

- MacOS or Linux 
    
    !!! important 
        The Private Network Quickstart is not supported on Windows. If using Windows, run the quickstart
        inside a Linux VM such as Ubuntu. 

- [Docker and Docker-compose](https://docs.docker.com/compose/install/) 

- [Nodejs](https://nodejs.org/en/download/)

- [Git command line](https://git-scm.com/)

- [Curl command line](https://curl.haxx.se/download.html) 

## Clone Pantheon Quickstart Source Code

Clone the repository from the `pantheon-quickstart` repository where `<version>` is replaced with the latest version (for example, `1.1.0`):

```bash tab="Command"
git clone --branch <version> https://github.com/PegaSysEng/pantheon-quickstart.git
```

```bash tab="Example"
git clone --branch 1.1.0 https://github.com/PegaSysEng/pantheon-quickstart.git
```

## Clone EEAJS Libraries 

Clone the `PegaSysEng/eeajs` library: 

```bash
git clone https://github.com/PegaSysEng/eeajs.git
```

In the `eeajs` directory: 
   
```bash
npm install
```

## Start the Private Network with Privacy Enabled 

Change to the `pantheon-quickstart/privacy` directory: 

```bash
cd pantheon-quickstart/privacy 
```

Start the network: 

```bash
./run.sh
```

The Docker images are pulled and network started.  Pulling the images takes a few minutes the first time.
The Pantheon and Orion nodes started are displayed. 

```bash
       Name                      Command               State                              Ports                           
--------------------------------------------------------------------------------------------------------------------------
privacy_bootnode_1    /opt/pantheon/bootnode_sta ...   Up      30303/tcp, 8545/tcp, 8546/tcp                              
privacy_explorer_1    nginx -g daemon off;             Up      0.0.0.0:32771->80/tcp                                      
privacy_minernode_1   /opt/pantheon/node_start.s ...   Up      30303/tcp, 8545/tcp, 8546/tcp                              
privacy_node1_1       /opt/pantheon/node_start.s ...   Up      30303/tcp, 0.0.0.0:20000->8545/tcp, 0.0.0.0:20001->8546/tcp
privacy_node2_1       /opt/pantheon/node_start.s ...   Up      30303/tcp, 0.0.0.0:20002->8545/tcp, 0.0.0.0:20003->8546/tcp
privacy_node3_1       /opt/pantheon/node_start.s ...   Up      30303/tcp, 0.0.0.0:20004->8545/tcp, 0.0.0.0:20005->8546/tcp
privacy_orion1_1      /orion/bin/orion data/data ...   Up                                                                 
privacy_orion2_1      /orion/bin/orion data/data ...   Up                                                                 
privacy_orion3_1      /orion/bin/orion data/data ...   Up                                                                 
privacy_rpcnode_1     /opt/pantheon/node_start.s ...   Up      30303/tcp, 8545/tcp, 8546/tcp                              
****************************************************************
JSON-RPC HTTP service endpoint      : http://localhost:32771/jsonrpc   *
JSON-RPC WebSocket service endpoint : ws://localhost:32771/jsonws   *
Web block explorer address          : http://localhost:32771   *                                                                             
****************************************************************
```

## Send Private Transactions and Read Values 

The Event Emitter script deploys a contract with a privacy group of Node1 and Node2. That is, Node3
cannot access the contract. After deploying the contract, Event Emitter stores a value. 

Run `eventEmitter.js`: 

```bash
node example/eventEmitter.js
```

!!! tip 
    The network takes a minute or so to get started.  If you get a ` Error: socket hang up` error, the network
    isn't fully setup. Wait and then run the command again. 

The Event Emitter logs are displayed. 

```bash

```

## Stop Network

Do one of the following to stop the network: 

* Stop the network: 

    ```bash
    ./stop.sh
    ```
    
* Stop the network and remove the containers and volumes: 

    ```bash
    ./remove.sh
    ```

* Stop the network and delete the Docker images: 

    ```bash
    ./delete.sh
    ```
