# Changelog

## 0.8.2
 
### Removed
 - Removed `import-blockchain` command because nothing exports to the required format yet (PR #223)

### Bug Fixes
 - `io.netty.util.internal.OutOfDirectMemoryError` errors by removing reference counting from network messages.
 - Log spam: endless loop in `nioEventLoopGroup` (#248 thanks to @5chdn for reporting) (PR #261)
 - Rinkeby import can stall with too many fragments (#228 thanks to @steffenkux and @5chdn for reporting) (PR #255)
 - Clique incorrectly used the chain ID instead of the network ID in ETH status messages (PR #209)
 - Gradle deprecation warnings (PR #246 with thanks to @jvirtanen)
 - Consensus issue on Ropsten:
    - Treat output length as a maximum length for CALL operations (PR #236)
    - ECRec precompile should return empty instead of 32 zero bytes when the input is invalid (PR #227)
 - File name too long error while building from source (#215 thanks to @5chdn for reporting) (PR #221)
 - Loop syntax in `runPantheonPrivateNetwork.sh` (PR #237 thanks to @matt9ucci)

### Additions and Improvements
 - Added `--ropsten` command line argument to make syncing to Ropsten easier (#186) (PR #197 with thanks to @jvirtanen)
 - Enabled constantinople in `--dev-mode` (PR #256)
 - Supported Constantinople with Clique (#245 thanks to @5chdn for reporting) (PR #250, PR #247)
 - Implemented `eth_chainId` JSON-RPC method (#206) (PR #219)
 - Updated client version to be ethstats friendly (PR #258)
 - Added `--node-private-key` option to allow nodekey file to be specified separately to data directory (#233 thanks to @peterbroadhurst for requesting)  (PR #234)
 - Added `--banned-nodeids` option to prevent connection to specific nodes (PR #254)
 - Send client quitting disconnect message to peers on shutdown (#184) (PR #253)
 - Improved error message for port conflict error (PR #232)
 - Improved documentation by adding the following pages:
    * [Getting Started](https://github.com/PegaSysEng/pantheon/wiki/Getting-Started)
    * [Network ID and Chain ID](https://github.com/PegaSysEng/pantheon/wiki/NetworkID-And-ChainID)
    * [Node Keys](https://github.com/PegaSysEng/pantheon/wiki/Node-Keys) 
    * [Networking](https://github.com/PegaSysEng/pantheon/wiki/Networking) 
    * [Accounts for Testing](https://github.com/PegaSysEng/pantheon/wiki/Accounts-for-Testing) 
    * [Logging](https://github.com/PegaSysEng/pantheon/wiki/Logging) 
    * [Proof of Authority](https://github.com/PegaSysEng/pantheon/wiki/Proof-of-Authority) 
    * [Passing JVM Options](https://github.com/PegaSysEng/pantheon/wiki/Passing-JVM-Options) 


 ### Technical Improvements
 - Upgraded Ethereum reference tests to 6.0 beta 2. (thanks to @jvirtanen for the initial upgrade to beta 1)
 - Set Java compiler default encoding to UTF-8 (PR #238 thanks to @matt9ucci)
 - Removed duplicate code defining default JSON-RPC APIs (PR #218 thanks to @matt9ucci)
 - Improved code for parsing config (PRs #208, #209)
 - Use `java.time.Clock` in favour of a custom Clock interface (PR #220)
 - Improve modularity of storage systems (PR #211, #207)
 - Treat JavaDoc warnings as errors (PR #171)
 - Add benchmark for `BlockHashOperation `as a template for benchmarking other EVM operations (PR #203)
 - Added unit tests for `EthBlockNumber` (#189) (PR #195 thanks to @jvirtanen)
 - Code style improvements (PR #196 thanks to @jvirtanen)
 - Added unit tests for `Web3ClientVersion` (#191) (PR #194 with thanks to @jvirtanen)
 - Removed RLPUtils from `RawBlockIterator` (PR #179)
 - Replace the JNI based snappy library with a pure-Java version (PR #257)
