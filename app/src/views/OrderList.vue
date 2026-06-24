<template>
  <div class="page">
    <van-nav-bar title="我的订单" />

    <van-tabs v-model:active="activeTab" @change="loadOrders" sticky>
      <van-tab title="全部" name="all" />
      <van-tab title="未支付" name="未支付" />
      <van-tab title="已出票" name="已出票" />
      <van-tab title="已退票" name="已退票" />
    </van-tabs>

    <van-loading v-if="loading" class="list-loading" size="24px">加载中...</van-loading>

    <van-pull-refresh v-model="refreshing" @refresh="loadOrders">
      <van-empty v-if="orders.length === 0 && !loading" description="暂无订单" />

      <div v-for="o in orders" :key="o.id" class="order-card">
        <van-cell-group inset>
          <van-cell>
            <template #title>
              <span>{{ o.orderNo }}</span>
            </template>
            <template #value>
              <span :class="statusClass(o.status)">{{ o.status }}</span>
            </template>
            <template #extra v-if="o.status === '已出票'">
              <van-button size="mini" type="primary" plain style="margin-right:4px;" @click.stop="router.push(`/change-ticket/${o.id}`)">改签</van-button>
              <van-button size="mini" type="danger" plain @click.stop="handleCancel(o.id)">退票</van-button>
            </template>
            <template #extra v-else-if="o.status === '未支付'">
              <van-button size="mini" type="success" @click.stop="handlePayNow(o)">去支付</van-button>
            </template>
          </van-cell>
          <van-cell title="车次" :value="o.trainNumber || `ID:${o.trainId}`" />
          <van-cell title="乘车人" :value="o.passengerName || `ID:${o.passengerId}`" />
          <van-cell title="金额" :value="`¥${o.price}`" />
          <van-cell title="下单时间" :value="formatDateTime(o.createTime)" />
        </van-cell-group>
      </div>
    </van-pull-refresh>

    <!-- 支付弹窗 -->
    <van-dialog v-model:show="showPayDialog" title="选择支付方式" :show-cancel-button="true" :close-on-click-overlay="true" @cancel="showPayDialog = false">
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
      <template #footer>
        <van-button block type="primary" :loading="payLoading" @click="handlePay">
          确认支付 ¥{{ payingOrder?.price }}
        </van-button>
      </template>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi } from '../api/index.js'
import { formatDateTime } from '../utils/format.js'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()
const activeTab = ref('all')
const orders = ref([])
const loading = ref(false)
const refreshing = ref(false)
const userId = localStorage.getItem('userId')

// 支付弹窗
const showPayDialog = ref(false)
const selectedPayType = ref('ALIPAY')
const payLoading = ref(false)
const payingOrder = ref(null)

onMounted(() => loadOrders())

function statusClass(s) {
  if (s === '已出票') return 'status-success'
  if (s === '已退票') return 'status-cancel'
  return ''
}

async function loadOrders() {
  loading.value = true
  try {
    const res = await orderApi.list(userId)
    let list = res.data
    if (activeTab.value !== 'all') {
      list = list.filter(o => o.status === activeTab.value)
    }
    orders.value = list
  } catch (e) {
    showToast('加载订单失败')
  } finally {
    loading.value = false
    refreshing.value = false
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
      showToast('正在打开支付宝支付页面...')
      window.open(`/api/pay/alipay/page/${payingOrder.value.id}`, '_blank')
    } else {
      // 微信支付/模拟支付 → 走模拟支付
      await orderApi.simulatePay(payingOrder.value.id)
      showToast('支付成功')
      showPayDialog.value = false
      loadOrders()
    }
  } catch (e) {
    showToast(e.message || '支付失败')
  } finally {
    payLoading.value = false
  }
}

async function handleCancel(orderId) {
  try {
    await showConfirmDialog({ title: '退票', message: '确定要退票吗？' })
    await orderApi.cancel(orderId)
    showToast('退票成功')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') showToast(e.message || '退票失败')
  }
}
</script>

<style scoped>
.list-loading { padding: 60px 0; }
.order-card { margin: 12px 0; }
:deep(.status-success) { color: #07c160; font-weight: 600; }
:deep(.status-cancel) { color: #969799; }
.pay-options { padding: 8px 16px; }
.pay-option {
  display: flex; align-items: center; gap: 12px;
  padding: 12px; margin: 4px 0; border-radius: 8px;
  border: 1px solid #eee; cursor: pointer;
}
.pay-option.selected { border-color: #1989fa; background: #f0f8ff; }
.pay-icon {
  display: inline-flex; align-items: center; justify-content: center;
  width: 32px; height: 32px; border-radius: 6px;
  color: #fff; font-size: 16px; font-weight: 600;
}
</style>
