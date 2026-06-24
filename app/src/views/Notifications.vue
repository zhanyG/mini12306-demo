<template>
  <div class="page">
    <van-nav-bar title="系统通知" left-arrow @click-left="$router.back()" />

    <van-loading v-if="loading" class="list-loading" size="24px">加载中...</van-loading>

    <van-pull-refresh v-model="refreshing" @refresh="load">
      <van-empty v-if="list.length === 0 && !loading" description="暂无通知" />

      <div v-for="item in list" :key="item.id" class="notif-item" @click="$router.push(`/notifications/${item.id}`)">
        <van-cell is-link>
          <template #title>
            <div class="notif-title-row">
              <van-tag :type="readIds.has(item.id) ? 'default' : 'primary'" size="medium">{{ readIds.has(item.id) ? '已读' : '系统通知' }}</van-tag>
              <span class="notif-time">{{ formatDate(item.createTime) }}</span>
            </div>
            <div class="notif-title">{{ item.title }}</div>
            <div class="notif-preview">{{ item.content }}</div>
          </template>
        </van-cell>
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { notificationApi } from '../api/index.js'
import { formatDate } from '../utils/format.js'

const userId = localStorage.getItem('userId')
const list = ref([])
const readIds = ref(new Set())
const loading = ref(false)
const refreshing = ref(false)

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await notificationApi.list(userId)
    list.value = res.data.list || res.data
    readIds.value = new Set(res.data.readIds || [])
  } catch { /* ignore */ } finally {
    loading.value = false
    refreshing.value = false
  }
}
</script>

<style scoped>
.notif-item {
  margin: 8px 12px;
}
.notif-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.notif-time {
  font-size: 12px;
  color: #999;
}
.notif-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}
.notif-preview {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
