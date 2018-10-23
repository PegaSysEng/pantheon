<template>
<router-link :to="{ name: 'tx', params: { hash: transaction }}">
  {{ transaction | cleanHash }}
</router-link>
</template>

<script>
export default {
  name: 'TransactionLink',
  props: {
    transaction: String,
  },
  components: {

  },
  filters: {
    cleanHash(string) {
      string = string.toLowerCase();
      const midpoint = Math.ceil(string.length / 2);
      const toremove = string.length - 10;
      const lstrip = Math.ceil(toremove / 2);
      const rstrip = toremove - lstrip;
      return (`${string.substring(0, midpoint - lstrip)} ... ${
        string.substring(midpoint + rstrip)}`);
    },
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>


</style>
