title: System Requirements
description: System requirements to sync and run Pantheon 
<!--- END of page meta data -->

# System Requirements 

The system requirements for Pantheon depend on a number of factors: 

* Network - are you connecting to a public Ethereum network (eg, mainnet) or a private network 
* Network size if connecting to a private network 
* Number and complexity of queries being performed by node 

## CPU Requirements for Syncing to Ethereum Mainnet

Syncing to the Ethereum mainnet using a t3.xlarge takes about 15 hours.  The sync to mainnet can be completed with 
a t3.large but takes longer.  

!!! tip
    Once the initial sync has been completed, the instance size can be reduced. 

## RAM 

When connected to a public Ethereum network, 8GB of RAM is required.

When connected to a private network, 4GB of RAM is required. 

## Disk Space 

Syncing to the Ethereum mainnet requires 1.5 TB to 2 TB for a full sync. 

Disk space requirements for private network depend on the network size.  A small test network  might require 200 MB.
