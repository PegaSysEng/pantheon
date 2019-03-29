var Ingress = artifacts.require("Ingress");
var SimplePermissioning = artifacts.require("SimplePermissioning");
module.exports = function(deployer) {
  deployer.deploy(Ingress);
  deployer.deploy(SimplePermissioning);
};
