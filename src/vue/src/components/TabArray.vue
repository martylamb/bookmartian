<template>
  <div class='page-tabs'>
    <div v-for='(page, index) in this.pages' v-bind:key='page.name' class='page-tab'>
      <router-link v-bind:to="'/Page/' + index">{{ page.name }}</router-link>
    </div>
    <div class='page-tab settings'>
      <router-link to="/Search">tag search</router-link>
    </div>
    <div class='page-tab settings'>
      <router-link to="/Settings">bookmartian</router-link>
    </div>
    <div class='page-tab settings'>
      <font-awesome-icon :icon="['fas', 'plus']" size='lg' v-on:click="$emit('new-bookmark')" />
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

.page-tab .router-link-active {
  text-decoration: underline;
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
  color: #b3b3b3;
}

</style>
