var SimplePermissioning = artifacts.require("SimplePermissioning");
var SimpleAccountPermissioning = artifacts.require("SimpleAccountPermissioning");

module.exports = function(deployer) {
  deployer.deploy(SimplePermissioning);
  deployer.deploy(SimpleAccountPermissioning);
};
