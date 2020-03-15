<template>
  <div class='form'>
    <modal name="BookmarkModal" class='bookmark-modal' :height='this.modalHeight' :clickToClose='this.clickToClose'>
      <div class='bookmark-modal-box'>
        <h1>{{this.dialogTitle}}</h1>
        <b-field>
          <b-input type='text' v-model='this.selectedBookmark.title' placeholder='bookmark title'/>
        </b-field>
        <b-field>
          <b-input type='text' v-model='this.selectedBookmark.url' placeholder='bookmark url'/>
        </b-field>
        <b-field>
          <b-taginput
            v-model="selectedBookmark.tags"
            :data='filteredTags'
            autocomplete
            :allow-new='true'
            field='name'
            placeholder="tag list"
            max-height='120px'
            @typing="getFilteredTags">
          </b-taginput>
        </b-field>
        <b-field>
          <b-input type='text' v-model='this.selectedBookmark.tileImageUrl' placeholder='url used for tile image (optional)'/>
        </b-field>
        <b-field>
          <b-input type='text' v-model='this.selectedBookmark.notes' placeholder='other notes (optional)'/>
        </b-field>
        <div class='bookmark-modal-statistics' v-show='this.existingBookmark'>
          <span>created on {{simplifyDate(this.selectedBookmark.created)}} | </span>
          <span>modified on {{simplifyDate(this.selectedBookmark.modified)}} | </span>
          <span>visit #{{this.selectedBookmark.visitCount}} on {{simplifyDate(this.selectedBookmark.lastVisited)}}</span>
        </div>
        <div class='bookmark-modal-button-box'>
          <button v-on:click='deleteBookmark' v-show='this.existingBookmark' class='bookmark-modal-button-delete'>Delete Bookmark</button>
          <button v-on:click='hideBookmarkModal' class='bookmark-modal-button-standard'>Cancel</button>
          <button v-on:click='saveBookmark' class='bookmark-modal-button-standard'>Save</button>
        </div>
      </div>
    </modal>
  </div>
</template>

<script>
// @ is an alias to /src
import Vue from 'vue'
import { Field, Input, Taginput } from 'buefy'

Vue.use(Field)
Vue.use(Input)
Vue.use(Taginput)

export default {
  name: 'BookmarkForm',
  components: {
  },
  props: {
    title: String,
    url: String,
    visible: Boolean,
    existingBookmark: Boolean,
    clickToClose: Boolean,
    dialogTitle: String
  },
  data: function () {
    return {
      tags: [],
      filteredTags: [],
      selectedBookmark: { }
    }
  },
  methods: {
    showBookmarkModal: function (bookmark) {
      if (bookmark) {
        this.selectedBookmark = bookmark
      }
      this.$modal.show('BookmarkModal')
    },
    getFilteredTags: function (text) {
      this.filteredTags = this.tags.filter((option) => {
        return option
          .toString()
          .toLowerCase()
          .indexOf(text.toLowerCase()) >= 0
      })
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

      this.$emit('delete-bookmark', this.selectedBookmark)
      this.selectedBookmark = {}
      this.$modal.hide('BookmarkModal')
    },
    saveBookmark: function (event) {
      // do the API stuff to save the bookmark to the service

      this.$modal.hide('BookmarkModal')
      // redirect backwards if this is the bookmarklet?
    },
    hideBookmarkModal: function (event) {
      this.$modal.hide('BookmarkModal')
      // redirect backwards if this is the bookmarklet?
      // this.$router.go(-1)
    },
    simplifyDate: function (datestring) {
      if (datestring) {
        return datestring.substring(0, 10)
      }
    },
    getTags: function (query) {
      // if the tag cache is empty load the full tag cloud from the API
      if (!this.tags.length) {
        const axios = require('axios')
        axios
          .get('http://localhost:4567/api/tags', {
            headers: {}
          })
          .then(response => {
            // handle success
            this.tags = response.data.map(a => a.name)
            console.log('BookmarkForm: retreived ' + this.tags.length + ' tags')
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
  },
  computed: {
    modalHeight: function () {
      var baseHeight = 385
      var statHeight = 40
      return baseHeight + (this.existingBookmark ? statHeight : 0)
    }
  },
  watch: {
  },
  mounted () {
    this.getTags()
    if (this.title) {
      this.selectedBookmark.title = this.title
      this.selectedBookmark.url = this.url
    }
    if (this.visible) {
      this.showBookmarkModal()
    }
  }
}
</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>
.form {
  margin-left: 60px;
  margin-right: 60px;
  background-color: black;
}

h1 {
  padding: 4px;
  margin-bottom: 14px;
  font-weight: bolder;
  color: lightgrey;
  text-align: center;
  font-size: 26px;
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
