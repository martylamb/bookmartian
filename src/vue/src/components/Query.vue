<template>
  <div class='query'>
    <span class='name'>{{ this.name }}</span>
    <b-dropdown aria-role='list' position='is-bottom-left' class='querymenu'>
      <font-awesome-icon :icon="['fas', 'ellipsis-h']" size='sm' class='editicon' slot='trigger' role='button'/>
      <b-dropdown-item aria-role='listitem' v-on:click='sortByTitle()' v-bind:class='{activeSort: isTitleSorted}'>{{this.titleSortedLabel}}</b-dropdown-item>
      <b-dropdown-item aria-role='listitem' v-on:click='sortByCreated()' v-bind:class='{activeSort: isCreatedSorted}'>{{this.createSortedLabel}}</b-dropdown-item>
      <b-dropdown-item aria-role='listitem' v-on:click='sortByVisited()' v-bind:class='{activeSort: isVisitedSorted}'>{{this.visitSortedLabel}}</b-dropdown-item>
      <b-dropdown-item aria-role='listitem' v-on:click='sortByVisits()' v-bind:class='{activeSort: isVisitsSorted}'>{{this.visitsSortedLabel}}</b-dropdown-item>
      <b-dropdown-item aria-role='listitem'></b-dropdown-item>
      <b-dropdown-item aria-role='listitem' v-on:click='openInSearch()'>Open in tag search</b-dropdown-item>
      <b-dropdown-item aria-role='listitem' v-on:click='openInTabs()'>Open every link in a tab</b-dropdown-item>
    </b-dropdown>
    <div class='querytext'>{{ this.query }}</div>
    <div class='bookmark' v-for='(bookmark) in this.bookmarks' v-bind:key='bookmark.url'>
      <a :href="'/api/visit?url=' + bookmark.url">{{bookmark.title}}</a>
      <div class='editspacer' v-on:click="$emit('edit-bookmark', bookmark)">
        <font-awesome-icon :icon="['fas', 'angle-right']" size='sm' transform='left-4' class='editicon' />
      </div>
    </div>
  </div>

</template>

<script>

