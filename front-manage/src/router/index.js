import { createRouter, createWebHistory } from 'vue-router'

function requireAdmin(to, from, next) {
  if (!localStorage.getItem('adminToken')) {
    next('/login')
  } else {
    next()
  }
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../layouts/AdminLayout.vue'),
    beforeEnter: requireAdmin,
    children: [
      { path: '', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'trains', name: 'Trains', component: () => import('../views/Trains.vue') },
      { path: 'orders', name: 'Orders', component: () => import('../views/Orders.vue') },
      { path: 'users', name: 'Users', component: () => import('../views/Users.vue') },
      { path: 'notifications', name: 'Notifications', component: () => import('../views/Notifications.vue') }
    ]
  }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
