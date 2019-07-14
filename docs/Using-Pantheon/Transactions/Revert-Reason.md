description: Including revert reason in transactions 
<!--- END of page meta data -->

# Revert Reason 

In contracts, the [`revert`](https://solidity.readthedocs.io/en/v0.5.10/control-structures.html#revert) function 
triggers an exception to flag an error and revert the current call. An optional string message containing 
information about the error is passed back to the caller.

!!! example
    ```sol
    pragma solidity >=0.5.0 <0.7.0;
    
    contract VendingMachine {
        function buy(uint amount) public payable {
            if (amount > msg.value / 2 ether)
                revert("Not enough Ether provided.");
            // Alternative way to do it:
            require(
                amount <= msg.value / 2 ether,
                "Not enough Ether provided."
            );
            // Perform the purchase.
        }
    }
    ```

## Enabling Revert Reason 

Use the [`--revert-reason-enabled`](../../Reference/Pantheon-CLI-Syntax.md#revert-reason-enabled) command line option
to include the revert reason in the transaction receipt in Pantheon. 

!!! caution 
    Enabling revert reason may use a significant amount of memory. We do not recommend enabling revert
    reason when connected to the Ethereum mainnet. 

## Where is the Revert Reason Included 

When revert reason is enabled, the revert reason is included in the transaction receipt and returned by 
[`eth_getTransactionReceipt`](../../Reference/Pantheon-API-Methods.md#eth_gettransactionreceipt). 

!!! important 
    The revert reason is not included in the transactions receipts root hash. Not being included in the 
    transactions receipts root hash means the revert reason is only available to nodes that execute the 
    transaction when importing the block. That is, the revert reason is not available if using fast sync. 

!!! example 
    QUESTION WITH DROPBEAR FOR EXAMPLE
    
NOT SUPPORTED BY WEB3J/WEB3JS -> QUESTION WITH DROPBEAR


