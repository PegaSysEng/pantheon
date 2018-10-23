<template>
<section class="home is-fullheight">
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
  <div class="error-container">
    <icon-base icon-name="error"
               width="48"
               height="48"
               viewBox="2 2 20 20"
    >
      <icon-error />
    </icon-base>
    <div class="error-text">
      <div>Looks like the resource you are looking for does not exist.</div>
      <div>Try searching for a different one.</div>
    </div>
  </div>
</section>
</template>

<script>

import IconBase from '@/components/icons/IconBase.vue';
import IconError from '@/components/icons/IconError.vue';
import IconAlethio from '@/components/icons/IconAlethio.vue';
import IconSearch from '@/components/icons/IconSearch.vue';

export default {
  name: 'NotFounf',
  components: {
    IconBase,
    IconError,
    IconAlethio,
    IconSearch,
  },
  data() {
    return ({
    });
  },
  mounted() {
    this.currentBlock = parseInt(this.$route.params.blocknumber);
  },
  watch: {

  },
  computed: {
    lastBlock() {
      return (this.$store.state.lastBlock);
    },
  },

};
</script>
