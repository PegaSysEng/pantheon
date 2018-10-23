
<template>
  <div>
    <div class="tx-logo-container">
      <div class="tx-logo">
        <div class="tx-logo-bg">Tx</div>
      </div>
    </div>
    <GridView v-on:item_clicked="lib_goToTx($event)"
              v-bind:elements="transactions"
              :maximum="maximum"
    />
  </div>
</template>


<script>
import GridView from '@/components/ui/GridView/GridView.vue';
import Vue from 'vue';

export default {
  name: 'TxSidebarNav',
  props: {
    currentHash: String,
  },
  components: {
    GridView,
  },
  data() {
    return ({
      tx: {},
      blockNumber: -1,
      transactions: [],
      maximum: -Infinity,
    });
  },
  watch: {
    async currentHash() {
      if (this.currentHash != '') {
        const context = this;
        context.transactions = [];
        // We save a UUID to prevent old callbacks from also giving their results
        context.tx_uid = this.lib_UID();
        const uid = context.tx_uid;
        context.lib_getTransaction(context.currentHash, true, (err, tx) => {
          context.tx = tx;
          context.blockNumber = tx.blockNumber;
          context.maximum = -Infinity;
          context.lib_getBlock(context.blockNumber, (err, block) => {
            for (var i = 0; i < block.transactions.length; i++) {
              context.transactions.push({
                id: block.transactions[i],
                value: 0,
                selected: block.transactions[i] == context.currentHash,
              });
            }
            for (var i = 0; i < block.transactions.length; i++) {
              context.lib_getTransaction(block.transactions[i], true, (err, tx) => {
                if (uid == context.tx_uid) { // safeguard as we are not synchronous
                  if (tx.value_eth > context.maximum) {
                    context.maximum = tx.value_eth;
                  }
                  Vue.set(context.transactions[tx.transactionIndex], 'value', tx.value_eth);
                }
              });
            }
          });
        });
      }
    },
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.tx_box {
  background-color: rgb(39, 54, 86);
  width: 65px;
  height: 65px;
  padding: 10px 0;
  border-radius: 50%;
  text-align: center;
  margin-top: 30%;
  margin-bottom: 40%;
}

.tx_box_text {
  color: rgb(255, 255, 255);
  font-size: 30px;
  font-weight: 700;
}
</style>
