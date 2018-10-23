<template>
  <div class="wrapper">
    <div class="top-tooltip-container">
      <div class="arrow"></div>
      <div class="top-tooltip">
        {{value}}
      </div>
    </div>
    <div v-if="value != null" class="hash">
      <a v-on:click="lib_goToBlock(number)" class="tooltip-target">{{ value | cleanHash }}</a>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BlockHash',
  props: {
    value: String,
    number: Number,
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

.tooltip-target {
  color: white;
}

.hash {
  cursor: pointer;
  background-color: rgb(167, 181, 209);
  color: rgb(255, 255, 255);
  padding: 1px 8px 3px;
  font-size: 20px;
  line-height: 24px;
  font-weight: 500;
  letter-spacing: 0.2px;
}

.wrapper {
  padding: 10px 8px;
  position: relative;
}

.wrapper:hover .top-tooltip-container {
  display: block;
}

.top-tooltip-container {
  display: none;
  padding: 2px 0 8px;
  position: absolute;
  top: -28px;
  left: -200px;
}
.top-tooltip {
  background-color: white;
  font-weight: 500;
  font-size: 16px;
  line-height: 19px;
  color: #273656;
  padding: 4px 16px;
  box-shadow: 0 2px 6px 0 rgba(0,0,0,0.04);
}
.arrow {
  position: absolute;
  bottom: 4px;
  left: calc(50% - 4px);
  width: 8px;
  height: 8px;
  transform: rotate(45deg);
  background-color: white;
}


</style>
