<template>
<router-link :to="{ name: 'account', params: { address: address }}">
  {{ address | cleanHash }}
</router-link>
</template>

<script>
export default {
  name: 'AddressLink',
  props: {
    address: String,
  },
  components: {

  },
  filters: {
    cleanHash(string) {
      if (string == null) { return ''; }
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
