<template>
  <div class='query'>
    <span class='name'>{{ this.name }}</span>
    <b-dropdown aria-role='list' position='is-bottom-left' class='querymenu'>
      <font-awesome-icon :icon="['fas', 'ellipsis-h']" size='sm' class='editicon' slot='trigger' role='button'/>
      <b-dropdown-item aria-role='listitem' v-on:click='sortByTitle()'>Sort by title</b-dropdown-item>
      <b-dropdown-item aria-role='listitem' v-on:click='sortByCreated()'>Sort by date created</b-dropdown-item>
      <b-dropdown-item aria-role='listitem' v-on:click='sortByVisited()'>Sort by last visit</b-dropdown-item>
      <b-dropdown-item aria-role='listitem'>Open query as new search</b-dropdown-item>
    </b-dropdown>
    <div class='querytext'>{{ this.query }}</div>
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
      bookmarks: {},
      isTitleSorted: true,
      isCreatedSorted: false,
      isVisitedSorted: false
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
    },
    sortByTitle: function () {
      if (this.isTitleSorted) {
        this.bookmarks.reverse()
      } else {
        this.bookmarks.sort(function (a, b) {
          a = a.title.toLowerCase()
          b = b.title.toLowerCase()
          return a > b ? 1 : b > a ? -1 : 0
        }
        )
      }
      this.isTitleSorted = !this.isTitleSorted
      this.isCreatedSorted = false
      this.isVisitedSorted = false
    },
    sortByCreated: function () {
      if (this.isCreatedSorted) {
        this.bookmarks.reverse()
      } else {
        this.bookmarks.sort(function (a, b) {
          a = a.created.toLowerCase()
          b = b.created.toLowerCase()
          return a > b ? 1 : b > a ? -1 : 0
        }
        )
      }
      this.isCreatedSorted = !this.isCreatedSorted
      this.isTitleSorted = false
      this.isVisitedSorted = false
    },
    sortByVisited: function () {
      if (this.isVisitedSorted) {
        this.bookmarks.reverse()
      } else {
        this.bookmarks.sort(function (a, b) {
          a = a.lastVisited.toLowerCase()
          b = b.lastVisited.toLowerCase()
          return a > b ? 1 : b > a ? -1 : 0
        }
        )
      }
      this.isVisitedSorted = !this.isVisitedSorted
      this.isTitleSorted = false
      this.isCreatedSorted = false
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

.querymenu {
  float: right;
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

.bookmark a:hover {
  text-decoration: underline;
}

</style>
