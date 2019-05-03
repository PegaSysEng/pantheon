description: Pantheon private network quickstart tutorial
<!--- END of page meta data -->

# Private Network with Privacy Enabled Quickstart Tutorial

The quickstart uses the Pantheon Docker image to run a private network of Pantheon and Orion nodes managed by Docker Compose.

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

- [Git command line](https://git-scm.com/)

- [Curl command line](https://curl.haxx.se/download.html) 

## Clone Pantheon Quickstart Source Code

Clone the repository from the `pantheon-quickstart` repository where `<version>` is replaced with the latest version (for example, `1.1.0`):

```bash tab="Linux/MacOS"
git clone --branch <version> https://github.com/PegaSysEng/pantheon-quickstart.git
```

```bash tab="Example"
git clone --branch 1.1.0 https://github.com/PegaSysEng/pantheon-quickstart.git
```

## Clone EEAJS Libraries 

Clone the `PegaSysEng/eeajs` library: 

```bash tab="Linux/MacOS"
git clone https://github.com/PegaSysEng/eeajs.git
```

## Start the Private Network with Privacy Enabled 

Change to the `pantheon-quickstart/privacy` directory: 

```bash tab="Linux/MacOS"
cd pantheon-quickstart/privacy 
```

Start the network: 

```bash tab="Linux/MacOS"
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