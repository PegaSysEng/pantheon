<template>
<div id="account" class="columns min100" >
  <div class="column is-5 flex-box min100">
    <div class="left-header">
      <div class="iconAlethio" v-on:click="lib_goHome()">
        <icon-base icon-name="alethio"
                   width="32"
                   height="32"
                   viewBox="2 24 24 24"
        >
          <icon-alethio />
        </icon-base>
      </div>
      <div class="search-icon-container">
        <div v-on:click="lib_openSearch()" class="iconSearch">
          <icon-base icon-name="search"
                     width="36"
                     height="36"
                     viewBox="-4 0 24 24"
          >
            <icon-search />
          </icon-base>
        </div>
      </div>
    </div>
    <div class="account-avatar-container">
      <div class="account-avatar">
        <div class="identicon-container">
          <HashAvatar v-if="currentAddress" :hash="currentAddress" />
        </div>
      </div>
    </div>
    <div class="account-content">
      <div class="flex-box">
        <div class="pair">
          <div class="label minW140">Address</div>
          <AccountHash :value="currentAddress" />
        </div>
        <div class="pair">
          <div class="label">Type</div>
          <div class="value" v-if="account.isContract">Contract account</div>
          <div class="value" v-if="account.isContract === false">External Account</div>
        </div>
      </div>
      <div class="flex-box">
        <div class="pair">
          <div class="label minW140">Balance</div>
          <div class="value" v-if="account.balance_eth || account.balance_eth === 0">{{ this.numberWithCommas(account.balance_eth.toFixed(4)) }} ETH</div>
        </div>
      </div>
      <div class="flex-box">
        <div class="label minW140" v-if="account.isContract">Account Code</div>
        <div class="account-code" v-if="account.isContract">{{account.code}}</div>
      </div>
    </div>
  </div>
</div>
</template>

<script>
import HashAvatar from '@/components/ui/HashAvatar.vue';
import AccountHash from '@/components/ui/AccountHash.vue';
import IconBase from '@/components/icons/IconBase.vue';
import IconSearch from '@/components/icons/IconSearch.vue';
import IconAlethio from '@/components/icons/IconAlethio.vue';
import HelpersMixin from '../../src/mixins/Helpers';

export default {
  name: 'account',
  components: {
    HashAvatar,
    AccountHash,
    IconBase,
    IconSearch,
    IconAlethio,
  },
  mixins: [HelpersMixin],
  data() {
    return ({
      currentAddress: '',
      account: {},
    });
  },
  mounted() {
    this.currentAddress = this.$route.params.address;
  },
  watch: {
    $route(to, from) {
      this.currentAddress = this.$route.params.address;
    },
    currentAddress(to, from) {
      this.account = {};
      const context = this;
      this.lib_getAccount(this.currentAddress, (err, account) => {
        context.account = account;
      });
    },
  },

};
</script>

<style scoped>

.code_block {
  display: block;
  word-wrap: break-word;
  height: 200px;
  max-height: 400px;
  white-space: pre-wrap;
}

</style>
