/* global artifacts contract describe assert it */

const TestPermissioning = artifacts.require('SimplePermissioning.sol');
var proxy;
var b16 = "0x9bd359fdc3a2ed5df436c3d8914b1532";
var b32 = "0x9bd359fdc3a2ed5df436c3d8914b1532740128929892092b7fcb320c1b62f375";
var port = 30303;

contract('Permissioning 1', () => {
  describe('Function: permissioning 1', () => {
    it('Should parse bytes16', async () => {
      proxy = await TestPermissioning.new();
      await proxy.test16(b16);

      assert(true, 'expected test 16 bytes');
    });

    it('Should add a node to the whitelist and then permit that node', async () => {
      await proxy.addEnodeIpv4(b32, b32, b16, port);
      let permitted = await proxy.connectionAllowedIpv4(b32, b32, b16, port);
      assert.equal(permitted, true, 'expected node added to be permitted');
 
      // await another
      await proxy.addEnodeIpv4(b32, b32, b16, port + 1);
      permitted = await proxy.connectionAllowedIpv4(b32, b32, b16, port + 1);
      assert.equal(permitted, true, 'expected node 2 added to be permitted');
 
      // first one still permitted
      permitted = await proxy.connectionAllowedIpv4(b32, b32, b16, port);
      assert.equal(permitted, true, 'expected node 1 added to be permitted');
    });

    it('Should remove a node from the whitelist and then NOT permit that node', async () => {
      await proxy.removeEnodeIpv4(b32, b32, b16, port);
      let permitted = await proxy.connectionAllowedIpv4(b32, b32, b16, port);
      assert.equal(permitted, false, 'expected removed node NOT permitted');
    });

    it('Should catch an error and then return', async () => {
        try {
          await proxy.test16(0x9bd359fdc3a2ed5df436c3d8914b1532);
        } catch (err) {
          assert(true, err.toString().includes('revert'), 'expected revert in message');

          return;
        }

      assert(false, 'did not catch expected error ');
    });

  });
});
