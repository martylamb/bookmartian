<template>
  <div class='query'>
    <div class='name'>{{ this.name }}</div>
    <div class='querytext'>{{ this.query }}</div>
    <div class='bookmark' v-for='(bookmark) in this.bookmarks' v-bind:key='bookmark.url'>{{bookmark.title}}</div>
  </div>
</template>

<script>

export default {
  name: 'Query',
  data: function () {
    return {
      bookmarks: {}
    }
  },
  props: {
    name: String,
    query: String
  },
  directives: {
  },
  methods: {
    getBookmarks: function (query) {
      if (query) {
        const axios = require('axios')
        axios
          .get('http://localhost:4567/api/bookmarks?q=' + query, {
            headers: {
            }
          })
          .then(response => {
            // handle success
            this.bookmarks = response.data.data.bookmarks
            console.log('Retreived ' + this.bookmarks.length + ' bookmarks')
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
  mounted () {
    // retrieve bookmarks
    this.getBookmarks(this.query)
  }
}

</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>

.query {
  /* align-items: center; */
  background: #fff;
  /* box-shadow:
    0 2.8px 2.2px rgba(0, 0, 0, 0.034),
    0 6.7px 5.3px rgba(0, 0, 0, 0.048),
    0 12.5px 10px rgba(0, 0, 0, 0.06),
    0 22.3px 17.9px rgba(0, 0, 0, 0.072),
    0 41.8px 33.4px rgba(0, 0, 0, 0.086),
    0 100px 80px rgba(0, 0, 0, 0.12); */
    border: 1px solid lightgrey;
    /* display: flex; */
  /* justify-content: center; */
  padding: 6px;
  padding-left: 10px;
}

.name {
  font-weight: bold;
  text-align: left;
}

.querytext {
  font-weight: lighter;
  font-size: smaller;
  color: lightgrey;
  margin-bottom: 6px;
}

.bookmark {
  border-top: 1px solid lightgrey;
}

</style>
