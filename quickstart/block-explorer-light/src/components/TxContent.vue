
<template>
<div class="block-content">
  <div class="flex-box">
    <div class="pair">
      <div class="label minW140">Hash</div>
      <TxHash :value="tx.hash" />
    </div>
    <div class="pair">
      <div class="tx-value-label value-grayed" v-if="tx.value_eth === 0">Value</div>
      <div class="value gray" v-if="tx.value_eth === 0">
        {{ this.numberWithCommas(tx.value_eth)}} ETH
      </div>
      <div class="tx-value-label" v-if="tx.value_eth !== 0">Value</div>
      <div class="value" v-if="tx.value_eth !== 0 && tx.value_eth">
        {{ this.numberWithCommas(tx.value_eth.toFixed(4))}} ETH
      </div>
    </div>
  </div>
  <div class="flex-box">
    <div class="pair">
      <div class="label minW140">Block</div>
      <BlockNumber :value="tx.blockNumber" :isLink="true"/>
    </div>
  </div>
  <div class="flex-box">
    <div class="pair">
      <div class="label minW140">Position</div>
      <div class="value">
        {{ tx.transactionIndex}}
      </div>
    </div>
    <div class="pair">
      <div class="label">Nonce</div>
      <div class="value">
        {{ this.numberWithCommas(tx.nonce)}}
      </div>
    </div>
  </div>
  <div class="flex-box">
    <div class="pair">
      <div class="label minW140">From</div>
      <AccountHash :value="tx.from" />
    </div>
    <div class="pair">
      <div class="label" v-if="tx.to !== null">To</div>
      <AccountHash :value="tx.to"  v-if="tx.to !== null"/>
    </div>
    <div class="pair">
      <div class="label" v-if="tx.receipt.contractAddress !== null">Creates</div>
      <AccountHash :value="tx.receipt.contractAddress"  v-if="tx.receipt.contractAddress !== null"/>
    </div>
  </div>
  <div class="flex-box separated">
    <div class="pair">
      <div class="label minW140">Gas limit</div>
      <div class="value">
        {{ this.numberWithCommas(tx.gas)}}
      </div>
    </div>
    <div class="pair">
      <div class="label">Gas price</div>
      <div class="value">
        {{ (tx.gasPrice / Math.pow(10, 18)).toFixed(9)}} ETH
      </div>
      <div class="value gray">
        {{ tx.gasPrice / Math.pow(10, 9)}} Gwei
      </div>
    </div>
  </div>
  <div class="flex-box">
    <div class="pair">
      <div class="label minW140">Gas used by tx</div>
      <div class="value" v-if="tx.receipt">
        {{ this.numberWithCommas(tx.receipt.gasUsed)}}
        <span class="percentage">{{((tx.receipt.gasUsed / tx.gas) * 100).toFixed(2)}}%</span>
      </div>
    </div>
    <div class="pair">
      <div class="label">Tx fee</div>
      <div class="value" v-if="tx.receipt">
        {{ ((tx.receipt.gasUsed * tx.gasPrice) / Math.pow(10, 18)).toFixed(9)}} ETH
      </div>
    </div>
  </div>
  <div class="flex-box">
    <div class="pair">
      <div class="label minW140">Cummulative gas used</div>
      <div class="value" v-if="tx.receipt">
        {{ this.numberWithCommas(tx.receipt.cumulativeGasUsed)}}
      </div>
    </div>
  </div>
</div>
</template>


<script>
import Vue from 'vue';
import HelpersMixin from '../../src/mixins/Helpers';

import Hash from '@/components/ui/Hash.vue';
import TxHash from '@/components/ui/TxHash.vue';
import AccountHash from '@/components/ui/AccountHash.vue';
import BlockNumber from '@/components/ui/BlockNumber.vue';
import AddressLink from '@/components/ui/AddressLink.vue';
import TransactionLink from '@/components/ui/TransactionLink.vue';


export default {
  name: 'TxContent',
  props: {
    currentHash: String,
  },
  components: {
    AddressLink, TransactionLink, Hash, TxHash, AccountHash, BlockNumber,
  },
  mixins: [HelpersMixin],
  data() {
    return ({
      tx: {},
      tx_uid: '',
      events: [],
    });
  },
  watch: {
    async currentHash() {
      if (this.currentHash != '') {
        const context = this;
        // We save a UUID to prevent old callbacks from also giving their results
        context.tx_uid = this.lib_UID();
        const uid = context.tx_uid;

        // We get the block to iterate all his transactions
        context.lib_getTransaction(context.currentHash, true, (err, tx) => {
          console.log(tx);
          context.tx = tx;
        });
      }
    },
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
span {
  display: inline-block;
}
</style>
