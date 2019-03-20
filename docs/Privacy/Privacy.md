## Privacy

### Private Transaction

A Transactions whose payload is only visible/store to the network actors
whose public keys are located in the `privateFor` parameter, and not in
any other node . `privateFor` can take multiple addresses in a comma
separated list. ( [eea_sendrawtransaction](../Reference/JSON-RPC-API-Methods.md#eea_sendRawTransaction) - see an example of private
transaction). Private transactions are kept private between related
parties, so unrelated parties have no access to the content of the
transaction, the sending party, or the list of participating addresses.


### Important Concepts
- **Precompiled Contract**:    A smart contract compiled from its source
 language to EVM bytecode and stored by an Ethereum node for later
 execution.
- **Privacy**: It is “The right of individuals to control or influence
 what information related to them may be collected and stored and by
 whom and to whom that information may be disclosed.” For the purposes
 of this Specification, this right of privacy also applies to
 organizations to the extent permitted by law.
- **Private State**:   A data that is not shared in the clear in the
 globally replicated state tree. This data can represent bilateral or
 multilateral arrangements between counterparties, for example in
 private transactions.
- **Private Transaction**: A transaction where some information about
 the transaction, such as the payload data, or the sender and
 recipient, is only available to the subset of network actors who are
 parties to that transaction.
- **Private Transaction Manager**: A subsystem of an Enterprise Ethereum
  system for implementing privacy and permissioning known as Enclave.
  e.g. [Orion](https://github.com/PegaSysEng/orion) .
- **Actor**:   The public key of someone involved in the private transaction (as per the privateFor member).
- **Privacy group**:   The actors that are involved in a set of transactions.


### How Private Transaction is Processed

1. DApp sends a private transaction through [eea_sendrawtransaction](../Reference/JSON-RPC-API-Methods.md#eea_sendRawTransaction),
  uses the `privateFor` attribute to specify the list of actors,
  `privateFrom` with the sender public key, and `restriction` to handle
  with *restricted*.
 
2. The JSON RPC detects the private transaction and hands it off to a
   Private Transaction Handler.

3. The Private Transaction Handler connect to the Enclave (Private
   Transaction Manager) instance and sends the transaction through this
   instance.

4. Enclave will authenticate, encrypt and distribute the private
   transaction through the nodes, including the actors, and an id for
   the privacy group that it has generated. Each Enclave node will store
   the transaction, along with the associated privacy group and actor
   list.

5. Enclave sends the Enclave-key back to the Private
  Transaction Handler (as a response to the synchronous HTTP call),
  along with the privacy group.
 
6. The Private Transaction Handler will lookup the address of the
  Privacy Precompile Smart Contract and will create and send a
  transaction to this Precompiled contract, including the Enclave-key as a parameter. 
  This transaction will be known as a Privacy Marker Transaction.
  -  The Privacy Marker Transaction will be signed using the Node
     Signing Key.

7. The Transaction Processor will process the Privacy Marker Transaction
  as per any other public transaction. When Privacy Marker transaction is mined into a block, the
  Privacy Precompile contract executes with the Enclave-key from Privacy Marker Transaction. 
  The Privacy Marker Transaction is propagated via Ethereum devp2p network.

8. The Privacy Precompile Contract queries the Enclave for the Private
  Transaction and Privacy Group Id based on the Enclave-key.

9. The Privacy Precompile Contract executes the Private Transaction using the
 Private Transaction Processor, specifying the world state to use based on the
 Privacy Group Id. The Private Transaction Processor will need to be
 able to read and write to the Privacy Group State, and read from the
 public world state.


#### Restricted Private Transaction
The payload data is transmitted to and readable only by the direct
actors. These transactions are referred to as restricted private
transactions.

#### Unrestricted Private Transaction
When the payload is transmitted to
whole the actors in the network but
readable only by the direct actors. These transactions are referred to
as unrestricted private transactions.

!!!note Unrestricted Private Transaction are not supported

### Privacy JSON-RPC API method

The [EEA methods](../Reference/JSON-RPC-API-Methods.md#eea_sendRawTransaction) created to
provide and support privacy.



!!!note EEA methods are for privacy features. Privacy features are under development and will be available in v1.1.


