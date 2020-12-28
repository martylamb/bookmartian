<template>
  <div class='login'>
    <div class='fixed-header'>
      bookmartian login
    </div>
    <div class='page-container'>
      <div class='page'>
        <h1><a href="#username" />Login with username</h1>
        <p>TODO: insert login form for username</p>

        <h1><a href='#code' />Login with a code</h1>
        <p>TODO: insert login form for code</p>
      </div>
    </div>
  </div>
</template>

<script>
// @ is an alias to /src
// import ___ from '@/components/___.vue'

export default {
  name: 'Login',
  components: {
    // ___
  },
  props: {},
  directives: {},
  data: function () {
    return {}
  },
  computed: {},
  watch: {},
  methods: {},
  mounted () {}
}
</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>
.page-container {
  background-color: #f7f7f7;
  width: 100%;
  color: black;
  min-height: 100vh;
}

.fixed-header {
  background-repeat: no-repeat;
  /* background-image: url(); */
  height: 96px;
  background-attachment: fixed;
  background-color: black;
  background-size: cover;
  color: white;
  text-align: center;
  font-size: x-large;
  padding: 12px;
}

.page {
  margin-left: 60px;
  margin-right: 60px;
}

h1 {
  font-size: 16px;
  line-height: 24px;
  font-weight: 600;
  padding-top: 24px;
  padding-bottom: ;
}
</style>
