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

input {
  border: none;
  border-color:#e5e5e5;
  border-radius: 4px;
  box-shadow: 0 0.3px 0.9px rgba(0, 0, 0, .12), 0 1.6px 3.6px rgba(0, 0, 0, .12);
  width:50%;
  height: 34px;
  font-size: large;
  padding-left: 10px;
  padding-right: 10px;
  display: block;
  margin: auto;
}

input:focus{
  outline: none;
}

</style>
