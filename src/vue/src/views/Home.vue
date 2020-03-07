<template>
  <div class='home'>
    <div class='fixed-header' v-bind:style='bannerImageStyle'>
      <SearchBar />
      <TileArray :query='this.config.pages[$route.params.page_index].tileQuery' />
      <TabArray :pages='this.config.pages' />
    </div>
    <div class='page-container'>
      <div class='page'>
        <router-view :pageConfig='this.config.pages[$route.params.page_index]'/>
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
  mounted () {
    // retrieve config file
    this.config = require('../assets/sample-config.json')
    this.bannerImageStyle = {
      backgroundRepeat: 'no-repeat',
      backgroundImage: 'url(' + this.config.bannerImageUrl + ')',
      backgroundAttachment: 'fixed',
      backgroundColor: 'black'
    }
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
