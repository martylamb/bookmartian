<template>
  <div class="search">
        <input type="text" :value="query" v-focus class="searchinput"/>
        <font-awesome-icon :icon="['fas', 'search']" size="lg" flip="horizontal" class="searchicon"/>
  </div>
</template>

<script>

export default {
  name: 'SearchBar',
  props: {
    // pre-filled query can be sent to the component and defaults into the search bar
    query: String
  },
  directives: {
    // directive definition to set focus on a componenet (used on page load for the search bar)
    focus: {
      inserted: function (el) {
        el.focus()
      }
    }
  }
}

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
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
  margin-top: 36px;
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
