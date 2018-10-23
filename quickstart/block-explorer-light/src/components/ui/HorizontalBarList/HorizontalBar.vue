<template>
  <div class="elem"
       :style="{ 'width': percent  + '%', 'height': '8px'}"
       v-on:click="$emit('item_clicked')">
    <div class="tooltip-container">
      <div class="tooltip">
        <div class="arrow"></div>
        <div class="flex-box">
          <div class="block-number">{{label}}</div>
          <div class="tx-number">{{value}} TXs</div>
        </div>
      </div>
    </div>
  </div>
</template>


<script>
export default {
  name: 'HorizontalBar',
  props: {
    percent: Number,
    label: String,
    value: Number,
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

.hidden {
  visibility: hidden;
  display: none;
}

.elem {
  position: relative;
  margin-top: 8px;
  min-width: 8px;
  height: 8px;
  border: 2px;
  border-radius: 1px;
  background-color: #D0DEF2;
  border-color: #D0DEF2;
  border-style: solid;
  max-width: 72px;
  box-sizing: border-box;
  overflow: visible;
}

.elem:hover {
  border-color: rgb(53, 124, 255);
  background-color: rgb(255, 255, 255);
  cursor:pointer;
}
.elem:hover .tooltip-container {
  display: block;
}

.selected {
  background-color: rgb(53, 124, 255);
  border-color: rgb(53, 124, 255);
}
  .tooltip-container {
    display: none;
    position: absolute;
    top: -16px;
    right: -170px;
    width: max-content;
    height: 36px;
    padding-left: 20px;
    z-index: 100;
    overflow: visible;
  }
  .tooltip {
    padding: 8px;
    background-color: white;
    -webkit-box-shadow: 0px 0px 20px -5px rgba(0,0,0,0.44);
    -moz-box-shadow: 0px 0px 20px -5px rgba(0,0,0,0.44);
    box-shadow: 0px 0px 20px -5px rgba(0,0,0,0.44);
  }

  .block-number {
    padding: 1px 8px 2px;
    background-color: rgb(53, 124, 255);
    color: white;
    font-weight: 600;
    font-size: 14px;
    line-height: 17px;
    letter-spacing: 0.2px;
  }

  .tx-number {
    padding: 1px 8px 2px;
    background-color: #273656;
    color: white;
    font-weight: 600;
    font-size: 14px;
    line-height: 17px;
    letter-spacing: 0.2px;
  }
  .arrow {
    position: absolute;
    top: 14px;
    left: 16px;
    height: 8px;
    width: 8px;
    transform: rotate(45deg);
    background-color: white;
  }

</style>
