pragma solidity >=0.4.0 <0.6.0;


contract SimplePermissioning {
    struct EnodeIpv6 {
        bytes32 enodeHigh;
        bytes32 enodeLow;
        bytes16 enodeHost; // Ipv6
        uint16 enodePort;
    }
    mapping(bytes => EnodeIpv6) private whitelistIpv6; // should there be a size for the whitelist?

    function connectionAllowedIpv6(bytes32 sourceEnodeHigh, bytes32 sourceEnodeLow, bytes16 sourceEnodeIpv6, uint16 sourceEnodePort) public view returns (bool){
        require (sourceEnodePort > 0 && sourceEnodePort < 99999, "port out of range");
        bytes memory key = computeKey(sourceEnodeHigh, sourceEnodeLow, sourceEnodeIpv6, sourceEnodePort);
        EnodeIpv6 storage whitelistSource = whitelistIpv6[key];
        if (whitelistSource.enodeHigh == sourceEnodeHigh) {
            return true;
        }
    }
    function addEnodeIpv6(bytes32 enodeHigh, bytes32 enodeLow, bytes16 enodeIpv6, uint16 enodePort) public {
        EnodeIpv6 memory newEnode = EnodeIpv6(enodeHigh, enodeLow, enodeIpv6, enodePort);
        bytes memory key = computeKey(enodeHigh, enodeLow, enodeIpv6, enodePort);
        whitelistIpv6[key] = newEnode;
    }
    function removeEnodeIpv6(bytes32 enodeHigh, bytes32 enodeLow, bytes16 enodeIpv4, uint16 enodePort) public {
        bytes memory key = computeKey(enodeHigh, enodeLow, enodeIpv4, enodePort);
        EnodeIpv6 memory zeros = EnodeIpv6(bytes32(0), bytes32(0), bytes16(0), 0);
        whitelistIpv6[key] = zeros;
    }
    function computeKey(bytes32 enodeHigh, bytes32 enodeLow, bytes16 enodeIpv6, uint16 enodePort) public pure returns (bytes memory) {
        //return abi.encodePacked(enodeHigh, enodeLow, enodeIpv4, enodePort);
        return abi.encode(enodeHigh, enodeLow, enodeIpv6, enodePort);
    }
    function test32(bytes32 x) pure public returns (bool) {
        return true;
    }
    function test16(bytes16 x) pure public returns (bool) {
        return true;
    }
}
