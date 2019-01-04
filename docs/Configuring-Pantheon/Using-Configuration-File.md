# Using a Configuration File

To specify command line options in a file, use a TOML configuration file. 

The configuration file can be saved and reused accross node startups. To specify the configuration file,
use the [`--config` option](../Reference/Pantheon-CLI-Syntax.md#config). 



The configuration file must be a valid TOML file and is composed of key/value pairs.

Each key is the same as the corresponding CLI option name without the leading dashes (`--`).

The `config` option is not used in the configuration file.

Values must be treated according to TOML specifications for string, numbers, arrays, and booleans.

!!!example "Example TOML configuration file"
    ```toml
    # Valid TOML config file
    datadir="~/pantheondata" # Path
    
    # Network
    bootnodes=["enode://001@123:4567", "enode://002@123:4567", "enode://003@123:4567"]
    p2p-listen="1.2.3.4:1234" # IP:port
    max-peers=42
    rpc-listen="5.6.7.8:5678" # IP:port
    ws-listen="9.10.11.12:9101" # IP:port
    
    # Chain
    genesis="~/genesis.json" # Path to the custom genesis file
    
    # Mining
    miner-enabled=true
    miner-coinbase="0x0000000000000000000000000000000000000002"
    ```
    
!!!example "Starting Pantheon with a Configuration File"
    ```bash
    pantheon --config=home/node/config.toml
    ```
