<template>
  <div class='query'>
    <div class='name'>{{ this.name }}</div>
    <span class='querytext'>{{ this.query }}</span><font-awesome-icon :icon="['fas', 'ellipsis-h']" size='sm' transform='down-6' class='editicon'/>
    <div class='querytext'></div>
    <div class='bookmark' v-for='(bookmark) in this.bookmarks' v-bind:key='bookmark.url'>
      <a :href="'http://localhost:4567/api/visit?url=' + bookmark.url">{{bookmark.title}}</a>
      <font-awesome-icon :icon="['fas', 'angle-right']" size='sm' transform='down-6 left-4' class='editicon'/>
    </div>
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
  background: #fff;
  border: 1px solid lightgrey;
  padding: 6px;
  margin-bottom: 18px;
}

.name {
  font-weight: bold;
  text-align: left;
}

.editicon {
  float: right;
  color: lightgrey;
  cursor: pointer;
}

.querytext {
  font-weight: lighter;
  font-size: smaller;
  color: lightgrey;
  margin-bottom: 6px;
}

.bookmark {
  border-top: 1px solid lightgrey;
  margin-bottom: 4px;
}

a {
  text-decoration: none;
  color: inherit;
}

a:hover {
  text-decoration: underline;
}

</style>
