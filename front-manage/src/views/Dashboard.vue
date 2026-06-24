<template>
  <div v-if="loading" class="loading">加载中...</div>
  <div v-else class="dashboard">
    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon">🚆</div>
        <div class="stat-value">{{ stats.trainCount ?? 0 }}</div>
        <div class="stat-label">车次总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-value">{{ stats.userCount ?? 0 }}</div>
        <div class="stat-label">注册用户</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📋</div>
        <div class="stat-value">{{ stats.orderCount ?? 0 }}</div>
        <div class="stat-label">订单总数</div>
      </div>
      <div class="stat-card stat-card-accent">
        <div class="stat-icon">✅</div>
        <div class="stat-value">{{ stats.issuedOrderCount ?? 0 }}</div>
        <div class="stat-label">已出票订单</div>
      </div>
    </div>
    <div class="panel">
      <h3>快捷操作</h3>
      <div class="quick-actions">
        <router-link to="/trains" class="action-card">+ 管理车次</router-link>
        <router-link to="/orders" class="action-card">查看全部订单</router-link>
        <router-link to="/users" class="action-card">查看用户列表</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../api/index.js'
import { useToast } from '../composables/useToast.js'

const toast = useToast()
const stats = ref({})
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await adminApi.getStats()
    stats.value = res.data
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
})
</script>
