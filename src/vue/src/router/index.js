import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
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
        component: Page,
        props: true
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

export default router
