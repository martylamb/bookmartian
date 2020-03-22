<template>
  <div class='new'>
    <BookmarkForm
      dialogTitle='bookmartian'
      :title='$route.query.title'
      :url='$route.query.url'
      :visible='true'
      :existingBookmark='false'
      :clickToClose='false'/>
  </div>
</template>

<script>
// @ is an alias to /src
import BookmarkForm from '@/components/BookmarkForm.vue'

export default {
  name: 'New',
  components: {
    BookmarkForm
  },
  data: function () {
    return {
    }
  },
  methods: {},
  computed: {},
  watch: {},
  mounted () {}
}
</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>

.new {
  margin-left: 60px;
  margin-right: 60px;
  background-color: black;
}

</style>
