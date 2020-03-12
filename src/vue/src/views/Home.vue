<template>
  <div class='home'>
    <div class='fixed-header' v-bind:style='bannerImageStyle'>
      <SearchBar ref='searchbar'/>
      <TileArray
        :query="this.config.pages[$route.params.page_index]?this.config.pages[$route.params.page_index].tileQuery:''"
        ref='tileArray'/>
      <TabArray :pages='this.config.pages' v-on:new-bookmark="showBookmarkModal({ title: '', url: '' })" />
    </div>
    <div class='page-container'>
      <div class='page'>
        <router-view
          :pageConfig='this.config.pages[$route.params.page_index]?this.config.pages[$route.params.page_index]:{}'
          v-on:edit-bookmark='showBookmarkModal($event)'
          v-on:search-changed='changeSearchTerm($event)'
          ref='page'/>
      </div>
    </div>
    <modal name="BookmarkModal" class='bookmark-modal' :height='320'>
      <div class='bookmark-modal-box'>
        <input type='text' :value='this.selectedBookmark.title' placeholder='bookmark title' class='bookmark-model-title'/>
        <input type='text' :value='this.selectedBookmark.url' placeholder='bookmark url'/>
        <input type='text' :value='this.selectedBookmark.imageUrl' placeholder='url used for tile image (optional)'/>
        <input type='text' :value='this.selectedBookmark.notes' placeholder='other notes'/>
        <input type='text' :value='this.selectedBookmark.tags' placeholder='tag list'/>
        <div class='bookmark-modal-statistics'>
          <span v-show='this.isExistingBookmark' >created on {{simplifyDate(this.selectedBookmark.created)}} | </span>
          <span v-show='this.isExistingBookmark' >modified on {{simplifyDate(this.selectedBookmark.modified)}} | </span>
          <span v-show='this.isExistingBookmark' >visit #{{this.selectedBookmark.visitCount}} on {{simplifyDate(this.selectedBookmark.lastVisited)}}</span>
        </div>
        <div class='bookmark-modal-button-box'>
          <button v-on:click='deleteBookmark' v-show='this.isExistingBookmark' class='bookmark-modal-button-delete'>Delete Bookmark</button>
          <button v-on:click='hideBookmarkModal' class='bookmark-modal-button-standard'>Cancel</button>
          <button v-on:click='saveBookmark' class='bookmark-modal-button-standard'>Save Changes</button>
        </div>
      </div>
    </modal>
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
      bannerImageStyle: { },
      selectedBookmark: { title: '', url: '' },
      isExistingBookmark: false
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
    simplifyDate: function (datestring) {
      if (datestring) {
        return datestring.substring(0, 10)
      }
    },
    showBookmarkModal: function (bookmark) {
      this.isExistingBookmark = false
      if (bookmark.url) {
        this.selectedBookmark = bookmark
        this.isExistingBookmark = true
      }
      this.$modal.show('BookmarkModal')
    },
    deleteBookmark: function () {
      // const axios = require('axios')
      // axios
      //   .post('http://localhost:4567/api/bookmark/delete?url=' + this.selectedBookmark.url, {
      //     headers: {
      //     }
      //   })
      //   .then(response => {
      //     // handle success
      //     console.log('Deleted ' + this.selectedBookmark.title)
      //   })
      //   .catch(error => {
      //     // handle error
      //     console.log(error)
      //   })
      //   .finally(function () {
      //     // always executed
      //   })

      this.$refs.page.deleteBookmark(this.selectedBookmark.url)
      this.$refs.tileArray.deleteBookmark(this.selectedBookmark.url)

      this.selectedBookmark = {}
      this.$modal.hide('BookmarkModal')
    },
    saveBookmark: function (event) {
      // do the API stuff to save the bookmark to the service

      this.$refs.page.refresh()
      this.$refs.tileArray.refresh()

      this.selectedBookmark = {}
      this.$modal.hide('BookmarkModal')
    },
    hideBookmarkModal: function (event) {
      this.selectedBookmark = {}
      this.$modal.hide('BookmarkModal')
    },
    changeSearchTerm: function (event) {
      this.$refs.searchbar.updateQuery(event)
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

.bookmark-modal {

}

.bookmark-modal-button-box {
  width: 100%;
  text-align: center;
  margin-top: 26px;
}

.bookmark-modal-box {
  padding: 12px;
}

.bookmark-modal input[type=text] {
  display: block;
  box-sizing: border-box;
  margin-bottom: 4px;
  width: 100%;
  line-height: 2;
  border: 0;
  border-bottom: 1px solid #DDDEDF;
  padding: 4px 8px;
  font-family: inherit;
  transition: 0.5s all;
  outline: none;
}

.bookmark-model-title {
  font-weight: bold;
  font-size: 16px;
}

.bookmark-modal-statistics {
  font-size: 10px;
  color: lightgrey;
  width: 100%;
  margin-top: 8px;
  text-align: right;
  min-height: 23px;
}

.bookmark-modal button {
    background: white;
    border-radius: 4px;
    box-sizing: border-box;
    padding: 10px;
    letter-spacing: 1px;
    font-family: "Open Sans", sans-serif;
    font-weight: 400;
    min-width: 140px;
    margin-top: 8px;
    margin-left: 8px;
    margin-right: 8px;
    color: #8b8c8d;
    cursor: pointer;
    border: 1px solid #DDDEDF;
    text-transform: uppercase;
    transition: 0.1s all;
    font-size: 10px;
    outline: none;
}

.bookmark-modal button:hover {
  border-color: darkgrey;
  color: white;
}

.bookmark-modal-button-standard:hover {
  background-color: lightblue;
}

.bookmark-modal-button-delete:hover {
  background-color: red;
}

</style>
