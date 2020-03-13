<template>
  <div class='search'>
    <form @submit.prevent="submitSearch">
        <input type='text' v-model='query' v-focus class='searchinput'/>
        <font-awesome-icon :icon="['fas', 'search']" size='lg' flip='horizontal' class='searchicon'/>
    </form>
  </div>
</template>

<script>

export default {
  name: 'SearchBar',
  props: {
    // pre-filled query can be sent to the component and defaults into the search bar
    q: String
  },
  data: function () {
    return {
      // query is the vue model variable for the search string
      query: ''
    }
  },
  directives: {
    // directive definition to set focus on a component (used on page load for the search bar)
    focus: {
      inserted: function (el) {
        el.focus()
      }
    }
  },
  methods: {
    // submit the search
    submitSearch: function () {
      this.$router.push({ path: '/Search', query: { q: this.query } })
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

/* component search box (includes input and search icon) */
.search {
  background-color: white;
  border: none;
  border-radius: 4px;
  box-shadow: 0 0.3px 0.9px rgba(0, 0, 0, .12), 0 1.6px 3.6px rgba(0, 0, 0, .12);
  width:40%;
  margin: auto;
  margin-top: 24px;
}

.searchinput {
  border: none;
  height: 34px;
  width: 90%;
  font-size: large;
  padding-left: 10px;
  padding-right: 10px;
}

.searchinput:focus{
  outline: none;
}

.searchicon {
  color: lightgrey;
}

.searchicon:hover {
  cursor:pointer;
}

</style>
