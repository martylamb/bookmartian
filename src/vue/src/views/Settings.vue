<template>
  <div class='page'>
    <h1>Configuration</h1>
    <p>URL to an external configuration file. The format of the file is described here, and the json schema is available here.</p>

    <h1>Bookmarklet</h1>
    <p>Using <a :href='this.bookmarkFQDN'>this bookmarklet</a> is the easiest way to add bookmarks to the bookmartian.</p>

    <h1>Manage your tags</h1>
    <p>Rename, merge and recolor tags used in your bookmark collection.</p>
    <div class='tagactions'>
      <b-field grouped group-multiline>
        <b-select placeholder='Select a tag' v-model='currentTag'>
          <option
              v-for='tag in this.tagCache.entries()'
              :value='tag[0]'
              :key='tag[0]'>
              {{ tag[0] }}
          </option>
        </b-select>

        <span class='tag' :style='{ "background-color": tagBGColor(currentTag), color: tagFGColor(currentTag) }'>{{ currentTag }}</span>

        <b-field >
          <p class='control'>
            <b-input :disabled='currentTag.length==0' placeholder='#ffffff' style='width:100px' v-model='tagColor'></b-input>
          </p>
          <p class='control'>
            <b-button :disabled='currentTag.length==0' class='button is-info' v-on:click='updateTag()'>set color</b-button>
          </p>
        </b-field>

        <b-field >
          <b-input :disabled='currentTag.length==0' placeholder='new tagname' style='width:120px' ></b-input>
          <p class='control'>
            <b-button :disabled='currentTag.length==0' class='button is-info'>rename</b-button>
          </p>
        </b-field>

        <p class='control'>
          <b-button :disabled='currentTag.length==0' class='button is-danger'>delete</b-button>
        </p>
      </b-field>
    </div>

    <h1>Import bookmarks from a file</h1>
    <p>You can import bookmarks into Bookmartian from an existing file. The file must be formatted in the Netscape Bookmark file format.</p>

    <h1>About this project</h1>
    <p>Bookmartian is a self-hosted, personal bookmark database intended to make saving, finding, and using bookmarks simple but powerful. Designed to be easily hosted on your home machine or on a cloud server, bookmartian is self-contained with no external software dependencies. Enjoy! Marty Lamb &amp; John Mutchek</p>
    <p class="monospace aboutbox">
      <strong>project.version:</strong> <span>{{this.about['project.version']}}</span><br/>
      <strong>git.remote.origin.url:</strong> <a :href="this.about['git.remote.origin.url']">{{this.about['git.remote.origin.url']}}</a><br/>
      <strong>git.commit.id.describe-short:</strong> <a :href="'https://github.com/martylamb/bookmartian/commit/' + this.about['git.commit.id.describe-short']">{{this.about['git.commit.id.describe-short']}}</a><br/>
      <strong>git.build.time:</strong> <span>{{this.about['git.build.time']}}</span><br/>
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
      about: {},
      configFileUrl: String,
      tagCache: new Map(),
      currentTag: '',
      tagColor: ''
    }
  },
  computed: {
    bookmarkFQDN: function () {
      var full = location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : '')
      return "javascript:location.href='" + full + "/new?url='+encodeURIComponent(location.href)+'&title='+encodeURIComponent(document.title)"
    }
  },
  watch: {
    currentTag: function (newVal, oldVal) {
      this.tagColor = this.tagCache.get(newVal)
    }
  },
  methods: {
    getTags: function (query) {
      const axios = require('axios')
      axios
        .get('/api/tags', {
          headers: {}
        })
        .then(response => {
          // handle success
          this.tags = response.data
          console.log('Settings: retreived ' + this.tags.length + ' tags')

          // build the tag color cache
          console.log('Settings: building the tag cache')
          this.tags.forEach(tag => {
            this.tagCache.set(tag.name, tag.color)
          })
        })
        .catch(error => {
          // handle error
          console.log(error)
        })
        .finally(function () {
          // always executed
        })
    },
    getAbout: function () {
      // retrieve about info
      const axios = require('axios')
      axios
        .get('/api/about', {
          headers: {
          }
        })
        .then(response => {
          // handle success
          this.about = response.data.data
          console.log('Settings: retrieved about info')
        })
        .catch(error => {
          // handle error
          console.log(error)
        })
        .finally(function () {
          // always executed
        })
    },
    // set default tag color from cache if none specified
    tagBGColor: function (tag) {
      var color = this.tagColor
      if (color === '#000000') {
        color = '#ffffff'
      }
      return color
    },
    // set default tag color from cache if none specified
    tagFGColor: function (tag) {
      var color = this.tagColor
      if (color !== '#000000') {
        color = '#ffffff'
      }
      return color
    },
    updateTag: function () {
      console.log('Settings: saving tag ' + this.currentTag)
      const axios = require('axios')
      const qs = require('qs')
      axios
        .post('/api/tag/update',
          qs.stringify({
            name: this.currentTag,
            color: this.tagColor
          }))
        .then(response => {
          // handle success
          this.tagCache.set(this.currentTag, this.tagColor)
          console.log('Settings: successful save')
        })
        .catch(error => {
          // handle error
          console.log(error)
        })
        .finally(function () {
          // always executed
        })
    }
  },
  mounted () {
    // blank out the searchbar whenever a dashboard page is loaded
    this.$emit('search-changed', '')

    this.getTags()
    this.getAbout()
  }
}

</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>

h1 {
  font-size: 16px;
  line-height: 24px;
  font-weight: 600;
  margin-top: 24px;
}

.tagactions {
  margin: 12px 6px 6px 6px;
}

.tag {
  border: 1px solid grey;
  margin: 6px 12px 0px 0px;
  color: black;
  background-color: white;
  min-width: 100px;
}

.newTagName {
}

.monospace, .monospace a {
    font-family:monospace;
    text-decoration: none;
    color: #767676;
}

.aboutbox {
    margin-top: 48px;
    padding: 6px;
    border: solid #767676 1px;
    background-color: white;
}

</style>
