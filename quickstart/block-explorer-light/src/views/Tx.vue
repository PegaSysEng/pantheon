<template>
<div id="block" class="columns is-fullheight is-mobile flex-box">
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
  <div class="column is-3 sidebar blockSidebar tx-sideBar">
    <TxSidebarNav :currentHash="currentTx"  />
  </div>
  <div class="column is-9 content">
    <TxContent :currentHash="currentTx" />
  </div>
</div>
</template>

<script>
import TxSidebarNav from '@/components/TxSidebarNav.vue';
import TxContent from '@/components/TxContent.vue';
import IconBase from '@/components/icons/IconBase.vue';
import IconSearch from '@/components/icons/IconSearch.vue';
import IconAlethio from '@/components/icons/IconAlethio.vue';

export default {
  name: 'block',
  components: {
    TxSidebarNav,
    TxContent,
    IconBase,
    IconSearch,
    IconAlethio,
  },
  data() {
    return ({
      currentTx: '',
    });
  },
  mounted() {
    this.currentTx = this.$route.params.hash;
    this.checkIfExist();
  },
  watch: {
    $route(to, from) {
      this.currentTx = this.$route.params.hash;
      this.checkIfExist();
    },
  },
  methods: {
    checkIfExist() {
      if (this.currentTx != null && this.currentTx !== '') {
        var context = this;
        this.lib_getTransaction(this.currentTx, false, function(err, tx) {
          if (err || tx == null) {
            context.$router.push({
              path: `/404`,
            });
          }
        })
      }
    }
  }

};
</script>
