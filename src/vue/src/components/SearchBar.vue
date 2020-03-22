<template>
  <div class='search'>
    <form @keyup.ctrl.enter.prevent='submitOnlineSearch' @submit.exact.prevent='submitSearch'>
        <b-input
          v-model='query'
          :autofocus="true"
          type='search'
          icon-pack='fas'
          icon-right='search'
          placeholder='enter for bookmark search, ctrl-enter for internet search'/>
    </form>
  </div>
</template>

<script>

export default {
  name: 'SearchBar',
  props: {
    // pre-filled query can be sent to the component and defaults into the search bar
    q: String,
    internetSearchUrl: String
  },
  data: function () {
    return {
      // query is the vue model variable for the search string
      query: ''
    }
  },
  directives: {},
  methods: {
    // submit the search
    submitSearch: function (e) {
      this.$router.push({ path: '/search', query: { q: this.query } })
    },
    submitOnlineSearch: function () {
      window.location = this.internetSearchUrl + this.query
    },
    // used by Home to keep the searchbar in sync with searching happening in the Search view
    updateQuery: function (query) {
      this.query = query
    }
  },
  mounted () {
    // when the site first loads, if a q= was provided, pre-fill it into the searchbar
    this.updateQuery(this.$route.query.q)
  }
}

</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>

a {
  color:white;
}

.search {
  width:40%;
  margin: auto;
  margin-top: 24px;

}
</style>
