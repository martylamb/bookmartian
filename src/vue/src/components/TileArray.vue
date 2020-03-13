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
            headers: {}
          })
          .then(response => {
            // handle success

            this.tiles = response.data.data
            console.log('TileArray: retreived ' + this.tiles.bookmarks.length + ' bookmark tiles')
          })
          .catch(error => {
            // handle error
            console.log(error)
          })
          .finally(function () {
            // always executed
          })
      } else {
        this.tiles = {}
      }
    },
    deleteBookmark: function (url) {
      for (var index = 0; index < this.tiles.bookmarks.length; index++) {
        if (this.tiles.bookmarks[index].url === url) {
          this.$delete(this.tiles.bookmarks, index)
          console.log('deleted tile ' + url)
        }
      }
    },
    refresh: function () {
      this.getTiles(this.query)
    }
  }
}

</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>

.tile-array-container {
  padding-left: 16px;
  padding-right: 16px;
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  align-content: center;
  min-height: 36px;
}

</style>
