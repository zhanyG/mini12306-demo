<template>
  <div class="panel">
    <div class="panel-header">
      <h3>全部订单</h3>
      <div class="header-actions">
        <select v-model="statusFilter" class="input input-sm">
          <option value="">全部状态</option>
          <option value="已出票">已出票</option>
          <option value="已退票">已退票</option>
          <option value="未支付">未支付</option>
          <option value="已支付">已支付</option>
        </select>
        <button class="btn btn-primary btn-sm" @click="openCreate">+ 代客下单</button>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="filtered.length === 0" class="empty">暂无订单</div>
    <div v-else class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>用户ID</th>
            <th>车次</th>
            <th>路线</th>
            <th>乘车人</th>
            <th>金额</th>
            <th>状态</th>
            <th>下单时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in filtered" :key="o.id">
            <td class="mono">{{ o.orderNo }}</td>
            <td>{{ o.userId }}</td>
            <td>{{ trainMap[o.trainId]?.trainNumber || o.trainId }}</td>
            <td>
              {{ trainMap[o.trainId]?.startStation || '—' }}
              →
              {{ trainMap[o.trainId]?.endStation || '—' }}
            </td>
            <td>{{ passengerMap[o.passengerId]?.name || o.passengerId }}</td>
            <td>¥{{ o.price }}</td>
            <td><span :class="orderStatusClass(o.status)">{{ o.status }}</span></td>
            <td>{{ formatDateTime(o.createTime) }}</td>
            <td class="actions">
              <button class="btn btn-sm btn-outline" @click="openEdit(o)">编辑</button>
              <button class="btn btn-sm btn-danger" @click="handleDelete(o)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 代客下单 -->
    <AppModal v-if="showCreate" title="代客下单" @close="showCreate = false">
      <p class="modal-hint">将自动扣减余票并生成已出票订单（与乘客端购票流程一致）。</p>
      <div class="form-group">
        <label>选择用户 *</label>
        <select v-model="createForm.userId" class="input" @change="onUserChange">
          <option value="">请选择用户</option>
          <option v-for="u in userList" :key="u.id" :value="u.id">
            {{ u.username }}（ID: {{ u.id }}）
          </option>
        </select>
      </div>
      <div class="form-group">
        <label>选择乘车人 *</label>
        <select v-model="createForm.passengerId" class="input" :disabled="!createForm.userId">
          <option value="">请选择乘车人</option>
          <option v-for="p in passengerList" :key="p.id" :value="p.id">{{ p.name }}</option>
        </select>
      </div>
      <div class="form-group">
        <label>选择车次 *</label>
        <select v-model="createForm.trainId" class="input">
          <option value="">请选择车次</option>
          <option v-for="t in trainList" :key="t.id" :value="t.id">
            {{ t.trainNumber }} {{ t.startStation }}→{{ t.endStation }}（余{{ t.availableSeats }}）
          </option>
        </select>
      </div>
      <template #footer>
        <button class="btn btn-ghost" @click="showCreate = false">取消</button>
        <button class="btn btn-primary" :disabled="creating" @click="handleCreate">
          {{ creating ? '提交中...' : '确认下单' }}
        </button>
      </template>
    </AppModal>

    <!-- 编辑订单 -->
    <AppModal v-if="showEdit" title="编辑订单" @close="showEdit = false">
      <div class="form-group">
        <label>订单号</label>
        <input class="input" :value="editForm.orderNo" disabled />
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>状态</label>
          <select v-model="editForm.status" class="input">
            <option value="未支付">未支付</option>
            <option value="已支付">已支付</option>
            <option value="已出票">已出票</option>
            <option value="已退票">已退票</option>
          </select>
        </div>
        <div class="form-group">
          <label>金额</label>
          <input v-model.number="editForm.price" class="input" type="number" step="0.5" min="0" />
        </div>
      </div>
      <p class="modal-hint">状态改为「已退票」将自动释放余票；改为「已出票」将尝试扣票。</p>
      <template #footer>
        <button class="btn btn-ghost" @click="showEdit = false">取消</button>
        <button class="btn btn-primary" :disabled="saving" @click="handleSave">
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { adminApi } from '../api/index.js'
import { formatDateTime, orderStatusClass } from '../utils/format.js'
import { useToast } from '../composables/useToast.js'
import AppModal from '../components/AppModal.vue'

