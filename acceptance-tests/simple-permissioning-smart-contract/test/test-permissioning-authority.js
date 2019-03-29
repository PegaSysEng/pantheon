const TestPermissioning = artifacts.require('PermissioningWithAuthority.sol');
var proxy;

var node1High = "0x9bd359fdc3a2ed5df436c3d8914b1532740128929892092b7fcb320c1b62f375";
var node1Low = "0x892092b7fcb320c1b62f3759bd359fdc3a2ed5df436c3d8914b1532740128929";
var node1Host = "0x9bd359fd";
var node1Port = 30303;

var originalAdmin = "627306090abaB3A6e1400e9345bC60c78a8BEf57".toLowerCase();
var newAdmin = "f17f52151EbEF6C7334FAD080c5704D77216b732".toLowerCase();
var newAdmin2 = "fe3b557e8fb62b89f4916b721be55ceb828dbd73".toLowerCase();

contract('Authority ', () => {
  describe('Function: Authority checks', () => {
    it('add an admin and check if they are authorized', async () => {
      proxy = await TestPermissioning.new();
      
      let resultAdmins = await proxy.getAllAdmins();
      console.log(resultAdmins);
      assert.equal(resultAdmins[0].toLowerCase(), '0x' + originalAdmin, 'expected first admin to be the deployer');
      assert.equal(resultAdmins.length, 1, 'expected 1 only');

      let result = await proxy.addAdmin(newAdmin);

      resultAdmins = await proxy.getAllAdmins();
      console.log(resultAdmins);
      assert.equal(resultAdmins[0].toLowerCase(), '0x' + originalAdmin, 'expected first admin to be the deployer');
      assert.equal(resultAdmins[1].toLowerCase(), '0x' + newAdmin, 'expected first added admin');
      assert.equal(resultAdmins.length, 2, 'expected 2 only');

      // add them a second time, result should be false
      result = await proxy.addAdmin(newAdmin);
      console.log(result);

      resultAdmins = await proxy.getAllAdmins();
      console.log(resultAdmins);
      assert.equal(resultAdmins[0].toLowerCase(), '0x' + originalAdmin, 'expected first admin to be the deployer');
      assert.equal(resultAdmins[1].toLowerCase(), '0x' + newAdmin, 'expected first added admin');
      assert.equal(resultAdmins.length, 2, 'expected 2 only - second add should have failed');

      result = await proxy.isAuthorized(newAdmin);
      assert.equal(result, true, 'expected new admin to be authorized');

      result = await proxy.removeAdmin(newAdmin);
      result = await proxy.isAuthorized(newAdmin);
      assert.equal(result, false, 'expected disallowed since admin was removed');

      resultAdmins = await proxy.getAllAdmins();
      console.log(resultAdmins);
      assert.equal(resultAdmins[0].toLowerCase(), '0x' + originalAdmin, 'expected first admin to be the deployer');
      assert.equal(resultAdmins.length, 1, 'expected 1 only');
    });

    it('enter read only mode and expect add to fail', async () => {
      await proxy.enterReadOnly();

      // add node should fail in read only mode
      try {
        await proxy.addEnodeIpv4(node1High, node1Low, node1Host, node1Port);
      } catch (err) {
        assert(true, err.toString().includes('revert'), 'expected revert in message');
         await proxy.exitReadOnly();
        return;
      }

      assert(false, 'did not catch expected error ');
    });

    it('attempt to exit read only mode and expect to fail', async () => {

      // exit read only mode should fail in normal mode
      try {
        await proxy.exitReadOnly();
      } catch (err) {
        assert(true, err.toString().includes('revert'), 'expected revert in message');
        return;
      }

      assert(false, 'did not catch expected error ');
    });

    it('get all admins', async () => {
      proxy = await TestPermissioning.new();
      let result = await proxy.addAdmin(newAdmin);
      await proxy.addAdmin(newAdmin2);

      result = await proxy.isAuthorized(newAdmin);
      assert.equal(result, true, 'expected new admin to be authorized');

      let resultAdmins = await proxy.getAllAdmins();
      console.log(resultAdmins);
      assert.equal(resultAdmins[0].toLowerCase(), '0x' + originalAdmin, 'expected first admin to be the deployer');
      assert.equal(resultAdmins[1].toLowerCase(), '0x' + newAdmin, 'expected first added admin');
      assert.equal(resultAdmins[2].toLowerCase(), '0x' + newAdmin2, 'expected second added admin');
      assert.equal(resultAdmins.length, 3, 'expected 3 only');
    });
  });
});
