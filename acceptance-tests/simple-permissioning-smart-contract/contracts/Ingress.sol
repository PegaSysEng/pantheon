pragma solidity >=0.4.0 <0.6.0;
// THIS CONTRACT IS FOR TESTING PURPOSES ONLY
// DO NOT USE THIS CONTRACT IN PRODUCTION APPLICATIONS

contract Ingress {
   struct ContractDetails {
      address owner;
      address contractAddress;
      uint16 version;
   }
   mapping(string => ContractDetails) registry;
   function registerName(string memory name, address addr, uint16 ver) public returns (bool) {
      // versions should start from 1
      require(ver >= 1);
      
      ContractDetails memory info = registry[name];
      require(info.owner == address(0) || info.owner == msg.sender);
      // create info if it doesn't exist in the registry
       if (info.contractAddress == address(0)) {
          info = ContractDetails({
             owner: msg.sender,
             contractAddress: addr,
             version: ver
          });
       } else {
          info.version = ver;
          info.contractAddress = addr;
       }
       // update record in the registry
       registry[name] = info;
       return true;
   }
    function getContractDetails(string memory name) public view returns(address, uint16) {
      return (registry[name].contractAddress, registry[name].version);
   }
}