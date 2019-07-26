description: JSON-RPC methods to use for EEA-compliant privacy 
<!--- END of page meta data -->

# Using EEA-compliant Privacy 

When using EEA-compliant privacy, the group of nodes specified by `privateFrom`and `privateFor` form a privacy group and are given a unique 
privacy group ID by Orion.

Enable the [`EEA` API methods](../../Reference/Pantheon-API-Methods.md#eea-methods) using the [`--rpc-http-api`](../../Reference/Pantheon-CLI-Syntax.md#rpc-http-api) 
or [`--rpc-ws-api`](../../Reference/Pantheon-CLI-Syntax.md#rpc-ws-api) command line options.

Specify `privateFor` when creating the signed transaction passed as an input parameter to [`eea_sendRawTransaction`](../../Reference/Pantheon-API-Methods.md#eea_sendrawtransaction)
to create an EEA-compliant private transaction. 

