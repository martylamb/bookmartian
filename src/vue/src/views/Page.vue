<template>
  <div class='page'>
    <masonry :cols="{default: 3, 1000: 2, 700: 1}" :gutter="{default: '16px', 700: '8px'}" id='masonry'>
      <Query v-for='(query) in this.pageConfig.queries' v-bind:key='query.name' ref='queries'
        :name='query.name'
        :query='query.query'
        v-on:edit-bookmark='bubbleEditBookmark($event)'/>
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
  },
  data: function () {
    return {
    }
  },
  methods: {
    // have to manually bubble the event coming from the Queue component up to Home
    bubbleEditBookmark (event) {
      this.$emit('edit-bookmark', event)
    },
    deleteBookmark (url) {
      // remove references to that bookmark from the active page (all queries)
      this.$refs.queries.forEach(query => {
        query.deleteBookmark(url)
      })
    },
    refresh () {
      this.$refs.queries.forEach(query => {
        query.refresh()
      })
    }
  }
}

</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>

</style>
