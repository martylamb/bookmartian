<template>
  <div class='page'>
    <!-- <h1>This is a dashboard page for {{ pageConfig.name }}</h1> -->
    <div class='queries'>
      <Query v-for='(query) in this.pageConfig.queries' v-bind:key='query.name'
        :name='query.name'
        :query='query.query'/>
    </div>
  </div>
</template>

<script>
// @ is an alias to /src
import Query from '@/components/Query.vue'

export default {
  name: 'Page',
  components: {
    Query
  },
  props: {
    pageConfig: Object
  },
  directives: {
  }
}

</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>

.queries {
  display: grid;
  grid-auto-flow: dense;
  grid-gap: 1em;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(3, fit-content);
  margin: 0 auto;
  max-width: 1200px;
}

</style>
