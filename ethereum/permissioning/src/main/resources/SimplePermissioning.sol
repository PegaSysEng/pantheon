pragma solidity >=0.4.0 <0.6.0;


contract SimplePermissioning {
    struct Enode {
        bytes32 enodeHigh;
        bytes32 enodeLow;
        bytes16 enodeHost; // Ipv4 OR Ipv6
        uint16 enodePort;
    }
    mapping(uint16 => Enode) public whitelist; // should there be a size for the whitelist?
    
    uint data;

    function set(uint value) public {
        data = value;
    }

    function get() public view returns (uint) {
        return data;
    }

    function connectionAllowedIpv4(bytes32 sourceEnodeHigh, bytes32 sourceEnodeLow, bytes16 sourceEnodeIpv4, uint16 sourceEnodePort) view public returns (bool){
        require (sourceEnodePort < 99999);
        Enode memory source = Enode(sourceEnodeHigh, sourceEnodeLow, sourceEnodeIpv4, sourceEnodePort);
        Enode storage whitelistSource = whitelist[sourceEnodePort];
        if (whitelistSource.enodeHigh == source.enodeHigh) {
            return true;
        }
    }
    function addEnodeIpv4(bytes32 enodeHigh, bytes32 enodeLow, bytes16 enodeIpv4, uint16 enodePort) public {
        Enode memory newEnode = Enode(enodeHigh, enodeLow, enodeIpv4, enodePort);
        whitelist[enodePort] = newEnode;
    }
    function removeEnodeIpv4(bytes32 enodeHigh, bytes32 enodeLow, bytes16 enodeIpv4, uint16 enodePort) public {
        //Enode memory newEnode = Enode(enodeHigh, enodeLow, enodeIpv4, enodePort);
        Enode memory zeros = Enode(bytes32(0), bytes32(0), bytes16(0), 0);
        whitelist[enodePort] = zeros;
    }
    function test32(bytes32 x) pure public returns (bool) {
        return true;
    }
    function test16(bytes16 x) pure public returns (bool) {
        return true;
    }
}
