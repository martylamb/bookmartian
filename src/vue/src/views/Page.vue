<template>
  <div class="page">
    <h1>This is a dashboard page for {{ pageConfig.name }}</h1>
  </div>
</template>

<script>
// @ is an alias to /src
// import ___ from '@/components/___.vue'

export default {
  name: 'Page',
  components: {
    // ___
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
