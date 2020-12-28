<template>
  <div class='login'>
    <div class='fixed-header'>bookmartian login</div>
    <div class='page-container'>
      <div class='page'>
        <section>
          <h1><a href='#username' />Login with username</h1>
          <div class='login-section'>
            <p class=description>
              When you login with a username, bookmartian will send you a 'magic link'. Use that link to complete the login in your current browser. We will write a single cookie to the browser cache so that you don't have to login with this browser in the future.
            </p>
            <b-field grouped>
              <b-field label='Username'>
                <b-input value='' v-model='username'>
                </b-input>
              </b-field>
            </b-field>
            <button class="button is-info" v-on:click='userLogin()'>Send me the magic link</button>
          </div>
        </section>
        <section>
          <h1><a href='#code' />Login with a code</h1>
          <div class='login-section'>
            <p class=description>
              When you login with a code, bookmartian uses it to validate your login with your current browser. We will write a single cookie to the browser cache so that you don't have to login with this browser in the future.
            </p>
            <b-field grouped>
              <b-field label='Login Code'>
                <b-input value='' v-model='code'>
                </b-input>
              </b-field>
            </b-field>
            <button class="button is-info" v-on:click='codeLogin()'>Login with this code</button>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script>
// @ is an alias to /src
// import ___ from '@/components/___.vue'
const axios = require('axios')
const qs = require('qs')

export default {
  name: 'Login',
  components: {
    // ___
  },
  props: {},
  directives: {},
  data: function () {
    return {
      username: '',
      code: ''
    }
  },
  computed: {},
  watch: {},
  methods: {
    userLogin: function () {
      console.log('Login: submitting username ' + this.username)
      axios
        .post('/login',
          qs.stringify({
            username: this.username
          }))
        .then(response => {
          // handle success
          // TODO: update UI to reflect status of submission
          console.log('Login: successful username submit')
        })
        .catch(error => {
          // handle error
          console.log(error)
        })
        .finally(function () {
          // always executed
        })
    },
    codeLogin: function () {
    }
  },
  mounted () { }
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

.description {
  margin-top: 6px;
  margin-bottom: 6px;
}

.login-section {
  margin: 20px;
}

</style>
