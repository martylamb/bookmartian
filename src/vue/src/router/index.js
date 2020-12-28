import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Page from '../views/Page.vue'
import New from '../views/New.vue'
import Login from '../views/Login.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/page/0',
    name: 'Home',
    component: Home,
    children: [
      {
        path: '/settings',
        component: () => import(/* webpackChunkName: "Settings" */ '../views/Settings.vue'),
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/search',
        component: () => import(/* webpackChunkName: "Search" */ '../views/Search.vue'),
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/page/:page_index',
        component: Page,
        props: true,
        meta: {
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/new',
    name: 'New',
    component: New,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/logout',
    name: 'Logout',
    props: { logout: true },
    component: Login
  }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

router.beforeEach((to, from, next) => {
  // TODO: actually implement an auth'ed user check here
  const authenticatedUser = true
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

  // Check for protected route
  if (requiresAuth && !authenticatedUser) next('login')
  else next()
})

export default router
