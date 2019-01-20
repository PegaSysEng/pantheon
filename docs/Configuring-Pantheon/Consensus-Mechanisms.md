# Comparing Proof of Authority Consensus Protocols  

Pantheon implements the Clique and IBFT 2.0 Proof of Authority consensus protocols. Proof of Authority 
consesus protocols are used when participants are known to each other and there is a level of trust between them. 
For example, in a permissioned consortium network. 

Proof of Authority consensus protocols are faster and allows much greater throughput of transactions 
than the Ethash Proof of Work consensus protocol used on the Ethereum MainNet. 

In Clique and IBFT 2.0, a group of nodes in the network act as signers(Clique) or validators(IBFT 2.0). These nodes propose, validate, 
and add blocks to the blockchain. Nodes are added to or removed from the signer/validator pool by the existing group of nodes voting. 

!!! note
    The term validator is used to refer to signers and validators for the rest of this page. 

## Properties 
   
Properties to consider when comparing Clique and IBFT 2.0 are: 

* Immediate finality 
* Liveness 
* Speed 
* Minimum number of validators 

### Immediate Finality 

IBFT 2.0 has immediate finality. When using IBFT 2.0 there are no forks and all valid blocks are included in the main chain.

Clique does not have immediate finality. Implementations using Clique must be aware of forks and chain reorganizations occurring. 

### Liveness 

Clique is more fault tolerant than IBFT 2.0. Clique tolerates up to half to the validators failing. 

IBFT 2.0 networks tolerates up to F faulty nodes in a N validator nodes network, where N = 3F + 1. That is, IBFT 2.0 fails if more 
than 1/3 of the validators are faulty. 

### Speed 

Reaching consensus and adding blocks is faster in Clique networks. 

For both Clique and IBFT 2.0, the time to add new blocks increases as the number of validators increases.   

### Minimum Number of Validators 

IBFT 2.0 requires 4 nodes to be byzantine fault tolerant. 






