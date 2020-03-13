<template>
  <div class='page'>
    <div class='tagcloud'>
      <span v-for='(tag) in this.visibleTags' v-bind:key='tag.name' v-on:click='incrementSearch(tag.name)'
        :style="'background-color:' + tagBGColor(tag.name) + '; color:' + tagFGColor(tag.name)" class='tag'>{{tag.name}} </span>
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
    // making this a computed property for reactivity (simply replacing the tags data var doesn't update the DOM)
    visibleTags: function () {
      if (this.query) {
        return this.$refs.query.tags
      } else {
        return this.tags
      }
    }
  },
  directives: {},
  data: function () {
    return {
      query: '',
      tags: [],
      tagCache: new Map()
    }
  },
  methods: {
    // populate the tag cloud
    getTags: function (query) {
      // if the tag cache is empty load the full tag cloud from the API
      if (!this.tagCache.length) {
        const axios = require('axios')
        axios
          .get('http://localhost:4567/api/tags', {
            headers: {}
          })
          .then(response => {
            // handle success
            this.tags = response.data
            console.log('Search: retreived ' + this.tags.length + ' tags')

            // build the tag color cache
            if (!this.tagCache.length) {
              console.log('Search: building the tag cache')
              this.tags.forEach(tag => {
                this.tagCache.set(tag.name, tag.color)
              })
            }
          })
          .catch(error => {
            // handle error
            console.log(error)
          })
          .finally(function () {
            // always executed
          })
      }
    },
    // execute the search
    getBookmarks: function (query) {
      this.$emit('search-changed', query)
      this.$refs.query.getBookmarks(this.query)
    },
    // add a tag to the query and execute the search again
    incrementSearch: function (tag) {
      var newQuery = ''
      if (this.query) {
        newQuery = this.query + '+'
      }
      this.$router.push({ path: '/Search', query: { q: newQuery + tag } })
    },
    // set default tag color from cache if none specified
    tagBGColor: function (tag) {
      var color = this.tagCache.get(tag)
      if (color === '#000000') {
        color = '#ffffff'
      }
      return color
    },
    // set default tag color from cache if none specified
    tagFGColor: function (tag) {
      var color = this.tagCache.get(tag)
      if (color !== '#000000') {
        color = '#ffffff'
      }
      return color
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

.tag {
  border: 1px solid grey;
  margin: 1px;
  cursor: pointer;
  background-color: white;
}

.tag:hover {
  box-shadow: 0 0 5px black;
}

</style>
