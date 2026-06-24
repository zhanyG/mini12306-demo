<template>
  <div class="page page-narrow">
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span>/</span>
      <router-link to="/notifications">系统通知</router-link>
      <span>/</span>
      <span>详情</span>
    </div>

    <div v-if="loading" class="card"><div class="loading"><span class="spinner" /> 加载中...</div></div>

    <div v-else-if="!item" class="card">
      <div class="empty-card">
        <div class="empty-title">通知不存在</div>
        <router-link to="/notifications" class="btn btn-outline">返回列表</router-link>
      </div>
    </div>

    <div v-else class="card">
      <div class="detail-header">
        <span class="detail-type">系统通知</span>
        <span class="detail-time">{{ formatDateTime(item.createTime) }}</span>
      </div>
      <h1 class="detail-title">{{ item.title }}</h1>
      <div class="detail-content">{{ item.content }}</div>
      <div class="detail-footer">
        <router-link to="/notifications" class="btn btn-outline">返回列表</router-link>
        <button class="btn btn-primary" :disabled="readDone" @click="markAsRead">
          {{ readDone ? '已读 ✓' : '标记已读' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { notificationApi } from '../api/index.js'
import { formatDateTime } from '../utils/format.js'
import { useAuth } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'

const route = useRoute()
const router = useRouter()
const { userId } = useAuth()
const toast = useToast()
const item = ref(null)
const loading = ref(false)
const readDone = ref(false)

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await notificationApi.getById(route.params.id)
    item.value = res.data
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

async function markAsRead() {
  try {
    await notificationApi.markRead(route.params.id, userId.value)
    readDone.value = true
    toast.success('已标记为已读')
    setTimeout(() => router.push('/notifications'), 600)
  } catch (e) {
    toast.error(e.message)
  }
}
</script>

<style scoped>
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.detail-type {
  font-size: 12px;
  color: #fff;
  background: var(--primary, #1677ff);
  padding: 2px 8px;
  border-radius: 4px;
}
.detail-time {
  font-size: 13px;
  color: var(--text-secondary, #999);
}
.detail-title {
  font-size: 20px;
  margin: 0 0 16px;
  color: var(--text);
}
.detail-content {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
}
.detail-footer {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--border);
}
</style>
