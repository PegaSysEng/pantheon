
<template>
<div style="overflow: visible">
  <div class="block-logo-container">
    <div class="block-logo">
      <div class="block-logo-bg">
        <div class="block-logo-text">Bk</div>
      </div>
    </div>
  </div>
  <HorizontalBarList v-on:item_clicked="lib_goToBlock($event)" v-bind:elements="blocks" :maximumValue="maximum" />
</div>
</template>


<script>
import HorizontalBarList from '@/components/ui/HorizontalBarList/HorizontalBarList.vue';
import Vue from 'vue';

export default {
  name: 'BlockSidebarNav',
  props: {
    currentBlock: Number,
  },
  components: {
    HorizontalBarList,
  },
  data() {
    return ({
      range: parseInt((window.innerHeight - 280) / 32, 10),
      blocks: [],
      maximum: -Infinity,
    });
  },
  computed: {
    lastBlock() {
      return (this.$store.state.lastBlock);
    },
  },
  watch: {
    lastBlock() {
      if (this.lastBlock < this.currentBlock + this.range) {
        this.fillSidebebar();
      }
    },
    currentBlock() {
      this.fillSidebebar();
    },
  },
  methods: {
    fillSidebebar() {
      if (this.currentBlock !== -1) {
        this.$emit('block_selected', this.currentBlock);
        const tmp = [];
        for (let i = -this.range; i <= this.range; i++) {
          const block = this.$store.state.blocks[this.currentBlock + i];
          if (block != null) {
            if (this.maximum < block.transactions.length) {
              this.maximum = block.transactions.length;
            }
            tmp.push({
              id: this.currentBlock + i,
              value: block.transactions.length,
              selected: i === 0,
            });
          } else {
            tmp.push({
              id: this.currentBlock + i,
              value: -1,
              selected: i === 0,
            });
          }
        }
        this.blocks = tmp.reverse();
        const context = this;
        for (let i = 0; i < this.blocks.length; i++) {
          this.lib_getBlock(this.blocks[i].id, (err, block) => {
            if (block != null) {
              const index = context.currentBlock - block.number + context.range;
              // console.log(index, context.blocks[index].id, block.number, context.blocks[index].id == block.number)
              if (context.blocks[index].id === block.number) {
                Vue.set(context.blocks[index], 'value', block.transactions.length);
                if (context.maximum < block.transactions.length) {
                  context.maximum = block.transactions.length;
                }
              }
            }
          });
        }
      }
    },
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.block_box {
  background-color: rgb(53, 124, 255);
  width: 65px;
  height: 65px;
  padding: 10px 0;
  text-align: center;
  margin-top: 30%;
  margin-bottom: 40%;
}

.block_box_text {
  color: rgb(255, 255, 255);
  font-size: 30px;
  font-weight: 700;
}
</style>
