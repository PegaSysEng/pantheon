<template>
<div class="erc-counter">
  {{this.numberWithCommas(transfers)}}
</div>
</template>

<script>
import HelpersMixin from '../../../src/mixins/Helpers';

export default {
  name: 'ERC20Transfers',
  data() {
    return ({
      transfers: 0,
      transfers_uid: '',
    });
  },
  mixins: [HelpersMixin],
  props: {
    block: Object,
  },
  watch: {
    // When the block changes we go through all it's
    // transactions receipts and events to check if it's a ERC20 transfer
    block() {
      const context = this;
      context.transfers = 0;
      context.transfers_uid = this.lib_UID(); // We store a UUID for the
      // current set of request so async callbacks don't polute the result table
      const uid = context.transfers_uid;
      for (let i = 0; i < context.block.transactions.length; i++) {
        context.lib_getTransaction(context.block.transactions[i], true, (err, tx) => {
          if (tx.receipt != null && tx.receipt.logs != null) {
            for (let j = 0; j < tx.receipt.logs.length; j++) {
              if (uid === context.transfers_uid && tx.receipt.logs[j].topics[0] === '0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef') {
                context.transfers += 1;
              }
            }
          }
        });
      }
    },
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>


</style>
