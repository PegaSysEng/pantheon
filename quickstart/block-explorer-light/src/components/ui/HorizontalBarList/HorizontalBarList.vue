<template>
  <div class="blocks-list">
    <div v-for="elem in elements" :key="elem.id" style="overflow: visible">
      <HorizontalBar :label="'# ' + elem.id"
                     :value="elem.value"
                     v-on:item_clicked="$emit('item_clicked', elem.id)"
                     v-bind:class="{ item: true, selected: elem.selected,
                     hidden:elem.value === -1 }"
                     :percent="(elem.value / maximumValue) * 100" >
      </HorizontalBar>
    </div>
  </div>
</template>

<script>
import HorizontalBar from '@/components/ui/HorizontalBarList/HorizontalBar.vue';

export default {
  name: 'HorizontalBarList',
  props: {
    elements: Array,
    maximumValue: Number,
  },
  components: {
    HorizontalBar,
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>


</style>
