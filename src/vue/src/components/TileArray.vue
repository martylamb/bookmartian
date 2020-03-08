<template>
  <div class='tile-array-container'>
    <Tile v-for='(tile) in this.tiles.bookmarks' v-bind:key='tile.url'
          :title='tile.title'
          :url='tile.url'
          :image-url='tile.imageUrl'/>
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
  data: function () {
    return {
      tiles: {}
    }
  },
  props: {
    query: String
  },
  watch: {
    query: function (newVal, oldVal) {
      this.getTiles(newVal)
    }
  },
  directives: {
  },
  methods: {
    getTiles: function (query) {
      if (query) {
        const axios = require('axios')
        axios
          .get('http://localhost:4567/api/bookmarks?q=' + query, {
            headers: {
            }
          })
          .then(response => {
            // handle success
            this.tiles = response.data.data
            console.log('Retreived ' + this.tiles.bookmarks.length + ' bookmark tiles')
          })
          .catch(error => {
            // handle error
            console.log(error)
          })
          .finally(function () {
            // always executed
          })
      }
    }
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
  min-height: 12px;
}

</style>
