<template>
  <div class='home'>
    <div class='fixed-header' v-bind:style='bannerImageStyle'>
      <SearchBar />
      <TileArray
        :query="this.config.pages[$route.params.page_index]?this.config.pages[$route.params.page_index].tileQuery:''"
        ref='tileArray'/>
      <TabArray :pages='this.config.pages' />
    </div>
    <div class='page-container'>
      <div class='page'>
        <router-view
          :pageConfig='this.config.pages[$route.params.page_index]?this.config.pages[$route.params.page_index]:{}'
          v-on:delete-bookmark='deleteBookmark($event)'/>
      </div>
    </div>
  </div>
</template>

<script>
// @ is an alias to /src
import SearchBar from '@/components/SearchBar.vue'
import TileArray from '@/components/TileArray.vue'
import TabArray from '@/components/TabArray.vue'

export default {
  name: 'Home',
  components: {
    SearchBar,
    TileArray,
    TabArray
  },
  data: function () {
    return {
      // init the json config object with an empty [0] because I reference it in the template
      // can be config: {} once I remove that hard reference
      config: { pages: [{ tileQuery: '' }], bannerImageUrl: '' },
      // store the currently selected/viewed page
      bannerImageStyle: { }
    }
  },
  methods: {
    updateBackground: function (url) {
      this.bannerImageStyle = {
        backgroundRepeat: 'no-repeat',
        backgroundImage: 'url(' + url + ')',
        backgroundAttachment: 'fixed',
        backgroundColor: 'black',
        backgroundSize: 'cover'
      }
    },
    // delete a specific url from the current tiles
    deleteBookmark: function (url) {
      this.$refs.tileArray.deleteBookmark(url)
    }
  },
  watch: {
    '$route.params.page_index': function (newVal, oldVal) {
      if (newVal && this.config.pages[newVal].bannerImageUrl) {
        this.updateBackground(this.config.pages[newVal].bannerImageUrl)
      } else {
        this.updateBackground(this.config.bannerImageUrl)
      }
    }
  },
  mounted () {
    // retrieve config file
    this.config = require('../assets/sample-config.json')
    this.updateBackground(this.config.bannerImageUrl)
  }
}
</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>
.fixed-header {
  position: fixed;
  width: 100%;
}

.page-container {
  background-color: #f7f7f7;
  padding-top: 300px;
  width: 100%;
  height: 100vh;
  margin: 0px;
  color: black;
}

.page {
  margin-left: 60px;
  margin-right: 60px;
}
</style>
