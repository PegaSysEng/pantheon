const TestPermissioning = artifacts.require('SimplePermissioning.sol');
var proxy;
var b16 = "0x9bd359fdc3a2ed5df436c3d8914b1532";
var b162 = "0x596c3d8914b1532fdc3a2ed5df439bd3";
var b32 = "0x9bd359fdc3a2ed5df436c3d8914b1532740128929892092b7fcb320c1b62f375";
var b322 = "0x892092b7fcb320c1b62f3759bd359fdc3a2ed5df436c3d8914b1532740128929";
var port1 = 30303;
var port2 = 30304;

contract('Permissioning Ipv6', () => {
  describe('Function: permissioning Ipv6', () => {

    it('Should NOT permit any node when none have been added', async () => {
      proxy = await TestPermissioning.new();
      let permitted = await proxy.enodeAllowedIpv6(b32, b32, b16, port1);
      assert.equal(permitted, false, 'expected node NOT permitted');
    });

    it('Should compute key', async () => {
      let key1 = await proxy.computeKeyIpv6(b32, b32, b16, port1);
      let key2 = await proxy.computeKeyIpv6(b32, b32, b16, port1);
      assert.equal(key1, key2, "computed keys should be the same");

      let key3 = await proxy.computeKeyIpv6(b32, b32, b16, port2);
      assert(key3 != key2, "keys for different ports should be different");
    });

    it('Should add a node to the whitelist and then permit that node', async () => {
      await proxy.addEnodeIpv6(b32, b32, b16, port1);
      let permitted = await proxy.enodeAllowedIpv6(b32, b32, b16, port1);
      assert.equal(permitted, true, 'expected node added to be permitted');
 
      // await another
      await proxy.addEnodeIpv6(b32, b322, b162, port2);
      permitted = await proxy.enodeAllowedIpv6(b32, b322, b162, port2);
      assert.equal(permitted, true, 'expected node 2 added to be permitted');
 
      // first one still permitted
      permitted = await proxy.enodeAllowedIpv6(b32, b32, b16, port1);
      assert.equal(permitted, true, 'expected node 1 added to be permitted');
    });

    it('Should allow a connection between 2 added nodes', async () => {
      let permitted = await proxy.connectionAllowedIpv6(b32, b32, b16, port1, b32, b322, b162, port2);
      assert.equal(permitted, true, 'expected 2 added nodes to work as source <> destination');
    });

    it('Should remove a node from the whitelist and then NOT permit that node', async () => {
      await proxy.removeEnodeIpv6(b32, b32, b16, port1);
      let permitted = await proxy.enodeAllowedIpv6(b32, b32, b16, port1);
      assert.equal(permitted, false, 'expected removed node NOT permitted');
    });

  });
});
