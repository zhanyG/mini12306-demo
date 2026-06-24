<template>
  <div class="page page-narrow">
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span>/</span>
      <span>系统通知</span>
    </div>

    <div class="card">
      <div class="card-title">系统通知</div>

      <div v-if="loading" class="loading"><span class="spinner" /> 加载中...</div>

      <div v-else-if="list.length === 0" class="empty-card">
        <div class="empty-icon">🔔</div>
        <div class="empty-title">暂无通知</div>
      </div>

      <div v-else class="notif-list">
        <router-link
          v-for="item in list"
          :key="item.id"
          :to="`/notifications/${item.id}`"
          class="notif-item"
          :class="{ 'notif-unread': !readIds.has(item.id) }"
        >
          <div class="notif-header">
            <span class="notif-type">系统通知</span>
            <span class="notif-time">{{ formatDate(item.createTime) }}</span>
          </div>
          <div class="notif-title">{{ item.title }}</div>
          <div class="notif-preview">{{ item.content }}</div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { notificationApi } from '../api/index.js'
import { formatDate } from '../utils/format.js'
import { useAuth } from '../composables/useAuth.js'

const { userId } = useAuth()
const list = ref([])
const readIds = ref(new Set())
const loading = ref(false)

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await notificationApi.list(userId.value)
    list.value = res.data.list
    readIds.value = new Set(res.data.readIds || [])
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.notif-list {
  display: flex;
  flex-direction: column;
}
.notif-item {
  display: block;
  padding: 16px;
  border-bottom: 1px solid var(--border);
  text-decoration: none;
  color: inherit;
  transition: background 0.2s;
}
.notif-item:hover {
  background: #f8f9fa;
}
.notif-item:last-child {
  border-bottom: none;
}
.notif-unread {
  border-left: 3px solid var(--primary, #1677ff);
  padding-left: 13px;
}
.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.notif-type {
  font-size: 12px;
  color: #fff;
  background: var(--primary, #1677ff);
  padding: 2px 8px;
  border-radius: 4px;
}
.notif-time {
  font-size: 12px;
  color: var(--text-secondary, #999);
}
.notif-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 4px;
}
.notif-preview {
  font-size: 13px;
  color: var(--text-secondary, #999);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
