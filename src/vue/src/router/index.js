import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
// import Settings from '../views/Settings.vue'
import Page from '../views/Page.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    children: [
      {
        path: '/Settings',
        component: () => import(/* webpackChunkName: "Settings" */ '../views/Settings.vue')
      },
      {
        path: '/Page/:page_index',
        component: Page
      }
    ]
  }
]

const router = new VueRouter({
  routes
})

export default router