export default {
  name: 'Query',
  components: {},
  data: function () {
    return {
      bookmarks: [],
      sort: String,
      isTitleSorted: '',
      isCreatedSorted: '',
      isVisitedSorted: '',
      isVisitsSorted: ''
    }
  },
  props: {
    name: String,
    query: String
  },
  directives: {
  },
  computed: {
    titleSortedLabel: function () {
      var menuLabel = 'Sort by title'
      if (this.isTitleSorted) {
        menuLabel += ' (' + this.isTitleSorted + ')'
      }
      return menuLabel
    },
    createSortedLabel: function () {
      var menuLabel = 'Sort by date created'
      if (this.isCreatedSorted) {
        menuLabel += ' (' + this.isCreatedSorted + ')'
      }
      return menuLabel
    },
    visitSortedLabel: function () {
      var menuLabel = 'Sort by date visited'
      if (this.isVisitedSorted) {
        menuLabel += ' (' + this.isVisitedSorted + ')'
      }
      return menuLabel
    },
    visitsSortedLabel: function () {
      var menuLabel = 'Sort by visit frequency'
      if (this.isVisitsSorted) {
        menuLabel += ' (' + this.isVisitsSorted + ')'
      }
      return menuLabel
    },
    // retrieve a list of all tags found on bookmarks in this query
    tags: function () {
      var uniqueTags = new Set()
      var tagsList = []
      for (var index = 0; index < this.bookmarks.length; index++) {
        if (this.bookmarks[index].tags) {
          for (var index2 = 0; index2 < this.bookmarks[index].tags.length; index2++) {
            if (!uniqueTags.has(this.bookmarks[index].tags[index2])) {
              uniqueTags.add(this.bookmarks[index].tags[index2])
              tagsList.push({ name: this.bookmarks[index].tags[index2], color: '#000000' })
            }
          }
        }
      }
      return tagsList
    }
  },
  methods: {
    getBookmarks: function (query) {
      if (query) {
        const axios = require('axios')
        axios
          .get('/api/bookmarks?q=' + query, {
            headers: {
            }
          })
          .then(response => {
            // handle success
            this.bookmarks = response.data.data.bookmarks
            this.sort = response.data.data.sort
            switch (this.sort) {
              case 'by:title':
                this.isTitleSorted = 'asc'
                break
              case 'by:most-recently-created':
                this.isCreatedSorted = 'desc'
                break
              case 'by:most-recently-visited':
                this.isVisitedSorted = 'desc'
                break
              case 'by:least-recently-created':
                this.isCreatedSorted = 'asc'
                break
              case 'by:least-recently-visited':
                this.isVisitedSorted = 'asc'
                break
              case 'by:most-visited':
                this.isVisitsSorted = 'desc'
                break
              case 'by:least-visited':
                this.isVisitsSorted = 'asc'
                break

              default:
                break
            }
            console.log('Query: retreived ' + this.bookmarks.length + ' bookmarks')
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
      if (this.isTitleSorted === 'asc') {
        this.bookmarks.reverse()
        this.isTitleSorted = 'desc'
      } else {
        this.bookmarks.sort(function (a, b) {
          a = a.title.toLowerCase()
          b = b.title.toLowerCase()
          return a > b ? 1 : b > a ? -1 : 0
        }
        )
        this.isTitleSorted = 'asc'
      }
      this.isVisitsSorted = ''
      this.isCreatedSorted = ''
      this.isVisitedSorted = ''
    },
    sortByCreated: function () {
      if (this.isCreatedSorted === 'desc') {
        this.bookmarks.reverse()
        this.isCreatedSorted = 'asc'
      } else {
        this.bookmarks.sort(function (b, a) {
          a = a.created.toLowerCase()
          b = b.created.toLowerCase()
          return a > b ? 1 : b > a ? -1 : 0
        }
        )
        this.isCreatedSorted = 'desc'
      }
      this.isTitleSorted = ''
      this.isVisitsSorted = ''
      this.isVisitedSorted = ''
    },
    sortByVisited: function () {
      if (this.isVisitedSorted === 'desc') {
        this.bookmarks.reverse()
        this.isVisitedSorted = 'asc'
      } else {
        this.bookmarks.sort(function (b, a) {
          if (a.lastVisited) {
            a = a.lastVisited.toLowerCase()
          } else {
            a = ''
          }

          if (b.lastVisited) {
            b = b.lastVisited.toLowerCase()
          } else {
            b = ''
          }

          return a > b ? 1 : b > a ? -1 : 0
        }
        )
        this.isVisitedSorted = 'desc'
      }
      this.isVisitsSorted = ''
      this.isTitleSorted = ''
      this.isCreatedSorted = ''
    },
    sortByVisits: function () {
      if (this.isVisitsSorted === 'desc') {
        this.bookmarks.reverse()
        this.isVisitsSorted = 'asc'
      } else {
        this.bookmarks.sort(function (b, a) {
          a = a.visitCount
          b = b.visitCount
          return a > b ? 1 : b > a ? -1 : 0
        }
        )
        this.isVisitsSorted = 'desc'
      }
      this.isVisitedSorted = ''
      this.isTitleSorted = ''
      this.isCreatedSorted = ''
    },
    // delete a specific url from the current query results
    deleteBookmark: function (url) {
      for (var index = 0; index < this.bookmarks.length; index++) {
        if (this.bookmarks[index].url === url) {
          this.$delete(this.bookmarks, index)
        }
      }
    },
    refresh: function () {
      this.getBookmarks(this.query)
    },
    openInSearch: function () {
      this.$router.push({ path: '/search', query: { q: this.query } })
    },
    openInTabs: function () {
      this.bookmarks.forEach(link => {
        window.open(link.url)
      })
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

.activeSort {
  font-weight: bold;
}

.editspacer {
  padding-left: 8px;
  cursor: pointer;
  margin-left: auto;
}

.editicon {
  color: lightgrey;
  cursor: pointer;
}

.editspacer:hover .editicon {
  color: blue;
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
  display: flex;
}

a {
  text-decoration: none;
  color: inherit;
}

.bookmark a:hover {
  text-decoration: underline;
}

</style>
