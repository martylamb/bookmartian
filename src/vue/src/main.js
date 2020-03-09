import Vue from 'vue'
import App from './App.vue'
import router from './router'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faSearch, faAngleRight, faEllipsisH } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import VueMasonry from 'vue-masonry-css'
import { Dropdown } from 'buefy'
import 'buefy/dist/buefy.css'

Vue.use(VueMasonry)
Vue.use(Dropdown)

library.add(faSearch, faAngleRight, faEllipsisH)

Vue.component('font-awesome-icon', FontAwesomeIcon)

Vue.config.productionTip = false

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
