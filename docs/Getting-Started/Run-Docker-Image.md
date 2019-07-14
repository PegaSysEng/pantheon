description: Run Pantheon using the official docker image
<!--- END of page meta data -->

# Running Pantheon from Docker Image

A Docker image is provided to run a Pantheon node in a Docker container. 

Use this Docker image to run a single Pantheon node without installing Pantheon. 

## Prerequisites

* [Docker](https://docs.docker.com/install/)   

* MacOS or Linux 
    
    !!! important 
        The Docker image does not run on Windows. 

## Quickstart

To run a Pantheon node in a container connected to the Ethereum mainnet: 

```bash tab="latest"
docker run pegasyseng/pantheon:latest
```

```bash tab="{{ versions.pantheon_stable }}"
docker run pegasyseng/pantheon:{{ versions.pantheon_stable }}
```

!!! note
    `latest` runs the latest cached version. To pull the latest version, use `docker pull pegasyseng/pantheon:latest`. 
 
## Command Line Options 
 
!!!note
    You cannot use the following Pantheon command line options when running Pantheon from the Docker image:
    QUESTION: Still correct? Asked in prod-dev
    * Host and port options, see [Exposing Ports](#exposing-ports). Host and port options are: 
        - [`--rpc-http-host`](../Reference/Pantheon-CLI-Syntax.md#rpc-http-host) and [`--rpc-http-port`](../Reference/Pantheon-CLI-Syntax.md#rpc-http-port)
        - [`--metrics-host`](../Reference/Pantheon-CLI-Syntax.md#metrics-host) and [`--metrics-port`](../Reference/Pantheon-CLI-Syntax.md#metrics-port)
        - [`--metrics-push-host`](../Reference/Pantheon-CLI-Syntax.md#metrics-push-host) and [`--metrics-push-port`](../Reference/Pantheon-CLI-Syntax.md#metrics-push-port)
        - [`--p2p-host`](../Reference/Pantheon-CLI-Syntax.md#p2p-host) and [`--p2p-port`](../Reference/Pantheon-CLI-Syntax.md#p2p-port)
        - [`--rpc-ws-host`](../Reference/Pantheon-CLI-Syntax.md#rpc-ws-host) and [`--rpc-ws-port`](../Reference/Pantheon-CLI-Syntax.md#rpc-ws-port)
    
    All other [Pantheon command line options](/Reference/Pantheon-CLI-Syntax) work in the same way as when Pantheon is installed locally.
 
## Starting Pantheon 

### Run a Node for Testing 

To run a node that mines blocks at a rate suitable for testing purposes with WebSockets enabled: 
```bash
docker run -p 8546:8546 --mount type=bind,source=/<myvolume/pantheon/testnode>,target=/var/lib/pantheon pegasyseng/pantheon:latest --miner-enabled --miner-coinbase fe3b557e8fb62b89f4916b721be55ceb828dbd73 --rpc-http-cors-origins="all" --rpc-ws-enabled --network=dev
```

### Run a Node on Rinkeby Testnet 

To run a node on Rinkeby: 
```bash
docker run -p 30303:30303 --mount type=bind,source=/<myvolume/pantheon/rinkeby>,target=/var/lib/pantheon pegasyseng/pantheon:latest --network=rinkeby
```

### Run a Node on Ethereum Mainnet 

To run a node on the Ethereum mainnet: 

```bash
docker run -p 30303:30303 --mount type=bind,source=/<myvolume/pantheon>,target=/var/lib/pantheon pegasyseng/pantheon:latest
```

To run a node on mainnet with the HTTP JSON-RPC service enabled: 
```bash
docker run -p 8545:8545 -p 30303:30303 --mount type=bind,source=/<myvolume/pantheon>,target=/var/lib/pantheon pegasyseng/pantheon:latest --rpc-http-enabled
```

## Stopping Pantheon and Cleaning up Resources

When you're done running nodes, you can shut down the node container without deleting resources. Alternatively, you can delete the container (after stopping it) and its associated volume. Run `docker container ls` and `docker volume ls` to obtain the container and volume names. Then run the following commands:

To stop a container:
```bash
docker stop <container-name>
```

To delete a container:
```bash
docker rm <container-name>
```

To delete a container volume (optional):
```bash
docker volume rm <volume-name>
```    

## Exposing Ports

Expose ports for P2P peer discovery, GraphQL, metrics, and HTTP and WebSockets JSON-RPC. This is required to use the 
defaults ports or specify different ports (the equivalent of specifying the [`--rpc-http-port`](../Reference/Pantheon-CLI-Syntax.md#rpc-http-port), 
[`--p2p-port`](../Reference/Pantheon-CLI-Syntax.md#p2p-port), [`--rpc-ws-port`](../Reference/Pantheon-CLI-Syntax.md#rpc-ws-port), 
[`--metrics-port`](../Reference/Pantheon-CLI-Syntax.md#metrics-port), [`--graphql-http-port`](../Reference/Pantheon-CLI-Syntax.md#graphql-http-port), 
and [`--metrics-push-port`](../Reference/Pantheon-CLI-Syntax.md#metrics-push-port) options).

To run Pantheon exposing local ports for access: 
```bash
docker run -p <localportJSON-RPC>:8545 -p <localportWS>:8546 -p <localportP2P>:30303 pegasyseng/pantheon:latest --rpc-http-enabled --rpc-ws-enabled
```

!!!example
    To enable RPC calls to http://127.0.0.1:8545 and P2P discovery on http://127.0.0.1:13001:
    ```bash
    docker run -p 8545:8545 -p 13001:30303 pegasyseng/pantheon:latest --rpc-http-enabled
    ```
