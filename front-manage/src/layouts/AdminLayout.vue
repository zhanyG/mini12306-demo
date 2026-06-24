<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <span>🚄</span>
        <div>
          <div class="brand-title">mini12306</div>
          <div class="brand-sub">管理后台</div>
        </div>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/" :class="{ active: isActive('/') }">📊 仪表盘</router-link>
        <router-link to="/trains" :class="{ active: isActive('/trains') }">🚆 车次管理</router-link>
        <router-link to="/orders" :class="{ active: isActive('/orders') }">📋 订单管理</router-link>
        <router-link to="/users" :class="{ active: isActive('/users') }">👥 用户管理</router-link>
        <router-link to="/notifications" :class="{ active: isActive('/notifications') }">🔔 通知管理</router-link>
      </nav>
      <div class="sidebar-footer">
        <div class="admin-user">{{ username }}</div>
        <button class="btn-logout" @click="handleLogout">退出登录</button>
      </div>
    </aside>
    <main class="main-area">
      <header class="topbar">
        <h1 class="page-title">{{ pageTitle }}</h1>
      </header>
      <div class="content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'
import { adminApi } from '../api/index.js'

const route = useRoute()
const router = useRouter()
const { username, logout } = useAuth()
const toast = useToast()

const titles = { Dashboard: '仪表盘', Trains: '车次管理', Orders: '订单管理', Users: '用户管理', Notifications: '通知管理' }
const pageTitle = computed(() => titles[route.name] || '管理后台')

function isActive(path) {
  return route.path === path
}

async function handleLogout() {
  await logout(() => adminApi.logout())
  toast.success('已退出')
  router.push('/login')
}
</script>
