<template>
  <div class="page page-narrow">
    <div class="breadcrumb">
      <router-link to="/">车次查询</router-link>
      <span>/</span>
      <span>确认订单</span>
    </div>

    <div v-if="pageLoading" class="card"><div class="loading"><span class="spinner" /> 加载中...</div></div>

    <template v-else>
      <!-- 车次信息 -->
      <div v-if="train" class="card order-train-card">
        <div class="card-title">车次信息</div>
        <div class="order-train-info">
          <div class="train-number-lg">{{ train.trainNumber }}</div>
          <div class="train-route-lg">
            <div>
              <div class="time-lg">{{ formatTime(train.departureTime) }}</div>
              <div class="station-lg">{{ train.startStation }}</div>
              <div class="date-sm">{{ formatDate(train.departureTime) }}</div>
            </div>
            <div class="route-arrow">→ {{ calcDuration(train.departureTime, train.arrivalTime) }}</div>
            <div>
              <div class="time-lg">{{ formatTime(train.arrivalTime) }}</div>
              <div class="station-lg">{{ train.endStation }}</div>
              <div class="date-sm">{{ formatDate(train.arrivalTime) }}</div>
            </div>
          </div>
          <div class="order-price-row">
            <span>票价</span>
            <span class="price-highlight">¥{{ train.price }}</span>
            <span class="seats-info">余票 {{ train.availableSeats }} 张</span>
          </div>
        </div>
      </div>

      <!-- 选择乘客 -->
      <div class="card">
        <div class="card-title-row">
          <div class="card-title" style="margin:0;border:0;padding:0;">选择乘车人</div>
          <button type="button" class="btn btn-outline btn-sm" @click="showQuickAdd = true">+ 快速添加</button>
        </div>

        <div v-if="passengers.length === 0" class="empty-inline">
          暂无常用乘客，
          <router-link to="/passengers">去添加</router-link>
          或使用上方快速添加
        </div>

        <div v-else class="passenger-select-list">
          <label
            v-for="p in passengers"
            :key="p.id"
            class="passenger-option"
            :class="{ active: selectedPassengerId === p.id }"
          >
            <input v-model="selectedPassengerId" type="radio" :value="p.id" />
            <div class="passenger-info">
              <span class="passenger-name">{{ p.name }}</span>
              <span v-if="p.phone" class="passenger-meta">{{ p.phone }}</span>
            </div>
          </label>
        </div>

        <div class="buy-actions">
          <button
            class="btn btn-primary btn-lg btn-block"
            :disabled="!selectedPassengerId || buying || (train && train.availableSeats <= 0)"
            @click="handleBuy"
          >
            {{ buying ? '提交中...' : `确认购票 ¥${train?.price || ''}` }}
          </button>
          <router-link to="/" class="btn btn-ghost btn-block">返回车次列表</router-link>
        </div>
      </div>
    </template>

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
            <div class="pay-option" :class="{ selected: selectedPayType === 'CASH' }" @click="selectedPayType = 'CASH'">
              <span class="pay-icon" style="background:#969799;">模</span>
              <span>模拟支付（调试）</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showPayDialog = false">取消</button>
          <button class="btn btn-primary" :disabled="payLoading" @click="handlePay">
            {{ payLoading ? '支付中...' : `确认支付 ¥${train?.price || ''}` }}
          </button>
        </div>
      </div>
    </div>

    <!-- 快速添加乘客 -->
    <AppModal v-if="showQuickAdd" title="快速添加乘车人" @close="showQuickAdd = false">
      <p class="modal-hint">测试环境只需填写姓名，身份证将自动生成。</p>
      <div class="form-group">
        <label>姓名 <span class="required">*</span></label>
        <input v-model="quickForm.name" class="form-input" placeholder="如：张三" />
      </div>
      <div class="form-group">
        <label>手机号</label>
        <input v-model="quickForm.phone" class="form-input" placeholder="可选" />
      </div>
      <template #footer>
        <button class="btn btn-ghost" @click="showQuickAdd = false">取消</button>
        <button class="btn btn-primary" :disabled="addingPassenger" @click="handleQuickAdd">
          {{ addingPassenger ? '添加中...' : '添加并选择' }}
        </button>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { trainApi, passengerApi, orderApi } from '../api/index.js'
import { formatTime, formatDate, calcDuration } from '../utils/format.js'
import { generateTestIdCard, suggestPassengerName } from '../utils/testData.js'
import { useAuth } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'
import AppModal from '../components/AppModal.vue'

const route = useRoute()
const router = useRouter()
const { userId } = useAuth()
const toast = useToast()

const train = ref(null)
const passengers = ref([])
const selectedPassengerId = ref(null)
const pageLoading = ref(true)
const buying = ref(false)
const showQuickAdd = ref(false)
const addingPassenger = ref(false)
const quickForm = reactive({ name: '', phone: '' })

// 支付弹窗
const showPayDialog = ref(false)
const selectedPayType = ref('ALIPAY')
const payLoading = ref(false)
const pendingOrder = ref(null)

onMounted(() => loadData())

async function loadData() {
  pageLoading.value = true
  try {
    const [trainRes, passengerRes] = await Promise.all([
      trainApi.getById(route.params.trainId),
      passengerApi.list(userId.value)
    ])
    train.value = trainRes.data
    passengers.value = passengerRes.data
    if (passengerRes.data.length === 1) {
      selectedPassengerId.value = passengerRes.data[0].id
    }
  } catch (e) {
    toast.error(e.message)
    router.push('/')
  } finally {
    pageLoading.value = false
  }
}

async function handleQuickAdd() {
  const name = quickForm.name.trim() || suggestPassengerName(passengers.value.length)
  addingPassenger.value = true
  try {
    const res = await passengerApi.add({
      userId: Number(userId.value),
      name,
      phone: quickForm.phone.trim() || undefined,
      idCard: generateTestIdCard()
    })
    passengers.value.push(res.data)
    selectedPassengerId.value = res.data.id
    quickForm.name = ''
    quickForm.phone = ''
    showQuickAdd.value = false
    toast.success('乘客添加成功')
  } catch (e) {
    toast.error(e.message)
  } finally {
    addingPassenger.value = false
  }
}

async function handleBuy() {
  if (!selectedPassengerId.value) return
  buying.value = true
  try {
    const res = await orderApi.createOrder(userId.value, route.params.trainId, selectedPassengerId.value)
    pendingOrder.value = res.data
    selectedPayType.value = 'ALIPAY'
    showPayDialog.value = true
  } catch (e) {
    toast.error(e.message)
  } finally {
    buying.value = false
  }
}

async function handlePay() {
  if (!pendingOrder.value) return
  payLoading.value = true
  try {
    if (selectedPayType.value === 'ALIPAY') {
      showPayDialog.value = false
      toast.success('正在打开支付宝支付页面...')
      window.open(`/api/pay/alipay/page/${pendingOrder.value.id}`, '_blank')
      toast.success('支付完成后请在订单页查看结果')
      setTimeout(() => router.push('/orders'), 1500)
    } else {
      await orderApi.simulatePay(pendingOrder.value.id)
      toast.success('支付成功')
      showPayDialog.value = false
      setTimeout(() => router.push('/orders'), 800)
    }
  } catch (e) {
    toast.error(e.message)
  } finally {
    payLoading.value = false
  }
}
</script>

<style scoped>
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
