<template>
  <div class="wrapper">
    <div class="top-tooltip-container">
      <div class="arrow"></div>
      <div class="top-tooltip">
          {{value}}
      </div>
    </div>
    <div v-if="value != null" class="hash">
        <div class="tooltip-target">{{ value | cleanHash }}</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TxHash',
  props: {
    value: String,
  },
  components: {

  },
  filters: {
    cleanHash(string) {
      if (string == null) { return ''; }
      string = string.toLowerCase();
      const midpoint = Math.ceil(string.length / 2);
      const toremove = string.length - 12;
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


.hash {
  background-color: rgb(39, 54, 86);
  color: rgb(255, 255, 255);
  padding: 1px 8px 3px;
  font-size: 20px;
  letter-spacing: 0.2px;
  line-height: 24px;
  font-weight: 500;
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
    left: -185px;
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
