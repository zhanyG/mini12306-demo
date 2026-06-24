<template>
  <div class="panel">
    <div class="panel-header">
      <h3>车次列表</h3>
      <button class="btn btn-primary" @click="openCreate">+ 新增车次</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="trains.length === 0" class="empty">暂无车次</div>
    <div v-else class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>车次</th>
            <th>始发站</th>
            <th>终点站</th>
            <th>出发</th>
            <th>到达</th>
            <th>票价</th>
            <th>余票/总座</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in trains" :key="t.id">
            <td>{{ t.id }}</td>
            <td><strong>{{ t.trainNumber }}</strong></td>
            <td>{{ t.startStation }}</td>
            <td>{{ t.endStation }}</td>
            <td>{{ formatDateTime(t.departureTime) }}</td>
            <td>{{ formatDateTime(t.arrivalTime) }}</td>
            <td>¥{{ t.price }}</td>
            <td>{{ t.availableSeats }} / {{ t.totalSeats }}</td>
            <td class="actions">
              <button class="btn btn-sm btn-outline" @click="openEdit(t)">编辑</button>
              <button class="btn btn-sm btn-danger" @click="handleDelete(t)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <AppModal v-if="showModal" :title="editingId ? '编辑车次' : '新增车次'" @close="closeModal">
      <div class="form-row">
        <div class="form-group">
          <label>车次号 *</label>
          <input v-model="form.trainNumber" class="input" placeholder="如 G1001" />
        </div>
        <div class="form-group">
          <label>票价 *</label>
          <input v-model.number="form.price" class="input" type="number" step="0.5" min="0" />
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>始发站 *</label>
          <input v-model="form.startStation" class="input" placeholder="如 北京南" />
        </div>
        <div class="form-group">
          <label>终点站 *</label>
          <input v-model="form.endStation" class="input" placeholder="如 上海虹桥" />
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>出发时间 *</label>
          <input v-model="form.departureTime" class="input" type="datetime-local" />
        </div>
        <div class="form-group">
          <label>到达时间 *</label>
          <input v-model="form.arrivalTime" class="input" type="datetime-local" />
        </div>
      </div>
      <div class="form-group">
        <label>总座位数</label>
        <input v-model.number="form.totalSeats" class="input" type="number" min="1" />
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
import { adminApi } from '../api/index.js'
import { formatDateTime, toDatetimeLocal } from '../utils/format.js'
import { useToast } from '../composables/useToast.js'
import AppModal from '../components/AppModal.vue'

const toast = useToast()
const trains = ref([])
const loading = ref(false)
const showModal = ref(false)
const editingId = ref(null)
const saving = ref(false)

const emptyForm = () => ({
  trainNumber: '',
  startStation: '',
  endStation: '',
  departureTime: '',
  arrivalTime: '',
  price: null,
  totalSeats: 100
})
const form = reactive(emptyForm())

onMounted(loadTrains)

async function loadTrains() {
  loading.value = true
  try {
    const res = await adminApi.getTrains()
    trains.value = res.data
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  showModal.value = true
}

function openEdit(t) {
  editingId.value = t.id
  Object.assign(form, {
    trainNumber: t.trainNumber,
    startStation: t.startStation,
    endStation: t.endStation,
    departureTime: toDatetimeLocal(t.departureTime),
    arrivalTime: toDatetimeLocal(t.arrivalTime),
    price: t.price,
    totalSeats: t.totalSeats || 100
  })
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingId.value = null
}

function buildPayload() {
  return {
    trainNumber: form.trainNumber.trim(),
    startStation: form.startStation.trim(),
    endStation: form.endStation.trim(),
    departureTime: form.departureTime.length === 16 ? form.departureTime + ':00' : form.departureTime,
    arrivalTime: form.arrivalTime.length === 16 ? form.arrivalTime + ':00' : form.arrivalTime,
    price: form.price,
    totalSeats: form.totalSeats || 100
  }
}

async function handleSave() {
  if (!form.trainNumber || !form.startStation || !form.endStation || !form.departureTime || !form.arrivalTime || !form.price) {
    toast.error('请填写所有必填项')
    return
  }
  saving.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) {
      await adminApi.updateTrain(editingId.value, payload)
      toast.success('车次已更新')
    } else {
      await adminApi.createTrain(payload)
      toast.success('车次已创建')
    }
    closeModal()
    loadTrains()
  } catch (e) {
    toast.error(e.message)
  } finally {
    saving.value = false
  }
}

async function handleDelete(t) {
  if (!confirm(`确定删除车次 ${t.trainNumber} 吗？`)) return
  try {
    await adminApi.deleteTrain(t.id)
    toast.success('已删除')
    loadTrains()
  } catch (e) {
    toast.error(e.message)
  }
}
</script>
