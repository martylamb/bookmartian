<template>
  <div class='tile-array-container'>
    <Tile />
    <Tile />
    <Tile />
    <Tile />
    <Tile />
  </div>
</template>

<script>
// @ is an alias to /src
import Tile from '@/components/Tile.vue'

export default {
  name: 'TileArray',
  components: {
    Tile
  },
  props: {
  },
  directives: {
  }
}

</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>

.tile-array-container {
  padding: 12px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
}

</style>
