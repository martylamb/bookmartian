<template>
  <div class='page'>
    <div class='tagcloud'>
      <router-link v-for='(tag) in this.tags' v-bind:key='tag.name'
        :style="'color:' + tag.color"
        v-bind:to="{ path: '/Search', query: { q: query + ' ' + tag.name } }">{{tag.name}} </router-link>
    </div>
    <Query v-show=query :name="'Search results for: ' + query" ref='query'
        :query='query'
        v-on:edit-bookmark='bubbleEditBookmark($event)'/>
  </div>
</template>

<script>
import Query from '@/components/Query.vue'

export default {
  name: 'Search',
  components: {
    Query
  },
  props: {
  },
  computed: {
  },
  directives: {},
  data: function () {
    return {
      query: '',
      tags: {}
    }
  },
  methods: {
    // populate the tag cloud
    getTags: function (query) {
      // if no query was specific load the full tag cloud
      if (!query) {
        const axios = require('axios')
        axios
          .get('http://localhost:4567/api/tags', {
            headers: {
            }
          })
          .then(response => {
            // handle success
            this.tags = response.data
            console.log('Search: retreived ' + this.tags.length + ' tags')
          })
          .catch(error => {
            // handle error
            console.log(error)
          })
          .finally(function () {
            // always executed
          })
      } else {
        // if there is a query, we will only list the tags found on bookmarks in the result set
        // magicitems is a placeholder
        this.tags = [{ name: 'magicitems', color: 'red' }]
      }
    },
    // execute the search
    getBookmarks: function (query) {
      this.$emit('search-changed', query)
      this.$refs.query.getBookmarks(this.query)
    },
    // delete a bookmark
    deleteBookmark (url) {
      // remove references to that bookmark from the active page
      this.$refs.query.deleteBookmark(url)
    },
    bubbleEditBookmark (event) {
      this.$emit('edit-bookmark', event)
    }
  },
  watch: {
    // when the querystring q= changes, we need to update the results on the page
    '$route.query': function (to, from) {
      if (to.q) {
        this.query = to.q
      } else {
        this.query = ''
      }
      this.getBookmarks(this.query)
      this.getTags(this.query)
    }
  },
  mounted () {
    // display initial tag cloud and search results based on q= querystring in the route
    if (this.$route.query.q) {
      this.query = this.$route.query.q
    } else {
      this.query = ''
    }
    this.getBookmarks(this.query)
    this.getTags(this.query)
  }
}
</script>

<style scoped>

.tagcloud {
  font-size: 12px;
  margin-bottom: 24px;
  text-align: center;
}

</style>
