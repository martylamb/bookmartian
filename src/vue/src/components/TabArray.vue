<template>
  <div class='page-tabs'>
    <div v-for='(page, index) in this.pages' v-bind:key='page.name' class='page-tab'>
      <router-link v-bind:to="'/Page/' + index">{{page.name}}</router-link>
    </div>
    <!-- <div class='page-tab settings'>tags</div> -->
    <div class='page-tab settings'>
      <router-link to="/Settings">bookmartian</router-link>
    </div>
  </div>
</template>

<script>
// @ is an alias to /src
// import ___ from '@/components/___.vue'

export default {
  name: 'TabArray',
  components: {
    // ___
  },
  props: {
    pages: Array
  },
  directives: {
  }
}

</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>

.page-tabs {
  padding: 1px;
  background-color: #f7f7f7;
  width: 100%;
  font-size: 16px;
  line-height: 24px;
  font-weight: 600;
  display: flex;
  justify-content: center;
}

.page-tab {
  cursor: pointer;
  padding: 12px 24px 12px 24px;
  height: 100%;
}

.page-tab:hover {
  background-color: #eeeeee;
}

a:link {
  text-decoration: none;
  color: inherit;
}

a:visited {
  text-decoration: none;
  color: inherit;
}

.settings {
  color: grey;
}

</style>
