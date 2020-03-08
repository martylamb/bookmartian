<template>
  <div class="page">
    <h1>Configuration</h1>
    <p>URL to an external configuration file. The format of the file is described here, and the json schema is available here.</p>
    <h1>Bookmarklet</h1>
    <p>Using this bookmarklet is the easiest way to add bookmarks to the bookmartian.</p>
    <h1>Import bookmarks from a file</h1>
    <p>You can import bookmarks into Bookmartian from an existing file. The file must be formatted in the Netscape Bookmark file format.</p>
    <h1>About this project</h1>
    <p>Bookmartian is a self-hosted, personal bookmark database intended to make saving, finding, and using bookmarks simple but powerful. Designed to be easily hosted on your home machine or on a cloud server, bookmartian is self-contained with no external software dependencies. Enjoy! Marty Lamb &amp; John Mutchek</p>
    <p class="monospace aboutbox">
      <strong>project.version:</strong> <span></span><br/>
      <strong>git.remote.origin.url:</strong> <a href="">github</a><br/>
      <strong>git.commit.id.describe-short:</strong> <a href="https://github.com/martylamb/bookmartian/commit/"></a><br/>
      <strong>git.build.time:</strong> <span></span><br/>
    </p>
  </div>
</template>

<script>
// @ is an alias to /src
// import ___ from '@/components/___.vue'

export default {
  name: 'Settings',
  components: {
    // ___
  },
  props: {
  },
  directives: {
  },
  data: function () {
    return {
      about: {}
    }
  },
  mounted () {
    // retrieve about info
    const axios = require('axios')
    axios
      .get('http://localhost:4567/api/about', {
        headers: {
        }
      })
      .then(response => {
        // handle success
        this.about = response.data.data
        console.log('Retreived about info')
      })
      .catch(error => {
        // handle error
        console.log(error)
      })
      .finally(function () {
        // always executed
      })
  }
}

</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>

h1 {
  font-size: 16px;
  line-height: 24px;
  font-weight: 600;
}

.monospace, .monospace a {
    font-family:monospace;
    text-decoration: none;
    color: #767676;
}

.aboutbox {
    padding: 6px;
    border: solid #767676 1px;
    background-color: white;
}

</style>
