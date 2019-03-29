var SimplePermissioning = artifacts.require("SimplePermissioning");
var PermissioningWithAuthority = artifacts.require("PermissioningWithAuthority");
module.exports = function(deployer) {
  deployer.deploy(SimplePermissioning);
  deployer.deploy(PermissioningWithAuthority);
};
