# Events and Logs

When a transaction is mined, smart contracts emit events and write logs to the blockchain.  

Logs are associated with the contract address and are included in the blockchain but logs are not accessible 
from within contracts. For example, logs can be used to display all transfers made using a specific contract 
but not the current state of the contract. 

A Dapp frontend can either poll for logs using the [JSON-RPC API filter methods](Accessing-Logs-Using-JSON-RPC.md) or subscribe to logs using the [RPC Pub/Sub API](RPC-PubSub.md#logs). 

## Event Parameters

Up to three event parameters can have the `indexed` attribute. Indexed parameters are stored as `topics` 
in the logs. Indexed parameters can be searched and filtered.

The first topic is the event signature hash. 

Non-indexed parameters are included in the logs `data` but cannot be easily searched or filtered. 

## Topic Filters

[Filter options objects] have a `topics` key to filter log entries by topics. 

Topics are order-dependent. A transaction with a log containing topics `[A, B]` is matched with the following topic filters:

    * [] - Match any topic
    * [A] - Match A in first position 
    * [[null], [B]] - Match any topic in first position AND B in second position
    * [[A],[B]] - Match A in first position AND B in second position
    * [[A, B], [C, D]] - Match (A OR B) in first position AND (C OR D) in second position 

### Example  

The following Solidity contract stores one indexed and one non-indexed parameter and the event emits the value of each parameter. 

```json
pragma solidity ^0.5.1;
contract Storage {
	uint256 public valueIndexed;
	uint256 public valueNotIndexed;

	event Event1(uint256 indexed valueIndexed, uint256 valueNotIndexed);

	function setValue(uint256 _valueIndexed, uint256 _valueNotIndexed) public {
    	valueIndexed = _valueIndexed;
    	valueNotIndexed = _valueNotIndexed;
    	emit Event1(_valueIndexed, _valueNotIndexed); 
	}
}
```

An example log entry created by invoking this contract with `valueIndexed` set to 5 and `valueNotIndexed` set to 7 is:  

```json
{
  "logIndex": "0x0",
  "removed": false,
  "blockNumber": "0x4d6",
  "blockHash": "0x7d0ac7c12ac9f622d346d444c7e0fa4dda8d4ed90de80d6a28814613a4884a67",
  "transactionHash": "0xe994022ada94371ace00c4e1e20663a01437846ced02f18b3f3afec827002781",
  "transactionIndex": "0x0",
  "address": "0x43d1f9096674b5722d359b6402381816d5b22f28",
  "data": "0x0000000000000000000000000000000000000000000000000000000000000007",
  "topics": [
    "0xd3610b1c54575b7f4f0dc03d210b8ac55624ae007679b7a928a4f25a709331a8",
    "0x0000000000000000000000000000000000000000000000000000000000000005"
   ]
}
```

### Topic Filters Example 

The following filter option object returns log entries for the example contract where valueIndexed is set to 
5 or 7. 

```json
{
  "fromBlock":"earliest", 
  "toBlock":"latest", 
  "address":"0x43d1f9096674b5722d359b6402381816d5b22f28",
  "topics":[
    ["0xd3610b1c54575b7f4f0dc03d210b8ac55624ae007679b7a928a4f25a709331a8"], 
    ["0x0000000000000000000000000000000000000000000000000000000000000005", "0x0000000000000000000000000000000000000000000000000000000000000003"]
  ]
}
```

