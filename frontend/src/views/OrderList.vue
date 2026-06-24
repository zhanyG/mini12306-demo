<template>
  <div class="page">
    <div class="card">
      <div class="card-title-row">
        <div class="card-title" style="margin:0;border:0;padding:0;">我的订单</div>
        <div class="filter-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            class="filter-tab"
            :class="{ active: statusFilter === tab.value }"
            @click="statusFilter = tab.value"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>

      <div v-if="loading" class="loading"><span class="spinner" /> 加载订单中...</div>

      <div v-else-if="filteredOrders.length === 0" class="empty-card">
        <div class="empty-icon">📋</div>
        <div class="empty-title">暂无订单</div>
        <div class="empty-desc">
          <router-link to="/">去查询车次并购票</router-link>
        </div>
      </div>

      <div v-else class="order-list">
        <div v-for="order in filteredOrders" :key="order.id" class="order-item">
          <div class="order-item-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span :class="orderStatusClass(order.status)">{{ order.status }}</span>
          </div>
          <div class="order-item-body">
            <div class="order-train-row">
              <span class="order-train-no">{{ getTrain(order.trainId)?.trainNumber || '—' }}</span>
              <span class="order-route">
                {{ getTrain(order.trainId)?.startStation || '—' }}
                →
                {{ getTrain(order.trainId)?.endStation || '—' }}
              </span>
            </div>
            <div class="order-detail-grid">
              <div>
                <span class="detail-label">乘车人</span>
                <span>{{ getPassenger(order.passengerId)?.name || '—' }}</span>
              </div>
              <div>
                <span class="detail-label">出发时间</span>
                <span>{{ formatDateTime(getTrain(order.trainId)?.departureTime) }}</span>
              </div>
              <div>
                <span class="detail-label">金额</span>
                <span class="price-highlight">¥{{ order.price }}</span>
              </div>
              <div>
                <span class="detail-label">下单时间</span>
                <span>{{ formatDateTime(order.createTime) }}</span>
              </div>
            </div>
          </div>
          <div class="order-item-footer">
            <template v-if="order.status === '已出票'">
              <button
                class="btn btn-outline btn-sm"
                :disabled="reschedulingId === order.id"
                @click="openReschedule(order)"
              >
                {{ reschedulingId === order.id ? '改签中...' : '改签' }}
              </button>
              <button
                class="btn btn-danger btn-sm"
                :disabled="cancellingId === order.id"
                @click="handleCancel(order.id)"
              >
                {{ cancellingId === order.id ? '退票中...' : '退票' }}
              </button>
            </template>
            <template v-else-if="order.status === '未支付'">
              <button
                class="btn btn-primary btn-sm"
                @click="handlePayNow(order)"
              >
                去支付
              </button>
            </template>
            <span v-else class="order-done-hint">{{ order.status === '已退票' ? '已退票' : '—' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 支付弹窗 -->
    <div v-if="showPayDialog" class="modal-overlay" @click.self="showPayDialog = false">
      <div class="modal-box" style="max-width: 400px;">
        <div class="modal-header">
          <span class="modal-title">选择支付方式</span>
          <button class="modal-close" @click="showPayDialog = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="pay-options">
            <div class="pay-option" :class="{ selected: selectedPayType === 'ALIPAY' }" @click="selectedPayType = 'ALIPAY'">
              <span class="pay-icon" style="background:#1677ff;">支</span>
              <span>支付宝支付</span>
            </div>
            <div class="pay-option" :class="{ selected: selectedPayType === 'WECHAT' }" @click="selectedPayType = 'WECHAT'">
              <span class="pay-icon" style="background:#07c160;">微</span>
              <span>微信支付</span>
            </div>
            <div class="pay-option" :class="{ selected: selectedPayType === 'CASH' }" @click="selectedPayType = 'CASH'">
              <span class="pay-icon" style="background:#969799;">模</span>
              <span>模拟支付（调试）</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showPayDialog = false">取消</button>
          <button class="btn btn-primary" :disabled="payLoading" @click="handlePay">
            {{ payLoading ? '支付中...' : `确认支付 ¥${payingOrder?.price || ''}` }}
          </button>
        </div>
      </div>
    </div>

    <!-- 改签选车次弹窗 -->
    <div v-if="showRescheduleModal" class="modal-overlay" @click.self="closeReschedule">
      <div class="modal-box" style="width: 640px;">
        <div class="modal-header">
          <span class="modal-title">选择新车次</span>
          <button class="modal-close" @click="closeReschedule">&times;</button>
        </div>
        <div class="modal-body">
          <div class="modal-hint">
            当前订单：<strong>{{ currentTrainForReschedule?.trainNumber }}</strong>
            {{ currentTrainForReschedule?.startStation }} → {{ currentTrainForReschedule?.endStation }}
          </div>

          <!-- 搜索区域 -->
          <div class="reschedule-search">
            <input v-model="searchStart" class="form-input" placeholder="出发站" style="flex:1;" />
            <input v-model="searchEnd" class="form-input" placeholder="到达站" style="flex:1;" />
            <button class="btn btn-primary btn-sm" :disabled="searchLoading" @click="searchTrains">
              {{ searchLoading ? '搜索中...' : '搜索' }}
            </button>
          </div>

          <div v-if="loadingTrains" class="loading"><span class="spinner" /> 加载车次中...</div>

          <div v-else-if="availableTrains.length === 0" class="empty-card">
            <div class="empty-title">暂无可以改签的车次</div>
            <div class="empty-desc">请尝试修改搜索条件</div>
          </div>

          <div v-else class="reschedule-train-list">
            <div
              v-for="train in availableTrains"
              :key="train.id"
              :class="['reschedule-train-item', { 'reschedule-selected': selectedNewTrainId === train.id }]"
              @click="selectedNewTrainId = train.id"
            >
              <div class="reschedule-train-info">
                <span class="reschedule-train-no">{{ train.trainNumber }}</span>
                <span class="reschedule-route">
                  {{ train.startStation }} → {{ train.endStation }}
                </span>
                <span class="reschedule-time">{{ formatTime(train.departureTime) }} - {{ formatTime(train.arrivalTime) }}</span>
                <span class="reschedule-price">¥{{ train.price }}</span>
                <span v-if="rescheduleTarget" :class="train.price > rescheduleTarget.price ? 'diff-up' : 'diff-down'">
                  {{ train.price > rescheduleTarget.price ? `补差价 ¥${(train.price - rescheduleTarget.price).toFixed(2)}` : `退款 ¥${(rescheduleTarget.price - train.price).toFixed(2)}` }}
                </span>
              </div>
              <button
                class="btn btn-primary btn-sm"
                :disabled="selectingTrainId === train.id"
                @click.stop="confirmReschedule(train)"
              >
                {{ selectingTrainId === train.id ? '处理中...' : '选择改签' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { orderApi, trainApi, loadOrderEnrichment } from '../api/index.js'
import { formatDateTime, formatTime, orderStatusClass } from '../utils/format.js'
import { useAuth } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'

const { userId } = useAuth()
const toast = useToast()

const orders = ref([])
const trainMap = ref({})
const passengerMap = ref({})
const loading = ref(false)
const cancellingId = ref(null)
const statusFilter = ref('全部')

// 支付弹窗
const showPayDialog = ref(false)
const selectedPayType = ref('ALIPAY')
const payLoading = ref(false)
const payingOrder = ref(null)

// 改签弹窗状态
const showRescheduleModal = ref(false)
const reschedulingId = ref(null)
const rescheduleTarget = ref(null)
const availableTrains = ref([])
const loadingTrains = ref(false)
const searchLoading = ref(false)
const selectingTrainId = ref(null)
const selectedNewTrainId = ref(null)
const searchStart = ref('')
const searchEnd = ref('')

const tabs = [
  { label: '全部', value: '全部' },
  { label: '未支付', value: '未支付' },
  { label: '已出票', value: '已出票' },
  { label: '已退票', value: '已退票' }
]

const filteredOrders = computed(() => {
  if (statusFilter.value === '全部') return orders.value
  return orders.value.filter(o => o.status === statusFilter.value)
})

onMounted(() => loadOrders())

function getTrain(id) {
  return trainMap.value[id]
}

function getPassenger(id) {
  return passengerMap.value[id]
}

const currentTrainForReschedule = computed(() => {
  if (!rescheduleTarget.value) return null
  return getTrain(rescheduleTarget.value.trainId)
})

async function loadOrders() {
  loading.value = true
  try {
    const res = await orderApi.listByUser(userId.value)
    orders.value = res.data.sort((a, b) => String(b.createTime).localeCompare(String(a.createTime)))
    const enriched = await loadOrderEnrichment(orders.value)
    trainMap.value = enriched.trainMap
    passengerMap.value = enriched.passengerMap
  } catch (e) {
    toast.error('加载订单失败：' + e.message)
  } finally {
    loading.value = false
  }
}

function handlePayNow(order) {
  payingOrder.value = order
  selectedPayType.value = 'ALIPAY'
  showPayDialog.value = true
}

async function handlePay() {
  if (!payingOrder.value) return
  payLoading.value = true
  try {
    if (selectedPayType.value === 'ALIPAY') {
      showPayDialog.value = false
      toast.success('正在打开支付宝支付页面...')
      window.open(`/api/pay/alipay/page/${payingOrder.value.id}`, '_blank')
      toast.success('支付完成后请刷新页面查看')
    } else {
      await orderApi.simulatePay(payingOrder.value.id)
      toast.success('支付成功')
      showPayDialog.value = false
      payingOrder.value = null
      loadOrders()
    }
  } catch (e) {
    toast.error(e.message)
  } finally {
    payLoading.value = false
  }
}

async function handleCancel(orderId) {
  if (!confirm('确定要退票吗？退票后座位将释放。')) return
  cancellingId.value = orderId
  try {
    const res = await orderApi.cancel(orderId)
    const msg = typeof res.data === 'string' ? res.data : '退票成功'
    toast.success(msg)
    await loadOrders()
  } catch (e) {
    toast.error(e.message)
  } finally {
    cancellingId.value = null
  }
}

async function openReschedule(order) {
  rescheduleTarget.value = order
  selectedNewTrainId.value = null
  showRescheduleModal.value = true
  loadingTrains.value = true
  availableTrains.value = []

  const current = getTrain(order.trainId)
  searchStart.value = current?.startStation || ''
  searchEnd.value = current?.endStation || ''

  try {
    const res = await trainApi.search(searchStart.value, searchEnd.value, '')
    availableTrains.value = res.data.filter(t =>
      t.availableSeats > 0 && t.id !== order.trainId
    )
  } catch (e) {
    toast.error('加载可改签车次失败：' + e.message)
    closeReschedule()
  } finally {
    loadingTrains.value = false
  }
}

async function searchTrains() {
  searchLoading.value = true
  try {
    const res = await trainApi.search(searchStart.value || '', searchEnd.value || '', '')
    let list = res.data
    if (rescheduleTarget.value) {
      list = list.filter(t => t.availableSeats > 0 && t.id !== rescheduleTarget.value.trainId)
    }
    availableTrains.value = list
  } catch (e) {
    toast.error('搜索车次失败：' + e.message)
  } finally {
    searchLoading.value = false
  }
}

function closeReschedule() {
  showRescheduleModal.value = false
  rescheduleTarget.value = null
  availableTrains.value = []
  selectingTrainId.value = null
  selectedNewTrainId.value = null
}

async function confirmReschedule(train) {
  if (!rescheduleTarget.value) return
  const order = rescheduleTarget.value
  const oldPrice = order.price
  const newPrice = train.price

  if (newPrice > oldPrice) {
    const ok = confirm(`改签至 ${train.trainNumber} 需补差价 ¥${(newPrice - oldPrice).toFixed(2)}，确定改签？`)
    if (!ok) return
    selectingTrainId.value = train.id
    reschedulingId.value = order.id
    try {
      await orderApi.payUpgrade(order.id, train.id)
      toast.success('改签成功（含补差价）')
      closeReschedule()
      await loadOrders()
    } catch (e) {
      toast.error(e.message)
    } finally {
      selectingTrainId.value = null
      reschedulingId.value = null
    }
  } else {
    let msg = newPrice < oldPrice ? `可退款 ¥${(oldPrice - newPrice).toFixed(2)}` : '票价相同'
    const ok = confirm(`改签至 ${train.trainNumber}，${msg}，确定改签？`)
    if (!ok) return
    selectingTrainId.value = train.id
    reschedulingId.value = order.id
    try {
      await orderApi.reschedule(order.id, train.id)
      toast.success('改签成功')
      closeReschedule()
      await loadOrders()
    } catch (e) {
      toast.error(e.message)
    } finally {
      selectingTrainId.value = null
      reschedulingId.value = null
    }
  }
}
</script>

<style scoped>
.reschedule-train-list {
  max-height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}

.reschedule-train-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.reschedule-train-item:hover {
  border-color: var(--primary);
}

.reschedule-selected {
  border-color: var(--primary) !important;
  background: #f0f8ff;
}

.reschedule-search {
  display: flex;
  gap: 8px;
  margin: 12px 0;
  align-items: center;
}

.reschedule-train-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
}

.reschedule-train-no {
  font-weight: 600;
  font-size: 15px;
  color: var(--primary);
}

.reschedule-route {
  color: var(--text);
}

.reschedule-time {
  color: var(--text-secondary);
  font-size: 13px;
}

.reschedule-price {
  color: var(--accent);
  font-weight: 600;
}

.diff-up {
  color: #ee0a24;
  font-size: 12px;
  font-weight: 600;
}

.diff-down {
  color: #07c160;
  font-size: 12px;
  font-weight: 600;
}

.pay-options {
  padding: 8px 0;
}
.pay-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  margin: 4px 0;
  border-radius: 8px;
  border: 1px solid #eee;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}
.pay-option.selected {
  border-color: #1677ff;
  background: #f0f8ff;
}
.pay-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}
</style>