const toast = useToast()
const orders = ref([])
const trainMap = ref({})
const passengerMap = ref({})
const userList = ref([])
const trainList = ref([])
const passengerList = ref([])
const loading = ref(false)
const statusFilter = ref('')
const showCreate = ref(false)
const showEdit = ref(false)
const creating = ref(false)
const saving = ref(false)
const editingId = ref(null)

const createForm = reactive({ userId: '', passengerId: '', trainId: '' })
const editForm = reactive({ orderNo: '', status: '', price: null })

const filtered = computed(() => {
  if (!statusFilter.value) return orders.value
  return orders.value.filter(o => o.status === statusFilter.value)
})

onMounted(loadOrders)

async function loadOrders() {
  loading.value = true
  try {
    const res = await adminApi.getOrders()
    orders.value = res.data.sort((a, b) => String(b.createTime).localeCompare(String(a.createTime)))
    await enrichData()
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function enrichData() {
  const trainIds = [...new Set(orders.value.map(o => o.trainId))]
  const passengerIds = [...new Set(orders.value.map(o => o.passengerId))]
  const tMap = {}
  const pMap = {}
  await Promise.all([
    ...trainIds.map(async id => {
      try {
        const res = await adminApi.getTrain(id)
        tMap[id] = res.data
      } catch { /* ignore */ }
    }),
    ...passengerIds.map(async id => {
      try {
        const res = await adminApi.getPassenger(id)
        pMap[id] = res.data
      } catch { /* ignore */ }
    })
  ])
  trainMap.value = tMap
  passengerMap.value = pMap
}

async function openCreate() {
  createForm.userId = ''
  createForm.passengerId = ''
  createForm.trainId = ''
  passengerList.value = []
  try {
    const [usersRes, trainsRes] = await Promise.all([
      adminApi.getUsers(),
      adminApi.getTrains()
    ])
    userList.value = usersRes.data.filter(u => u.role !== 'ADMIN')
    trainList.value = trainsRes.data
  } catch (e) {
    toast.error(e.message)
    return
  }
  showCreate.value = true
}

async function onUserChange() {
  createForm.passengerId = ''
  if (!createForm.userId) {
    passengerList.value = []
    return
  }
  try {
    const res = await adminApi.getPassengersByUser(createForm.userId)
    passengerList.value = res.data
  } catch (e) {
    toast.error(e.message)
  }
}

async function handleCreate() {
  if (!createForm.userId || !createForm.passengerId || !createForm.trainId) {
    toast.error('请完整选择用户、乘车人和车次')
    return
  }
  creating.value = true
  try {
    await adminApi.createOrder({
      userId: Number(createForm.userId),
      trainId: Number(createForm.trainId),
      passengerId: Number(createForm.passengerId)
    })
    toast.success('下单成功')
    showCreate.value = false
    loadOrders()
  } catch (e) {
    toast.error(e.message)
  } finally {
    creating.value = false
  }
}

function openEdit(o) {
  editingId.value = o.id
  editForm.orderNo = o.orderNo
  editForm.status = o.status
  editForm.price = o.price
  showEdit.value = true
}

async function handleSave() {
  saving.value = true
  try {
    await adminApi.updateOrder(editingId.value, {
      status: editForm.status,
      price: editForm.price
    })
    toast.success('订单已更新')
    showEdit.value = false
    loadOrders()
  } catch (e) {
    toast.error(e.message)
  } finally {
    saving.value = false
  }
}

async function handleDelete(o) {
  if (!confirm(`确定删除订单 ${o.orderNo} 吗？已出票订单将自动释放余票。`)) return
  try {
    await adminApi.deleteOrder(o.id)
    toast.success('已删除')
    loadOrders()
  } catch (e) {
    toast.error(e.message)
  }
}
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
.modal-hint {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 16px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
}
</style>
