<template>
  <div class='page'>
    <!-- <h1>This is a dashboard page for {{ pageConfig.name }}</h1> -->
    <masonry :cols="{default: 3, 1000: 2, 700: 1}" :gutter="{default: '16px', 700: '8px'}" id='masonry'>
      <Query v-for='(query) in this.pageConfig.queries' v-bind:key='query.name' ref='queries'
        :name='query.name'
        :query='query.query'
        v-on:edit-bookmark='showBookmarkModal($event)'/>
    </masonry>
    <modal name="BookmarkModal" class='bookmark-modal' :height='320'>
      <div class='bookmark-modal-box'>
        <input type='text' :value='this.selectedBookmark.title' placeholder='bookmark title' class='bookmark-model-title'/>
        <input type='text' :value='this.selectedBookmark.url' placeholder='bookmark url'/>
        <input type='text' :value='this.selectedBookmark.imageUrl' placeholder='url used for tile image (optional)'/>
        <input type='text' :value='this.selectedBookmark.notes' placeholder='other notes'/>
        <input type='text' :value='this.selectedBookmark.tags' placeholder='tag list'/>
        <div class='bookmark-modal-statistics'>
          <span >created on {{simplifyDate(this.selectedBookmark.created)}} | </span>
          <span >modified on {{simplifyDate(this.selectedBookmark.modified)}} | </span>
          <span >visit #{{this.selectedBookmark.visitCount}} on {{simplifyDate(this.selectedBookmark.lastVisited)}}</span>
        </div>
        <div class='bookmark-modal-button-box'>
          <button v-on:click='deleteBookmark' class='bookmark-modal-button-delete'>Delete Bookmark</button>
          <button v-on:click='hideBookmarkModal' class='bookmark-modal-button-standard'>Cancel</button>
          <button v-on:click='saveBookmark' class='bookmark-modal-button-standard'>Save Changes</button>
        </div>
      </div>
    </modal>
  </div>
</template>

<script>
// @ is an alias to /src
import Query from '@/components/Query.vue'

export default {
  name: 'Page',
  components: {
    Query
  },
  props: {
    pageConfig: Object
  },
  directives: {
  },
  data: function () {
    return {
      selectedBookmark: { title: '', url: '' }
    }
  },
  methods: {
    showBookmarkModal: function (bookmark) {
      this.selectedBookmark = bookmark
      this.$modal.show('BookmarkModal')
    },
    deleteBookmark: function (event) {
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

      // remove references to that bookmark from the active page (all queries)
      this.$refs.queries.forEach(query => {
        query.deleteBookmark(this.selectedBookmark.url)
      })

      // propagate an event up to the Home component to delete the url from the TileArray
      this.$emit('delete-bookmark', this.selectedBookmark.url)

      this.selectedBookmark = {}
      this.$modal.hide('BookmarkModal')
    },
    saveBookmark: function (event) {
      this.selectedBookmark = {}
      this.$modal.hide('BookmarkModal')
    },
    hideBookmarkModal: function (event) {
      this.selectedBookmark = {}
      this.$modal.hide('BookmarkModal')
    },
    simplifyDate: function (datestring) {
      if (datestring) {
        return datestring.substring(0, 10)
      }
    }
  }
}

</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>

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
