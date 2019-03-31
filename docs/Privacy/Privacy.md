# Privacy

Privacy in Pantheon refers to the ability to send transactions that are kept private between the 
involved parties. Other parties have no access to the transaction content, the sending party, or the 
list of participating parties. 

This page describes what private transactions are and how they are processed in Pantheon. Sending Private
Transactions describes how to configure Pantheon and the Private Transaction Manager (for example, Orion) to send private transactions and
how to send private transactions. 

## Private Transactions

Private transactions are defined as *restricted* or *unrestricted*:  

* In restricted private transactions the payload of the private transaction is only stored in the nodes
participating in the transaction. 

* In unrestricted private transactions the payload of the private transaction is transmitted to all nodes
in the network but readable only by actors involved in the transaction.   

!!! important 
    Pantheon implements restricted private transactions only.

### Privacy Concepts

Processing private transactions involves the following concepts: 

- **Precompiled Contract**: Smart contract compiled from the source language to EVM bytecode and stored by an 
Ethereum node for later execution.

- **Private State**: Data that is not shared in the clear in the globally replicated state tree. Private state
data can represent arrangements involving multiple parties. For example, data in private transactions.

- **Actor**: Public key of an involved party in a private transaction (for example, public key of the sender).

- **Privacy Group ID**: Unique ID associated with the set of actors involved in a private transaction.

- **Private Transaction**: Transaction where some information (for example, the payload data, or sender and
 recipient) is only available to the subset of actors that are parties to that transaction.

- **Private Transaction Manager**: Subsystem of an Enterprise network for implementing privacy. Also known as an Enclave.
For example, [Orion](https://github.com/PegaSysEng/orion).

- **Private Marker Transaction**: Public Ethereum transaction with a payload of the Enclave-key of the 
private transaction. The `to` attribute of the Privacy Marker Transaction is the privacy precompile contract. 
The Privacy Marker Transaction is signed with the nodes default key.


### How a Private Transaction is Processed

1. A private transaction is sent to the [eea_sendrawtransaction](../Reference/JSON-RPC-API-Methods.md#eea_sendRawTransaction)
endpoint, using the `privateFor` attribute to specify the list of actors,
The `privateFrom` attribute specifies the sending actor, and `restriction`
attribute is set to *restricted*.
 
2. The JSON RPC endpoint hands off the private transaction to a
Private Transaction Handler.

3. The Private Transaction Handler sends the private transaction to an Enclave 
(Private Transaction Manager)

4. The Enclave will store the private transaction in map. The transaction will
be associated with a random key (Enclave-key) identifying that transaction 
in the enclave and a privacy group id.

5. The Enclave distributes the private transaction point-to-point directly
to the enclaves of actors specified in the `privateFor` attribute. All
enclaves storing this transaction will associate it with the same key and 
privacy group id.

6. The Enclave returns the Enclave-key back to the Private
Transaction Handler (as a response to the synchronous HTTP call).
     
7. The Private Transaction Handler will create a Privacy Marker Transaction (PMT).
The PMT is sent to the transaction pool. Once the PMT has been mined in a block
it is distributed to all nodes in the network. 

8. The Transaction Processor of every node in the network will process the 
PMT as per any other public transaction. The PMT will be passed to the
privacy precompile contract on those nodes that contain that precompile
contract specified in the `to` attribute of the PMT.

8. The Privacy Precompile Contract queries the Enclave for the Private
Transaction and Privacy Group Id based on the Enclave-key.

9. The Privacy Precompile Contract executes the Private Transaction using the
Private Transaction Processor, specifying the private world state to use based on the
Privacy Group Id. The Private Transaction Processor can read and write to 
the private world state, and read from the public world state.

### Privacy JSON-RPC API method

The [EEA methods](../Reference/JSON-RPC-API-Methods.md#eea_sendRawTransaction) created to
provide and support privacy.

!!!note EEA methods are for privacy features. Privacy features are under development and will be available in v1.1.

