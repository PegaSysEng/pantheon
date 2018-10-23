<template>
<div id="app" class="is-fullheight">
  <!--<div class="column has-text-right top-menu ">-->
    <!--<div class="top-menu-item-link label">-->
      <!--Best block:-->
    <!--</div>-->
    <!--<BlockNumber :value="lastBlock" :isLink="true"/>-->
    <!--<div  v-on:click="lib_openSearch()" class="top-menu-item-link button search-btn" style="margin-right:5px;">-->
      <!--Search-->
    <!--</div>-->
  <!--</div>-->

  <router-view/>
</div>
</template>
<style>
  .icon {
    fill: #00d1b2 !important;
  }
  .icon:hover {
    fill: #0a0a0a !important;
  }
html,
body {
  height: 100%;
}

.is-fullheight {
  height: 100%;
  min-height: max-content;
}

.sidebar {
  min-height: 100%;
  background-color: rgb(255, 255, 255);
  border-right: 1px solid #D0DEF2;
}

.content {
  min-height: 100%;
  background-color: rgb(247, 248, 250);
}

.tooltip-inner {
  background: #FFF;
  color: black;
  padding: 4px;
  border-radius: 1px;
  border-color: rgb(208, 222, 242);
  border-style: solid;
  border-width: 1px;
}

.top-menu-item-link {
  cursor: pointer;
}
.search-btn {
  margin-left: 28px;
}
  .button {
    border-radius: 4px;
    transition: background-color 0.2s ease-in-out 0s, border-color 0.2s ease-in-out 0s;
    font-weight: 700;
    box-sizing: border-box;
    vertical-align: middle;
    background-color: rgb(167, 181, 209);
    color: rgb(255, 255, 255);
    padding: 7px 16px 8px;
    margin-top: 2px;
    border-width: 1px;
    border-style: solid;
    text-transform: uppercase;
    font-size: 12px;
    line-height: 14px;
  }
  .button:hover {
    background-color: #357CFF;
  }
</style>

<script>
import BlockNumber from '@/components/ui/BlockNumber.vue';
import IconBase from '@/components/icons/IconBase.vue';
import IconSearch from '@/components/icons/IconSearch.vue';

export default {
  components: {
    BlockNumber,
    IconBase,
    IconSearch,
  },
  mounted() {
    // We start here the watcher for last blocks
    this.lib_monitorLastBlock();
  },
  computed: {
    lastBlock() {
      return (this.$store.state.lastBlock);
    },
  },

};
</script>
