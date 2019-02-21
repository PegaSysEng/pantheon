# Roadmap
This document represents the current working roadmap for Pantheon.  It is a living document, which will 
evolve and change over time.  In particular the features in later versions are likely to be refined and change.

We use the approach of  `#now`, `#next`, `#later` [used by foursquare](https://medium.com/@noah_weiss/now-next-later-roadmaps-without-the-drudgery-1cfe65656645), with a slightly different time horizon.  
Our now scale is about 3 months, next about 6 months, and then later is beyond.

## Now/v1.1
Our key areas for now are:
* Privacy
* Smart Contract Based Permissioning
* First Class Client

### Privacy
The Enterprise Ethereum `restricted` privacy will be implemented.

### Smart Contract Based Permissioning
Building on the Permissioning system  implemented in version 1.0 of Pantheon, we will use a smart contract to share this information across the network, giving a consistent set of permissions, and ensuring that all nodes in the network work consistently.

### First Class Client
There is an ongoing piece of work underway enhancing the core performance of Pantheon, and ensuring that it behaves well as a first class client. The key elements of this are implementation of some performance benchmarks and enhancing our fast sync mechanism.

## Next
The key areas for next are:
* IBFT 2.x
* Ethereum 1.x
* State Pruning

### IBFT 2.x 

Continued network and consensus improvements to IBFT 2.0. 

### Ethereum 1.x
The 1.x series of EIPs that are currently under early development will be implemented as they are ready.  In addition, the team will work on helping the specification of the Ethereum roadmap.

### State Pruning 

State pruning will be implemented. State pruning reduces the disk space required for the Pantheon database by discarding outdated world state data.

## Future
In addition to making incremental improvements to the above features, there will be some bigger pieces of work.  
These are deliberately kept vague at this time, and will be elaborated upon when they move up to the now and next levels of work.
* Ethereum 2.0
* Advanced Privacy
* Alternate Consensus Mechanisms
* Sidechains

