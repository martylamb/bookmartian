<template>
  <div class="search">
    <p>
      <input type="text" :value="query" v-focus/>
    </p>
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
  color: #42b983;
}

</style>
