import Vue from 'vue'
import App from './App.vue'
import router from './router'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faSearch, faAngleRight, faEllipsisH, faPlus } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import VueMasonry from 'vue-masonry-css'
import Buefy from 'buefy'
import 'buefy/dist/buefy.css'
import VModal from 'vue-js-modal'

library.add(faSearch, faAngleRight, faEllipsisH, faPlus)

Vue.component('font-awesome-icon', FontAwesomeIcon)

Vue.use(VueMasonry)
Vue.use(VModal)
Vue.use(Buefy, {
  defaultIconComponent: 'font-awesome-icon',
  defaultIconPack: 'fas'
})

Vue.config.productionTip = false

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
