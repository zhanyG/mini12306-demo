<template>
  <div class="panel">
    <div class="panel-header">
      <h3>通知列表</h3>
      <button class="btn btn-primary" @click="openCreate">+ 新增通知</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="list.length === 0" class="empty">暂无通知</div>
    <div v-else class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>内容</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.id }}</td>
            <td><strong>{{ item.title }}</strong></td>
            <td class="content-cell">{{ item.content }}</td>
            <td>{{ formatDateTime(item.createTime) }}</td>
            <td class="actions">
              <button class="btn btn-sm btn-danger" @click="handleDelete(item)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <AppModal v-if="showModal" title="新增通知" @close="closeModal">
      <div class="form-group">
        <label>标题 *</label>
        <input v-model="form.title" class="input" placeholder="通知标题" />
      </div>
      <div class="form-group">
        <label>内容 *</label>
        <textarea v-model="form.content" class="input" rows="5" placeholder="通知内容" style="resize:vertical;"></textarea>
      </div>
      <template #footer>
        <button class="btn btn-ghost" @click="closeModal">取消</button>
        <button class="btn btn-primary" :disabled="saving" @click="handleCreate">
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../api/index.js'
import { useToast } from '../composables/useToast.js'
import { formatDateTime } from '../utils/format.js'
import AppModal from '../components/AppModal.vue'

const toast = useToast()

const list = ref([])
const loading = ref(false)
const showModal = ref(false)
const saving = ref(false)
const form = ref({ title: '', content: '' })

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await adminApi.getNotifications()
    list.value = res.data
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = { title: '', content: '' }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  form.value = { title: '', content: '' }
}

async function handleCreate() {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    toast.error('请填写标题和内容')
    return
  }
  saving.value = true
  try {
    await adminApi.createNotification(form.value)
    toast.success('通知已发送')
    closeModal()
    load()
  } catch (e) {
    toast.error(e.message)
  } finally {
    saving.value = false
  }
}

async function handleDelete(item) {
  if (!confirm(`确定删除通知「${item.title}」？`)) return
  try {
    await adminApi.deleteNotification(item.id)
    toast.success('已删除')
    load()
  } catch (e) {
    toast.error(e.message)
  }
}
</script>

<style scoped>
.content-cell {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
