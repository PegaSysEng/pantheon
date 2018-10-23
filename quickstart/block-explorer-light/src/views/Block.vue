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
  <div class="column is-3 sidebar blockSidebar">
    <BlockSidebarNav :currentBlock="currentBlock" v-on:block_selected="lib_goToBlock($event)" />
  </div>
  <div class="column is-9 content">
    <BlockContent :currentBlock="currentBlock" />
  </div>
</div>
</template>

<script>
import BlockSidebarNav from '@/components/BlockSidebarNav.vue';
import BlockContent from '@/components/BlockContent.vue';
import IconBase from '@/components/icons/IconBase.vue';
import IconSearch from '@/components/icons/IconSearch.vue';
import IconAlethio from '@/components/icons/IconAlethio.vue';

export default {
  name: 'block',
  components: {
    BlockSidebarNav,
    BlockContent,
    IconBase,
    IconSearch,
    IconAlethio,
  },
  data() {
    return ({
      currentBlock: -1,
    });
  },
  mounted() {
    this.currentBlock = parseInt(this.$route.params.blocknumber);
    this.checkIfExist();
  },
  watch: {
    $route(to, from) {
      this.currentBlock = parseInt(this.$route.params.blocknumber);
      this.checkIfExist();
    },
  },
  methods: {
    checkIfExist() {
      if (this.currentBlock != -1) {
        var context = this;
        this.lib_getBlock(this.currentBlock, function(err, block) {
          if (err || block == null) {
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
