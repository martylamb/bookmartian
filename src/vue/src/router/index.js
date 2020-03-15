import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Page from '../views/Page.vue'
import New from '../views/New.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/Page/0',
    name: 'Home',
    component: Home,
    children: [
      {
        path: '/Settings',
        component: () => import(/* webpackChunkName: "Settings" */ '../views/Settings.vue')
      },
      {
        path: '/Search',
        component: () => import(/* webpackChunkName: "Search" */ '../views/Search.vue')
      },
      {
        path: '/Page/:page_index',
        component: Page,
        props: true
      }
    ]
  },
  {
    path: '/New',
    name: 'New',
    component: New
  }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

export default router
