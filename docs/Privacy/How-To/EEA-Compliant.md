description: JSON-RPC methods to use for EEA-compliant privacy 
<!--- END of page meta data -->

# Using EEA-compliant Privacy 

When using EEA-compliant privacy, the group of nodes specified by `privateFrom`and `privateFor` form a privacy group and are given a unique 
privacy group ID by Orion.

Specify `privateFor` when creating the signed transaction passed as an input parameter to [`eea_sendRawTransaction`](../../Reference/Pantheon-API-Methods.md#eea_sendrawtransaction)
to create an EEA-compliant private transaction. 

