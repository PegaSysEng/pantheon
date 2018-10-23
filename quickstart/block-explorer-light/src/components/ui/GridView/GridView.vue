<template>
<div style="height:100%; width: 100%" class="hello txList">
  <div v-for="elem in elements">
    <div class="item_container" v-on:click="$emit('item_clicked', elem.id)">
      <div v-bind:class="{ item: true, selected: elem.selected }"
           :style="{} | computeSize(elem.value, maximum)">
      </div>
      <div class="tooltip-container">
        <div class="tooltip">
          <div class="arrow"></div>
          <div class="flex-box">
            <div class="tx-number">{{elem.id.substr(0, 6) + `...` + elem.id.substr(-6)}}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
</template>

<script>
export default {
  name: 'GridList',
  props: {
    elements: Array,
    maximum: Number,
  },
  watch: {
    metric(a, b) {
      console.log(a, b);
    },
  },
  filters: {
    computeSize(pipe, value, max) {
      return ({
        // margin: `${100 - ((value / max) * 100) / 2}%`,
        width: `${(value / max) * 100}%`,
        height: `${(value / max) * 100}%`,
      });
    },
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.item_container {
  position: relative;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  justify-content: space-around;
  align-items: center;

}

.item_container:hover {
  cursor: pointer;
}

.item_container:hover .item{
  border-color: rgb(39, 54, 86);
  background-color: rgb(255, 255, 255);
}


.item {
  border-radius: 50%;
  border: 2px;
  background-color: #D0DEF2;
  border-color: #D0DEF2;
  border-style: solid;
  box-sizing: border-box;
}

.item:hover {
  border-color: rgb(39, 54, 86);
  background-color: rgb(255, 255, 255);
  cursor: pointer;
}

.selected {
  background-color: rgb(39, 54, 86);
  border-color: rgb(39, 54, 86);
}

.tooltip-container {
  display: none;
  position: absolute;
  top: -5px;
  right: -145px;
  width: max-content;
  height: 36px;
  padding-left: 20px;
  z-index: 100;
  overflow: visible;
}

.item_container:hover .tooltip-container {
  display: block;
}

.tooltip {
  padding: 8px;
  background-color: white;
  -webkit-box-shadow: 0px 0px 20px -5px rgba(0,0,0,0.44);
  -moz-box-shadow: 0px 0px 20px -5px rgba(0,0,0,0.44);
  box-shadow: 0px 0px 20px -5px rgba(0,0,0,0.44);
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
