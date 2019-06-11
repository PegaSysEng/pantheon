description: EEA JS methods reference
<!--- END of page meta data -->


web3.eea.getMakerTransaction

web3.eea.generatePrivacyGroup 

web3.eea.sendRawTransaction 

web3.eea.getTransactionCount 

web3.eea.getTransactionReceipt 



### eea.getMakerTransaction

Gets the [privacy marker transaction](../Privacy/Private-Transaction-Processing.md) transaction receipt.

**Parameters**

`txHash` - `string` : Transaction hash of the private transaction
`retries` - `int` : Number of attempts to make to get the private marker transaction receipts 
`delay` - `int` : Delay between retries (in seconds?)

**Returns**

`result` : Privacy marker transaction receipt  

!!! example
    ```bash tab="JS request"
    eea.getMakerTransaction("tx hash -add", 5, 2)
    ```
    
    ```bash tab="Result"
    Add this
    ```
    
 
