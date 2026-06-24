import { createRouter, createWebHistory } from 'vue-router'

function requireAuth(to, from, next) {
  if (!localStorage.getItem('token')) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
}

function guestOnly(to, from, next) {
  if (localStorage.getItem('token')) {
    next('/')
  } else {
    next()
  }
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    beforeEnter: guestOnly
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    beforeEnter: guestOnly
  },
  {
    path: '/',
    name: 'Trains',
    component: () => import('../views/TrainList.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/buy/:trainId',
    name: 'BuyTicket',
    component: () => import('../views/BuyTicket.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/OrderList.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/passengers',
    name: 'Passengers',
    component: () => import('../views/Passengers.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/bills',
    name: 'Bills',
    component: () => import('../views/Bills.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('../views/Notifications.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/notifications/:id',
    name: 'NotificationDetail',
    component: () => import('../views/NotificationDetail.vue'),
    beforeEnter: requireAuth
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
