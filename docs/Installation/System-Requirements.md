title: System Requirements
description: System requirements to sync and run Pantheon 
<!--- END of page meta data -->

# System Requirements 

The system requirements for Pantheon depend on a number of factors: 

* Network - are you connecting to a public Ethereum network (eg, mainnet) or a private network 
* Network size if connecting to a private network 
* Number and complexity of queries being performed by node 

## Determining System Requirements  

To determine system requirements, monitor CPU and disk space requirements using [Prometheus](https://docs.pantheon.pegasys.tech/en/stable/Monitoring/Monitoring-Performance/#monitor-node-performance-using-prometheus). 
A sample [Grafana dashboard](https://grafana.com/grafana/dashboards/10273) is provided for Pantheon. 

!!! tip
    CPU requirements are highest when syncing to the network and reduce once the node is synchronized to the chain head. 

## RAM 

When connected to the Ethereum mainnet, 8GB of RAM is required.

When connected to a private network, 4GB of RAM is required. 

## Disk Space 

Syncing to the Ethereum mainnet requires 3TB for a full sync. 
