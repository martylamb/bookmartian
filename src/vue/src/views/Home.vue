<template>
  <div class="home">
    <SearchBar />
    <TileArray />
  </div>
</template>

<script>
// @ is an alias to /src
import SearchBar from '@/components/SearchBar.vue'
import TileArray from '@/components/TileArray.vue'

export default {
  name: 'Home',
  components: {
    SearchBar,
    TileArray
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
