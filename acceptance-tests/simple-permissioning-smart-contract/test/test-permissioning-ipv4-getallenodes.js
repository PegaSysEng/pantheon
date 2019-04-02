const TestPermissioning = artifacts.require('PermissioningWithAuthority.sol');
var proxy;

var node1High = "0x9bd359fdc3a2ed5df436c3d8914b1532740128929892092b7fcb320c1b62f375";
var node1Low = "0x2e1092b7fcb320c1b62f3759bd359fdc3a2ed5df436c3d8914b1532740128929";
var node1Host = "0x9bd359fd";
var node1Port = 1;

var node2High = "0x892092b7fcb320c1b62f3759bd359fdc3a2ed5df436c3d8914b1532740128929";
var node2Low = "0xcb320c1b62f37892092b7f59bd359fdc3a2ed5df436c3d8914b1532740128929";
var node2Host = "0x9bd359fd";
var node2Port = 2;

var node3High = "0x765092b7fcb320c1b62f3759bd359fdc3a2ed5df436c3d8914b1532740128929";
var node3Low = "0x920982b7fcb320c1b62f3759bd359fdc3a2ed5df436c3d8914b1532740128929";
var node3Host = "0x7fc359fd";
var node3Port = 3;

var nodes = [[0,0,0,0],
              [node1High, node1Low, node1Host, node1Port],
              [node2High, node2Low, node2Host, node2Port],
              [node3High, node3Low, node3Host, node3Port]];

var newAdmin = "f17f52151EbEF6C7334FAD080c5704D77216b732";

contract('Permissioning Ipv4 WITH AUTHORITY ', () => {
  describe('Function: Permissioning Ipv4 + Authority - iterate through all added enodes', () => {
    it('Should NOT permit any node when none have been added', async () => {
      proxy = await TestPermissioning.new();
      try {
        let permitted = await proxy.enodeAllowedIpv4(node1High, node1Low, node1Host, node1Port);
        assert.equal(permitted, false, 'expected node NOT permitted');
        // get head when no nodes in list should fail
        await proxy.getHeadEnodeIpv4();
      } catch (err) {
        assert(true, err.toString().includes('revert'), 'expected revert in message');
        return;
      }
      assert(false, 'did not catch expected error from getHeadEnode() when no nodes in whitelist');
    });

    it('Should add a node to the whitelist and then permit that node', async () => {
      // add node1
      await proxy.addEnodeIpv4(node1High, node1Low, node1Host, node1Port);

      let result = await proxy.getHeadEnodeIpv4();

      assert.equal(result[2], node1High, 'expected high node1');
      assert.equal(result[3], node1Low, 'expected low node1');
      assert.equal(result[4], node1Host, 'expected host node1');
      assert.equal(result[5], node1Port, 'expected port node1');
      assert.equal(result[0], result[1], 'for node1 expected next == prev when only one node added');

      // add node2
      await proxy.addEnodeIpv4(node2High, node2Low, node2Host, node2Port);
      // add node3
      await proxy.addEnodeIpv4(node3High, node3Low, node3Host, node3Port);

      result = await proxy.getHeadEnodeIpv4();

      let key = result[0];
      let foundNode1, foundNode2, foundNode3 = false;

      let i = 0;
      let originalKey = key;
      while (i<9 ) {
        result = await proxy.getEnodeIpv4(key);

        // assert the values match the nodes array
        assert.equal(result[2], nodes[result[5]][0], 'expected high node' + result[5]);
        assert.equal(result[3], nodes[result[5]][1], 'expected low node' + result[5]);
        assert.equal(result[4], nodes[result[5]][2], 'expected host node' + result[5]);
        if (result[2] == node1High) {
          foundNode1 = true;
        }
        if (result[2] == node2High) {
          foundNode2 = true;
        }
        if (result[2] == node3High) {
          foundNode3 = true;
        }

        key = result[0];
        i++;
        if (key == originalKey) {
          break;
        }
      }
      assert.equal(i, 3, 'expected 3 values');
      assert.equal(foundNode1, true, 'expected to find node1');
      assert.equal(foundNode2, true, 'expected to find node2');
      assert.equal(foundNode3, true, 'expected to find node3');
    });

    it('Should remove a node from the whitelist and then NOT find it in the list', async () => {
      result = await proxy.getHeadEnodeIpv4();
      let key = result[0];

      // remove node3
      result = await proxy.removeEnodeIpv4(node1High, node1Low, node1Host, node1Port);
      
      result = await proxy.getHeadEnodeIpv4();
      key = result[0];
      let foundNode1 = false;
      let foundNode2 = false;
      let foundNode3 = false;

      let i = 0;
      let originalKey = key;
      while (i<9) {
        result = await proxy.getEnodeIpv4(key);

        // assert the values match the nodes array
        assert.equal(result[2], nodes[result[5]][0], 'expected high node' + result[5]);
        assert.equal(result[3], nodes[result[5]][1], 'expected low node' + result[5]);
        assert.equal(result[4], nodes[result[5]][2], 'expected host node' + result[5]);
        if (result[2] == node1High) {
          foundNode1 = true;
        }
        if (result[2] == node2High) {
          foundNode2 = true;
        }
        if (result[2] == node3High) {
          foundNode3 = true;
        }

        key = result[0];
        i++;
        if (key == originalKey) {
          break;
        }
      }
      assert.equal(foundNode1, false, 'expected to NOT find node1');
      assert.equal(foundNode2, true, 'expected to find node2');
      assert.equal(foundNode3, true, 'expected to find node3');
      assert.equal(i, 2, 'expected 2 values');
    });

  });
});
