<template>
<div v-if="value != null" class="value">
  <div class="top-tooltip-container">
    <div class="arrow"></div>
    <div class="top-tooltip">
      {{value}}
    </div>
  </div>
  <div  v-on:click="showTimeAgo = !showTimeAgo" class="tooltip-target cursor" >
    <Timeago v-if="showTimeAgo" :datetime="value * 1000" />
    <span v-if="!showTimeAgo">{{value | timestampToText}}</span>
  </div>
</div>
</template>

<script>
export default {
  name: 'TimeStamp',
  props: {
    value: Number,
  },
  components: {

  },
  data() {
    return ({
      showTimeAgo: true,
    });
  },
  filters: {
    timestampToText(timestamp) {
      var d = new Date(timestamp * 1000);
      return (d.toLocaleString());
    },
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

.cursor {
  cursor: pointer;
}
.value {
  position: relative;
}

.value:hover .top-tooltip-container {
  display: block;
}

.top-tooltip-container {
  display: none;
  padding: 2px 0 8px;
  position: absolute;
  top: -28px;
  left: 0;
  right: 0;
}
.top-tooltip {
  text-align: center;
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
