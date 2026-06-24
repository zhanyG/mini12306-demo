import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { guest: true } },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue'), meta: { guest: true } },
  { path: '/', name: 'TrainList', component: () => import('../views/TrainList.vue'), meta: { auth: true } },
  { path: '/buy/:trainId', name: 'BuyTicket', component: () => import('../views/BuyTicket.vue'), meta: { auth: true } },
  { path: '/change-ticket/:orderId', name: 'ChangeTicket', component: () => import('../views/ChangeTicket.vue'), meta: { auth: true } },
  { path: '/orders', name: 'OrderList', component: () => import('../views/OrderList.vue'), meta: { auth: true } },
  { path: '/passengers', name: 'Passengers', component: () => import('../views/Passengers.vue'), meta: { auth: true } },
  { path: '/profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { auth: true } },
  { path: '/bills', name: 'Bills', component: () => import('../views/Bills.vue'), meta: { auth: true } },
  { path: '/notifications', name: 'Notifications', component: () => import('../views/Notifications.vue'), meta: { auth: true } },
  { path: '/notifications/:id', name: 'NotificationDetail', component: () => import('../views/NotificationDetail.vue'), meta: { auth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.auth && !token) {
    next('/login')
  } else if (to.meta.guest && token) {
    next('/')
  } else {
    next()
  }
})

export default router
