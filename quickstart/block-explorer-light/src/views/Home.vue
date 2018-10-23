<template>
<section class="home is-fullheight">
  <!--<div class="columns">-->
    <!--<div class="column has-text-centered">-->
    <!--<h1 class="title">hello there</h1>-->
    <!--<p>this is the lighweight block explorer in the future we can put some general graphs here...</p>-->
    <!--<p class="button" v-on:click="lib_goToBlock(lastBlock)">Go to last block</p>-->
    <!--</div>-->
    <!--<div class="column">-->
    <!--</div>-->
  <!--</div>-->
  <div class="centered-absolute">
    <div class="flex-box" style="align-items: center">
      <div class="label" v-if="nodeType">Type</div>
      <div class="value white-bg-value" v-if="nodeType">{{ nodeType.split('/')[0] }}</div>
      <div class="label" v-if="peers">Peers</div>
      <div class="value white-bg-value" v-if="peers">{{ peers }}</div>
      <div class="label">Best block</div>
      <BlockNumber :value="lastBlock" :isLink="true"/>
      <div class="label" v-if="lastBlockFull">Transactions</div>
      <div class="tx-counter" v-if="lastBlockFull">{{ lastBlockFull.transactions.length }}</div>
      <div class="label" v-if="lastBlockFull">Uncles</div>
      <div class="uncle-counter" v-if="lastBlockFull">{{ lastBlockFull.uncles.length }}</div>
    </div>
    <div class="home-search-container">
      <div class="flex-box">
        <div class="searchbar-alethio-logo">
          <icon-base icon-name="alethio"
                     width="24"
                     height="24"
                     viewBox="22 22 24 24"
          >
            <icon-alethio />
          </icon-base>
        </div>
        <input v-model="search" placeholder="Enter a block number, transaction hash or address." v-on:keyup.enter="lib_processSearch(search)"/>
      </div>
      <div v-on:click="lib_processSearch(search)" class="submit-container">
        <div class="searchbar-search-icon">
          <icon-base icon-name="search"
                     width="16"
                     height="16"
                     viewBox="4 4 16 16"
          >
            <icon-search />
          </icon-base>
        </div>
        <span class="search-text">Search</span>
      </div>
    </div>
  </div>
</section>
</template>

<script>
import BlockNumber from '@/components/ui/BlockNumber.vue';
import IconBase from '@/components/icons/IconBase.vue';
import IconSearch from '@/components/icons/IconSearch.vue';
import IconAlethio from '@/components/icons/IconAlethio.vue';

export default {
  name: 'home',
  components: {
    BlockNumber,
    IconBase,
    IconSearch,
    IconAlethio,
  },
  data() {
    return ({
      search: '',
    });
  },
  mounted() {

  },
  watch: {

  },
  computed: {
    lastBlock() {
      return (this.$store.state.lastBlock);
    },
    lastBlockFull() {
      return (this.$store.state.lastBlockFull);
    },
    peers() {
      return (this.$store.state.peers);
    },
    nodeType() {
      return (this.$store.state.nodeType);
    },
  },

};
</script>
