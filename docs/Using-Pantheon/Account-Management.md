## Using Wallets for Account Management

Pantheon does not implement private key management. Use third-party tools (for example, [MetaMask](https://consensys.zendesk.com/hc/en-us/articles/360004685212-Generating-MetaMask-Wallet-New-UI-) and [web3j](https://web3j.io/)) for creating accounts. 

In Pantheon, you can use the JSON-RPC methods:
 * [eth_getBalance](JSON-RPC-API-Methods#eth_getbalance) to obtain the account balance
 * [eth_sendRawTransaction](JSON-RPC-API-Methods#eth_sendrawtransaction) to transfer ether or create and interact with contracts (for more information, refer to [Transactions](Transactions#transactions)).  