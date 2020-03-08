<template>
  <div class='page'>
    <!-- <h1>This is a dashboard page for {{ pageConfig.name }}</h1> -->
    <masonry :cols="{default: 3, 1000: 2, 700: 1}" :gutter="{default: '16px', 700: '8px'}" id='masonry'>
      <Query v-for='(query) in this.pageConfig.queries' v-bind:key='query.name'
        :name='query.name'
        :query='query.query'/>
    </masonry>
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

</style>
