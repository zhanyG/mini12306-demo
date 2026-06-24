<template>
  <header class="navbar">
    <router-link to="/" class="navbar-brand">
      <span class="brand-icon">🚄</span>
      <span class="brand-text">mini12306</span>
      <span v-if="loggedIn" class="brand-badge">测试版</span>
    </router-link>

    <nav v-if="loggedIn" class="navbar-links">
      <router-link to="/">车次查询</router-link>
      <router-link to="/orders">我的订单</router-link>
      <router-link to="/passengers">常用乘客</router-link>
      <router-link to="/notifications" class="nav-notif">
        通知
        <span v-if="notifCount > 0" class="notif-badge">{{ notifCount > 99 ? '99+' : notifCount }}</span>
      </router-link>
      <router-link to="/profile">个人中心</router-link>
    </nav>

    <div v-if="loggedIn" class="navbar-right">
      <span class="user-greeting">你好，{{ username }}</span>
      <button class="btn-logout" @click="handleLogout">退出</button>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'
import { notificationApi } from '../api/index.js'

const router = useRouter()
const { userId, username, loggedIn, logout } = useAuth()
const toast = useToast()

const notifCount = ref(0)

onMounted(() => loadNotifCount())

async function loadNotifCount() {
  try {
    const res = await notificationApi.unreadCount(userId.value)
    notifCount.value = res.data.unreadCount
  } catch { /* ignore */ }
}

function handleLogout() {
  logout()
  toast.success('已退出登录')
  router.push('/login')
}
</script>
