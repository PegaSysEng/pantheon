const PrivateKeyProvider = require('truffle-hdwallet-provider');

const localDev = 'http://127.0.0.1:8545';
const mnemonic = "candy maple cake sugar pudding cream honey rich smooth crumble sweet treat"; // 12 word mnemonic

const privateKeyProvider = new PrivateKeyProvider(mnemonic, localDev);

module.exports = {
  networks: {
    development: {
      host: '127.0.0.1',
      port: 7545,
      network_id: '*',
    },
    devwallet: {
      provider: privateKeyProvider,
      network_id: '2018',
    },
  },
};
