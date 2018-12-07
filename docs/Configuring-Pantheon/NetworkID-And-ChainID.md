# Network ID and Chain ID

Ethereum networks have a network ID and a chain ID. The network ID is specified using the `--network-id` option and the chain ID is specified in the genesis file.

For most networks including mainnet and the public testnets, the network ID and the chain ID are the same. 

The network ID is automatically set by Pantheon when connecting to the Ethereum mainnet (`1`), Rinkeby (`4`), and Ropsten(`3`).

When using the `--dev-mode` or `--genesis` options, specify the network ID using the `--network-id` option. 

