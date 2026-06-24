<template>
  <div class="app-container">
    <router-view />
    <van-tabbar v-if="showTabbar" v-model="active" @change="onTabChange" route fixed>
      <van-tabbar-item icon="search" to="/">车次查询</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/orders">我的订单</van-tabbar-item>
      <van-tabbar-item icon="bell-o" :badge="notifBadge" to="/notifications">通知</van-tabbar-item>
      <van-tabbar-item icon="contact" to="/passengers">乘客</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { notificationApi } from './api/index.js'

const route = useRoute()
const active = ref(0)

const showTabbar = computed(() => {
  return !['Login', 'Register'].includes(route.name)
})

const notifCount = ref(0)
const notifBadge = computed(() => notifCount.value > 0 ? (notifCount.value > 99 ? '99+' : notifCount.value) : '')

onMounted(() => loadNotifCount())

async function loadNotifCount() {
  const uid = localStorage.getItem('userId')
  if (!uid) return
  try {
    const res = await notificationApi.unreadCount(uid)
    notifCount.value = res.data.unreadCount
  } catch { /* ignore */ }
}

function onTabChange(index) {
  active.value = index
}
</script>

<style scoped>
.app-container {
  min-height: 100vh;
}
</style>
