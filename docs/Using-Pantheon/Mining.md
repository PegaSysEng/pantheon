# Mining

Start Pantheon with the following options to enable mining: 
```bash
bin/pantheon --miner-enabled --miner-coinbase <account>
```

Where `<account>` is the account to which mining rewards are to be paid. For example, `fe3b557e8fb62b89f4916b721be55ceb828dbd73`.

JSON-RPC API methods for mining are: 
* [`miner_start`](JSON-RPC-API-Methods#miner_start) to start mining. 
* [`miner_stop`](JSON-RPC-API-Methods#miner_stop) to stop mining. 
* [`eth_mining`](JSON-RPC-API-Methods#eth_mining) to determine whether the client is actively mining new blocks.   