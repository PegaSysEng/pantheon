description: Pantheon IBFT 2.0 Proof-of-Authority (PoA) consensus protocol implementation
<!--- END of page meta data -->

# IBFT 2.0

Pantheon implements the IBFT 2.0 Proof-of-Authority (PoA) consensus protocol. IBFT 2.0 can be used for private networks. 

In IBFT 2.0 networks, transactions and blocks are validated by approved accounts, known as validators. 
Validators take turns to create the next block. Existing validators propose and vote to add or remove validators. 

## Genesis File

To use IBFT 2.0 requires an IBFT 2.0 genesis file. The genesis file defines properties specific to IBFT 2.0:

!!! example "IBFT 2.0 Genesis File (stripped)"
    ```json
      {
        "config": {
          ...
          "revisedibft": {
            "blockperiodseconds": 2,
            "epochlength": 30000,
            "requesttimeoutseconds": 10
          }
        },
        "nonce": "0x0",
        "extraData": "0xf853a00000000000000000000000000000000000000000000000000000000000000000ea94be068f726a13c8d46c44be6ce9d275600e1735a4945ff6f4b66a46a2b2310a6f3a93aaddc0d9a1c193808400000000c0",
        "difficulty": "0x1",
        "mixHash": "0x63746963616c2062797a616e74696e65206661756c7420746f6c6572616e6365",
        ...
      }
    ```
    
Properties specific to IBFT 2.0 are:

* `blockperiodseconds` - Minimum block time in seconds. 
* `epochlength` - Number of blocks after which to reset all votes.
* `requesttimeoutseconds` - Timeout for each consensus round before a round change. 
* `extraData` - `[32 Bytes Vanity][RLP --> 0Seals; Round=Int(0); List<Validators>] `

!!! note
    The Recursive Length Prefix (RLP) encoding scheme is a space efficient object serialization scheme used in Ethereum.

Properties that have specific values in IBFT 2.0 genesis files are: 

* `nonce` - `0x0`
* `difficulty` - `0x1`
* `mixHash` - `0x63746963616c2062797a616e74696e65206661756c7420746f6c6572616e6365` for Istanbul block identification.

To start a node on an IBFT 2.0 private network, use the [`--genesis`](../Reference/Pantheon-CLI-Syntax.md#genesis`) option to specify the custom genesis file. 

## Adding and Removing Validators

To propose adding or removing validators using the JSON-RPC methods, enable the RPC interface 
using the [`--rpc-enabled`](../Reference/Pantheon-CLI-Syntax.md#rpc-enabled) option. If also using the 
[`--rpc-api`](../Reference/Pantheon-CLI-Syntax.md#rpc-api) option, include `IBFT`.

The JSON-RPC methods to add or remove validators are:

* [ibft_getPendingVotes](../Reference/JSON-RPC-API-Methods.md#ibft_getPendingVotes)
* [ibft_proposeValidatorVote](../Reference/JSON-RPC-API-Methods.md#ibft_proposeValidatorVote)
* [ibft_discardValidatorVote](../Reference/JSON-RPC-API-Methods.md#ibft_discardValidatorVote)

To propose adding a validator, call `ibft_proposeValidatorVote` specifying the address of the proposed signer and `true`.
!!! example "JSON-RPC ibft_proposeValidatorVote Request Example"
    ```bash
    curl -X POST --data '{"jsonrpc":"2.0","method":"ibft_proposeValidatorVote","params":["0xFE3B557E8Fb62b89F4916B721be55cEb828dBd73", true], "id":1}' <JSON-RPC-endpoint:port>
    ``` 

When the next block is created by the validator, a vote is added to the block for the proposed validator.  

When more than half of the existing validators propose adding the validators and their votes have been 
distributed in blocks, the validator is added and can begin validating blocks. 

Use `ibft_getValidatorsByBlockNumber` to return a list of the validators and confirm your proposed validator has been added. 

!!! example "JSON-RPC ibft_getValidatorsByBlockNumber Request Example"
    ```bash
    curl -X POST --data '{"jsonrpc":"2.0","method":"ibft_getValidatorsByBlockNumber","params":["latest"], "id":1}' <JSON-RPC-endpoint:port>
    ```  
 
To discard your proposal after confirming the validator was added, call `ibft_discardValidatorVote` specifying the address of the proposed validator.

!!! example "JSON-RPC ibft_discardValidatorVote Request Example"
    ```bash
    curl -X POST --data '{"jsonrpc":"2.0","method":"ibft_discardValidatorVote","params":["0xFE3B557E8Fb62b89F4916B721be55cEb828dBd73"], "id":1}' <JSON-RPC-endpoint:port>
    ```

The process for removing a validator is the same as adding a validator except you specify `false` as the second parameter of `ibft_proposeValidatorVote`. 

### Epoch Transition

At each epoch transition, all pending votes collected from received blocks are discarded. Existing proposals remain 
in effect and validators re-add their vote the next time they create a block. 

Define the number of blocks between epoch transitions in the genesis file. 