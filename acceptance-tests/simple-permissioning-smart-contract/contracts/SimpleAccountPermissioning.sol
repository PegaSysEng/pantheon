pragma solidity >=0.4.0 <0.6.0;
// THIS CONTRACT IS FOR TESTING PURPOSES ONLY
// DO NOT USE THIS CONTRACT IN PRODUCTION APPLICATIONS

contract SimpleAccountPermissioning {
    address[] private whitelist;

    function transactionAllowed(
        address sender,
        address target,
        uint256 value,
        uint256 gasPrice,
        uint256 gasLimit,
        bytes memory payload)
        public view returns (bool) {
        return whitelistContains(sender);
    }
    function addAccount(address account) public {
        whitelist.push(account);
    }
    function removeAccount(address account) public {
        for (uint256 i = 0; i < whitelist.length; i++) {
          if (whitelist[i] == account) {
            whitelist[i] = whitelist[whitelist.length - 1];
            whitelist.length --;
          }
        }
    }
    function whitelistContains(address account) public view returns(bool) {
        for (uint256 i = 0; i < whitelist.length; i++) {
          if (whitelist[i] == account) {
            return true;
          }
        }
        return false;
    }
}
