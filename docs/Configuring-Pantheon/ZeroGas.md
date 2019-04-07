description: Configuring Zero Gas Networks 
<!--- END of page meta data -->

In public networks, gas ...

In private networks, gas is generally meaningless because Ether has no value in the private network. 
As a means to limit resource usage, you can give network participants a limited allocation of ETH 
to use for gas. 

To configure a private network that does not use gas, set the `min-gas-price` property in the genesis 
file to 0. 

<something about transactions> 

!!! tip
    The ``
    
    
# Using Pantheon and Truffle without gas

Rewarding miners for transactions in a block by paying gas is a key part of standard public ethereum.  In many consortium networks, the block miners (validators) are run by the organisations, and do not require the same incentive models.  In these networks it may make sense to have 0 gas fees.

Gas should still form some part of the setup of the network (in particular helping control block sizes)

Here we look at how to configure a network to work without gas, and some of the settings to consider when using truffle with this kind of setup.

In Pantheon the 3 items to consider are:
1) the size of a transaction or contract in a block
2) the size of a block (in terms of gas)
3) the minimum reward miners will accept for a transaction

In truffle we want to do the following:
1) the gasPrice
2) the gas to pay
3) the provider of the settings

# Pantheon
First let’s look at the items in Pantheon

## the size of a block (in terms of gas)

This is configured in the genesis block in the gasLimit field.

The maximum value for truffle is “0x1fffffffffffff”. Set this using:
“gasLimit”: “0x1fffffffffffff”

## the size of a transaction or contract in a block

This is in the config section in the genesis block.

Set “contractSizeLimit” to an integer value.  For example:
   “contractSizeLimit”: 2147483647 // The value of Integer.MAX_VALUE in java, the current maximum supported size in bytes.

## the minimum reward miners will accept for a transaction

This is set in the config of the miners (validators in an PoA network). Set this in the config file for the node, or pass it in through the commandline. For 0 gas, set the config to be 0.

In the commandline use:
--min-gas-price=0

or in the config:

min-gas-price=0

# Truffle

Having setup Pantheon to support 0 gas, you will now need to configure truffle to use it.

There will be a couple of settings to put into the truffle-config.js.

## gasPrice

Make the gasPrice 0.
     gasPrice: 0

## gas to pay

Set the gas Limit for a transaction (contract creation) to be the gasLimit -1

      gas: “0x1ffffffffffffe”

## wallet provider

As Pantheon does not perform transaction signing, you will need to specify a wallet-provider. The truffle hd wallet provider works well for this.

See https://github.com/trufflesuite/truffle/tree/develop/packages/truffle-hdwallet-provider for the details.

To use this first install it via npm:
npm install truffle-hdwallet-provider

require it in the truffle-config.js

var HDWalletProvider = require(“truffle-hdwallet-provider”);

In the network config then you will want:
provider: () =>
       new HDWalletProvider(mnemonic, “http://localhost:8545")