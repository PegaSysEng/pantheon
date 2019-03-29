const TestNameRegistry = artifacts.require('Ingress.sol');
var proxy;

var name1 = "authority";
var address1 = "0x0000000000000000000000000000000000001234";

var name2 = "rules";
var address2 = "0x0000000000000000000000000000000012345678";

contract('Ingress', () => {
  describe('Function: register a name and retrieve name', () => {
    
    it('What happens when I call getContractDetails for something not registered', async () => {
      proxy = await TestNameRegistry.new();

      let actualInfo = await proxy.getContractDetails(name1);
      assert.equal(actualInfo[0], 0, 'expected 0 when not registered');
      assert.equal(actualInfo[1], 0, 'expected ver 0 when not registered');
    });

    it('Should allow registration of contract', async () => {
      proxy = await TestNameRegistry.new();
      let success = await proxy.registerName(name1, address1, 1);

      let actualInfo = await proxy.getContractDetails(name1);
      assert.equal(actualInfo[0], address1, 'expected to retrieve at this address');
      assert.equal(actualInfo[1], 1, 'expected ver 1');
    });

    it('Should allow registration of a new version of contract', async () => {
      proxy = await TestNameRegistry.new();
      let success = await proxy.registerName(name2, address2, 1);

      let actualInfo = await proxy.getContractDetails(name2);
      assert.equal(actualInfo[0], address2, 'expected to retrieve at this address');
      assert.equal(actualInfo[1], 1, 'expected ver 1');

      // deploy v2
      success = await proxy.registerName(name2, address2, 2);

      actualInfo = await proxy.getContractDetails(name2);
      assert.equal(actualInfo[0], address2, 'expected to retrieve at this address');
      assert.equal(actualInfo[1], 2, 'expected ver 2');
    });

  });
});
