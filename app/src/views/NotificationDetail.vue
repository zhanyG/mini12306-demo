<template>
  <div class="page">
    <van-nav-bar title="通知详情" left-arrow @click-left="$router.back()" />

    <van-loading v-if="loading" class="list-loading" size="24px">加载中...</van-loading>

    <div v-else-if="!item" class="page">
      <van-empty description="通知不存在" />
    </div>

    <div v-else class="detail-wrap">
      <div class="detail-header">
        <van-tag type="primary" size="medium">系统通知</van-tag>
        <span class="detail-time">{{ formatDateTime(item.createTime) }}</span>
      </div>
      <h1 class="detail-title">{{ item.title }}</h1>
      <div class="detail-content">{{ item.content }}</div>
      <div style="margin-top: 24px; text-align: center;">
        <van-button type="primary" :disabled="readDone" :loading="marking" round block @click="markAsRead">
          {{ readDone ? '已读 ✓' : '标记已读' }}
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { notificationApi } from '../api/index.js'
import { formatDateTime } from '../utils/format.js'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()
const userId = localStorage.getItem('userId')
const item = ref(null)
const loading = ref(false)
const readDone = ref(false)
const marking = ref(false)

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
  marking.value = true
  try {
    await notificationApi.markRead(route.params.id, userId)
    readDone.value = true
    showToast('已标记为已读')
    setTimeout(() => router.push('/notifications'), 600)
  } catch { /* ignore */ } finally {
    marking.value = false
  }
}
</script>

<style scoped>
.detail-wrap {
  padding: 20px 16px;
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.detail-time {
  font-size: 13px;
  color: #999;
}
.detail-title {
  font-size: 20px;
  margin: 0 0 16px;
  color: var(--text, #333);
}
.detail-content {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
}
</style>
