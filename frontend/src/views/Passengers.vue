<template>
  <div class="page">
    <div class="card">
      <div class="card-title-row">
        <div>
          <div class="card-title" style="margin:0;border:0;padding:0;">常用乘客</div>
          <p class="card-desc">测试环境添加乘客只需姓名，身份证自动生成</p>
        </div>
        <button class="btn btn-primary btn-sm" @click="openAdd">+ 添加乘客</button>
      </div>

      <div v-if="loading" class="loading"><span class="spinner" /> 加载中...</div>

      <div v-else-if="passengers.length === 0" class="empty-card">
        <div class="empty-icon">👤</div>
        <div class="empty-title">暂无常用乘客</div>
        <div class="empty-desc">添加乘客后可快速购票</div>
        <button class="btn btn-primary btn-sm" style="margin-top:12px;" @click="openAdd">立即添加</button>
      </div>

      <div v-else class="passenger-grid">
        <div v-for="p in passengers" :key="p.id" class="passenger-card">
          <div class="passenger-card-avatar">{{ p.name.charAt(0) }}</div>
          <div class="passenger-card-body">
            <div class="passenger-card-name">{{ p.name }}</div>
            <div class="passenger-card-meta">
              <span v-if="p.phone">📱 {{ p.phone }}</span>
              <span v-else class="text-muted">未填写手机号</span>
            </div>
          </div>
          <div class="passenger-card-actions">
            <button class="btn btn-outline btn-sm" @click="openEdit(p)">编辑</button>
            <button class="btn btn-danger btn-sm" @click="handleDelete(p.id)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑弹窗 -->
    <AppModal
      v-if="showModal"
      :title="editingId ? '编辑乘客' : '添加乘客'"
      @close="closeModal"
    >
      <p class="modal-hint">测试环境：姓名必填，手机号可选，身份证由系统自动生成。</p>
      <div class="form-group">
        <label>姓名 <span class="required">*</span></label>
        <input v-model="form.name" class="form-input" placeholder="请输入姓名" />
      </div>
      <div class="form-group">
        <label>手机号</label>
        <input v-model="form.phone" class="form-input" placeholder="可选" />
      </div>
      <template #footer>
        <button class="btn btn-ghost" @click="closeModal">取消</button>
        <button class="btn btn-primary" :disabled="saving" @click="handleSave">
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { passengerApi } from '../api/index.js'
import { generateTestIdCard, suggestPassengerName } from '../utils/testData.js'
import { useAuth } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'
import AppModal from '../components/AppModal.vue'

const { userId } = useAuth()
const toast = useToast()

const passengers = ref([])
const loading = ref(false)
const showModal = ref(false)
const editingId = ref(null)
const saving = ref(false)
const form = reactive({ name: '', phone: '' })

onMounted(() => loadPassengers())

async function loadPassengers() {
  loading.value = true
  try {
    const res = await passengerApi.list(userId.value)
    passengers.value = res.data
  } catch (e) {
    toast.error('加载失败：' + e.message)
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editingId.value = null
  form.name = suggestPassengerName(passengers.value.length)
  form.phone = ''
  showModal.value = true
}

function openEdit(p) {
  editingId.value = p.id
  form.name = p.name
  form.phone = p.phone || ''
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingId.value = null
}

async function handleSave() {
  const name = form.name.trim()
  if (!name) {
    toast.error('请填写乘客姓名')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await passengerApi.update(editingId.value, { name, phone: form.phone.trim() || undefined })
      toast.success('乘客信息已更新')
    } else {
      await passengerApi.add({
        userId: Number(userId.value),
        name,
        phone: form.phone.trim() || undefined,
        idCard: generateTestIdCard()
      })
      toast.success('乘客添加成功')
    }
    closeModal()
    loadPassengers()
  } catch (e) {
    toast.error(e.message)
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  if (!confirm('确定要删除该乘客吗？')) return
  try {
    await passengerApi.remove(id)
    toast.success('已删除')
    loadPassengers()
  } catch (e) {
    toast.error(e.message)
  }
}
</script>
