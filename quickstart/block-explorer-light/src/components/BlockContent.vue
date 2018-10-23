
<template>
    <div>
        <div class="block-content">
            <div class="flex-box">
                <div class="pair">
                    <div class="label minW140">Block</div>
                    <BlockNumber :value="block.number" />
                </div>
                <div class="pair">
                    <div class="label">Time</div>
                    <TimeStamp :value="block.timestamp" />
                </div>
            </div>
            <div class="flex-box">
                <div class="pair">
                    <div class="label minW140">Hash</div>
                    <Hash :value="block.hash" />
                </div>
                <div class="pair">
                    <div class="label">Parent</div>
                    <BlockHash :number="block.number - 1" :value="block.parentHash" />
                </div>
            </div>
            <div class="flex-box">
                <div class="pair">
                    <div class="label minW140">Nonce</div>
                    <div class="value">
                        {{ block.nonce }}
                    </div>
                </div>
                <div class="pair">
                    <div class="label">Size</div>
                    <div class="value">
                        {{ this.numberWithCommas(block.size) }} bytes
                    </div>
                </div>
            </div>
            <div class="flex-box">
                <div class="pair">
                    <div class="label minW140">Transactions</div>
                    <div class="counter-container">
                        <div class="tx-counter">
                            {{ block.transactions == null ? 0 :
                            this.numberWithCommas(block.transactions.length) }}
                        </div>
                    </div>
                </div>
                <div class="pair">
                    <div class="label">ERC20 Transfers</div>
                    <ERC20Transfers  v-bind:block="block" />
                </div>
            </div>
        </div>

        <div class="columns txs-table-container">
            <div class="column is-12">
                <b-table :data="transactions"
                         default-sort="transactionIndex"
                         default-sort-direction="asc"
                         :per-page="20" :paginated="true"
                         :pagination-simple="true"
                >
                    <template slot-scope="props">
                        <b-table-column
                                field="transactionIndex"
                                label="ID"
                                sortable
                                numeric
                                class="first-column"
                        >
                            {{ props.row.transactionIndex }}
                        </b-table-column>
                        <b-table-column
                                field="hash"
                                label="HASH"
                                sortable
                                class="normal-column"
                        >
                            <TransactionLink :transaction="props.row.hash" />
                        </b-table-column>
                        <b-table-column
                                field="from"
                                label="FROM"
                                sortable
                                class="normal-column"
                        >
                            <AddressLink :address="props.row.from" />
                        </b-table-column>
                        <b-table-column
                                field="to"
                                label="TO"
                                sortable
                                class="normal-column"
                        >
                            <AddressLink :address="props.row.to !== null ? props.row.to : props.row.receipt.contractAddress" />
                        </b-table-column>
                        <b-table-column
                                field="value_eth"
                                label="VALUE (ETH)"
                                sortable
                                class="last-column"
                        >
                            {{ props.row.value_eth.toFixed(4) }}
                        </b-table-column>
                    </template>
                    <template slot="empty">
                       <section class="section no-txs">
                           <div class="content has-text-grey has-text-centered">
                               <div class="value">Don't have data to show.</div>
                           </div>
                       </section>
                    </template>
                </b-table>
            </div>
        </div>
    </div>

</template>


<script>
import Vue from 'vue';
import HelpersMixin from '../../src/mixins/Helpers';

import Hash from '@/components/ui/Hash.vue';
import BlockHash from '@/components/ui/BlockHash.vue';
import BlockNumber from '@/components/ui/BlockNumber.vue';
import AddressLink from '@/components/ui/AddressLink.vue';
import TransactionLink from '@/components/ui/TransactionLink.vue';
import TimeStamp from '@/components/ui/TimeStamp.vue';

import ERC20Transfers from '@/components/plugins/ERC20Transfers.vue';

export default {
  name: 'BlockContent',
  props: {
    currentBlock: Number,
  },
  components: {
    AddressLink, TransactionLink, Hash, BlockHash, BlockNumber, TimeStamp, ERC20Transfers,
  },
  mixins: [HelpersMixin],
  data() {
    return ({
      block: {},
      transactions: [],
      transactions_uid: '',
    });
  },
  watch: {
    async currentBlock() {
      if (this.currentBlock !== -1) {
        const context = this;

        // We save a UUID to prevent old callbacks from also giving their results
        context.transactions_uid = this.lib_UID();
        const uid = context.transactions_uid;

        // We get the block to iterate all his transactions
        context.lib_getBlock(context.currentBlock, (err, block) => {
          context.block = block;
          const currentBlock = context.currentBlock;
          context.transactions = [];

          // For every individual transaction we fill the table.
          for (let i = 0; i < context.block.transactions.length; i++) {
            context.lib_getTransaction(context.block.transactions[i], true, (err, tx) => {
              if (tx != null && uid === context.transactions_uid) {
                context.transactions.push(tx);
              }
            });
          }
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
