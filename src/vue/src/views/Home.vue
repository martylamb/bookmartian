<template>
  <div class='home'>
    <div class='fixed-header' v-bind:style='bannerImageStyle'>
      <SearchBar ref='searchbar' :internetSearchUrl='this.config.internetSearchUrl'/>
      <TileArray
        :query="this.config.pages[$route.params.page_index]?this.config.pages[$route.params.page_index].tileQuery:'--nochange'"
        ref='tileArray'/>
      <TabArray :pages='this.config.pages' v-on:new-bookmark="addBookmarkModal()" />
    </div>
    <div class='page-container' :class="[this.needsPadding ? 'padded' : '']">
      <div class='page'>
        <router-view
          :pageConfig='this.config.pages[$route.params.page_index]?this.config.pages[$route.params.page_index]:{}'
          v-on:edit-bookmark='showBookmarkModal($event)'
          v-on:search-changed='changeSearchTerm($event)'
          ref='page'/>
      </div>
    </div>
    <BookmarkForm ref='bookmarkform'
      dialogTitle='edit bookmark'
      :visible='false'
      :existingBookmark='this.isExistingBookmark'
      :clickToClose='true'
      v-on:delete-bookmark='deleteBookmark($event)'
      v-on:save-bookmark='saveBookmark($event)'/>
  </div>
</template>

<script>
// @ is an alias to /src
import SearchBar from '@/components/SearchBar.vue'
import TileArray from '@/components/TileArray.vue'
import TabArray from '@/components/TabArray.vue'
import BookmarkForm from '@/components/BookmarkForm.vue'

export default {
  name: 'Home',
  components: {
    SearchBar,
    TileArray,
    TabArray,
    BookmarkForm
  },
  data: function () {
    return {
      // init the json config object with an empty [0] because I reference it in the template
      // can be config: {} once I remove that hard reference
      config: { pages: [{ tileQuery: '' }], bannerImageUrl: '' },
      bannerImageStyle: { },
      selectedBookmark: { title: '', url: '' },
      needsPadding: false,
      isExistingBookmark: true
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
    showBookmarkModal: function (bookmark) {
      this.isExistingBookmark = true
      this.selectedBookmark = bookmark
      this.$refs.bookmarkform.showBookmarkModal(bookmark)
    },
    addBookmarkModal: function () {
      this.isExistingBookmark = false
      this.selectedBookmark = {}
      this.$refs.bookmarkform.showBookmarkModal({ title: '', url: '', tags: [], tileImageUrl: '', notes: '' })
    },
    // update the ui after a bookmark deletion event
    deleteBookmark: function (bookmark) {
      this.selectedBookmark = bookmark
      this.$refs.page.deleteBookmark(this.selectedBookmark.url)
      this.$refs.tileArray.deleteBookmark(this.selectedBookmark.url)

      this.selectedBookmark = {}
    },
    // update the ui after a bookmark save event
    saveBookmark: function (bookmark) {
      this.$refs.page.refresh()
      this.$refs.tileArray.refresh()

      this.selectedBookmark = {}
    },
    changeSearchTerm: function (event) {
      this.$refs.searchbar.updateQuery(event)
    },
    // use the API to retrieve a config file
    getConfig: async function () {
      const axios = require('axios')
      await axios
        .get('/api/config', {
          headers: {
          }
        })
        .then(response => {
          // handle success
          this.config = response.data
          console.log('Home: loaded config file')
        })
        .catch(error => {
          // handle error
          this.config = require('../assets/default-config.json')
          console.log(error)
        })
        .finally(function () {
          // always executed
        })
    }
  },
  computed: {},
  watch: {
    '$route.params.page_index': function (newVal, oldVal) {
      if (newVal) {
        if (this.config.pages[newVal].bannerImageUrl) {
          this.updateBackground(this.config.pages[newVal].bannerImageUrl)
        } else {
          this.updateBackground(this.config.bannerImageUrl)
        }

        // change padding on dashboard pages to reflect tiles
        if (this.$route.params.page_index) {
          if (this.config.pages[this.$route.params.page_index].tileQuery) {
            this.needsPadding = true
          } else {
            this.needsPadding = false
          }
        }
      }
    }
  },
  async mounted () {
    // retrieve config file
    await this.getConfig()
    // this.config = require('../assets/sample-config.json')

    // set the banner background to template default
    this.updateBackground(this.config.bannerImageUrl)

    // change padding on dashboard pages to reflect tiles
    if (this.$route.params.page_index) {
      if (this.config.pages[this.$route.params.page_index].tileQuery) {
        this.needsPadding = true
      } else {
        this.needsPadding = false
      }
    }
  }
}
</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>
.fixed-header {
  position: fixed;
  width: 100%;
  z-index: 100;
}

.page-container {
  background-color: #f7f7f7;
  width: 100%;
  min-height: 100vh;
  margin: 0px;
  color: black;
  padding-top: 148px;
}

.padded {
  padding-top: 250px;
}

.page {
  margin-left: 60px;
  margin-right: 60px;
}

</style>
