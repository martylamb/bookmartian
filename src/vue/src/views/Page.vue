<template>
  <div class='page'>
    <!-- <h1>This is a dashboard page for {{ pageConfig.name }}</h1> -->
    <masonry :cols="{default: 3, 1000: 2, 700: 1}" :gutter="{default: '16px', 700: '8px'}" id='masonry'>
      <Query v-for='(query) in this.pageConfig.queries' v-bind:key='query.name'
        :name='query.name'
        :query='query.query'
        v-on:edit-bookmark='ShowBookmarkModal($event)'/>
    </masonry>
    <modal name="BookmarkModal" class='bookmark-modal'>
      <div class='bookmark-modal-box'>
        <input type='text' :value='this.selectedBookmark.title' placeholder='bookmark title' class='bookmark-model-title'/>
        <input type='text' :value='this.selectedBookmark.url' placeholder='bookmark url'/>
        <input type='text' :value='this.selectedBookmark.imageUrl' placeholder='url used for tile image (optional)'/>
        <input type='text' :value='this.selectedBookmark.notes' placeholder='other notes'/>
        <input type='text' :value='this.selectedBookmark.tags' placeholder='tag list'/>
        <div class='bookmark-modal-button-box'>
          <button>Delete Bookmark</button><button>Cancel</button><button>Save Changes</button>
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
    ShowBookmarkModal: function (bookmark) {
      this.selectedBookmark = bookmark
      this.$modal.show('BookmarkModal')
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
    margin-top: 32px;
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
    background-color: grey;
    color: white;
  }

</style>
