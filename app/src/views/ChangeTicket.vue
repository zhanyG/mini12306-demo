<template>
  <div class="page">
    <van-nav-bar title="改签" left-arrow @click-left="$router.back()" />

    <div class="scroll-area">
      <van-loading v-if="pageLoading" class="list-loading" size="24px">加载中...</van-loading>

      <template v-else>
        <!-- 原订单信息 -->
        <van-cell-group inset style="margin: 12px;">
          <van-cell title="当前车次" :value="currentTrain?.trainNumber" />
          <van-cell title="出发" :value="`${currentTrain?.startStation} ${formatTime(currentTrain?.departureTime)}`" />
          <van-cell title="到达" :value="`${currentTrain?.endStation} ${formatTime(currentTrain?.arrivalTime)}`" />
          <van-cell title="当前票价" :value="`¥${order?.price}`" />
        </van-cell-group>

        <!-- 查询新车次 -->
        <van-cell-group inset style="margin: 12px;">
          <van-cell title="查询新车次" />
          <van-field v-model="searchStart" label="出发站" placeholder="输入出发站" clearable />
          <van-field v-model="searchEnd" label="到达站" placeholder="输入到达站" clearable />
          <van-cell>
            <van-button block type="primary" size="small" :loading="searchLoading" @click="searchTrains">
              查询车次
            </van-button>
          </van-cell>
        </van-cell-group>

        <!-- 可选新车次 -->
        <div v-if="alternativeTrains.length > 0" style="padding: 0 16px; font-size: 14px; font-weight: 600; color: #323233;">
          可选车次（点击选择）
        </div>

        <van-card
          v-for="t in alternativeTrains"
          :key="t.id"
          :title="t.trainNumber"
          :desc="`${t.startStation} → ${t.endStation}`"
          :price="`¥${t.price}`"
          :tag="t.availableSeats > 10 ? '有票' : '少量'"
          :origin-price="calcDuration(t.departureTime, t.arrivalTime)"
          :class="{ 'selected-card': selectedTrainId === t.id }"
          @click="selectedTrainId = t.id"
        >
          <template #tags>
            <van-tag plain style="margin-right:4px;">{{ formatDate(t.departureTime) }}</van-tag>
            <van-tag plain>{{ formatTime(t.departureTime) }}-{{ formatTime(t.arrivalTime) }}</van-tag>
          </template>
          <template #footer>
            <div v-if="order && t.price !== order.price" :class="t.price > order.price ? 'price-up' : 'price-down'">
              {{ t.price > order.price ? `补差价 ¥${(t.price - order.price).toFixed(2)}` : `退款 ¥${(order.price - t.price).toFixed(2)}` }}
            </div>
          </template>
        </van-card>

        <van-empty v-if="alternativeTrains.length === 0 && !searchLoading" description="请查询车次" />

        <!-- 底部占位，防止按钮遮挡内容 -->
        <div style="height: 80px;"></div>
      </template>
    </div>

    <!-- 底部固定按钮 -->
    <div class="bottom-bar">
      <van-button
        block type="primary"
        :disabled="!selectedTrainId"
        :loading="submitting"
        @click="handleConfirm"
      >
        确认改签
      </van-button>
    </div>

    <!-- 支付弹窗 -->
    <van-dialog v-model:show="showPayDialog" title="支付补差价" :show-cancel-button="true" @cancel="showPayDialog = false">
      <div style="padding: 12px 16px; text-align: center; font-size: 14px; color: #666;">
        需补差价 <span style="color:#ee0a24;font-size:20px;font-weight:700;">¥{{ diffAmount.toFixed(2) }}</span>
      </div>
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
      <template #footer>
        <van-button block type="primary" :loading="payLoading" @click="handlePayDiff">
          支付差价 ¥{{ diffAmount.toFixed(2) }}
        </van-button>
      </template>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi, trainApi } from '../api/index.js'
import { formatTime, formatDate, calcDuration } from '../utils/format.js'
import { showToast, showConfirmDialog } from 'vant'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const currentTrain = ref(null)
const alternativeTrains = ref([])
const selectedTrainId = ref(null)
const pageLoading = ref(true)
const searchLoading = ref(false)
const loading = ref(false)
const submitting = ref(false)

const searchStart = ref('')
const searchEnd = ref('')

// 支付弹窗
const showPayDialog = ref(false)
const selectedPayType = ref('ALIPAY')
const payLoading = ref(false)
const diffAmount = ref(0)

onMounted(async () => {
  try {
    const orderRes = await orderApi.getById(route.params.orderId)
    order.value = orderRes.data

    const trainRes = await trainApi.getById(order.value.trainId)
    currentTrain.value = trainRes.data

    searchStart.value = currentTrain.value.startStation
    searchEnd.value = currentTrain.value.endStation

    // 默认查询同线路车次
    await searchTrains()
  } catch (e) {
    showToast('加载数据失败')
    router.back()
  } finally {
    pageLoading.value = false
  }
})

async function searchTrains() {
  searchLoading.value = true
  try {
    const res = await trainApi.search({
      start: searchStart.value || '',
      end: searchEnd.value || ''
    })
    let list = res.data
    if (order.value?.trainId) {
      list = list.filter(t => t.id !== Number(order.value.trainId))
    }
    list = list.filter(t => t.availableSeats > 0)
    alternativeTrains.value = list
  } catch (e) {
    showToast('查询失败')
  } finally {
    searchLoading.value = false
  }
}

async function handleConfirm() {
  if (!selectedTrainId.value) return
  const newTrain = alternativeTrains.value.find(t => t.id === selectedTrainId.value)
  if (!newTrain) return

  const oldPrice = order.value.price
  const newPrice = newTrain.price

  if (newPrice > oldPrice) {
    // 需要补差价 → 弹支付窗
    diffAmount.value = newPrice - oldPrice
    selectedPayType.value = 'ALIPAY'
    showPayDialog.value = true
  } else {
    // 同价或降价 → 直接改签
    let msg = newPrice < oldPrice ? `将退款 ¥${(oldPrice - newPrice).toFixed(2)}` : '票价相同'
    await doReschedule(newTrain, msg)
  }
}

async function handlePayDiff() {
  if (!selectedTrainId.value) return
  payLoading.value = true
  const newTrain = alternativeTrains.value.find(t => t.id === selectedTrainId.value)
  if (!newTrain) return

  const msg = `需补差价 ¥${diffAmount.value.toFixed(2)}`

  if (selectedPayType.value === 'ALIPAY') {
    // 打开支付宝支付差价页面
    showPayDialog.value = false
    showToast('正在打开支付宝...')
    window.open(`/api/pay/alipay/page-upgrade/${order.value.id}?newTrainId=${selectedTrainId.value}`, '_blank')
    // 返回后直接调用改签
    showToast('支付完成后请返回点击确认')
    payLoading.value = false
    return
  } else {
    // 模拟支付 → 直接走 payUpgrade
    try {
      await showConfirmDialog({
        title: '支付确认',
        message: `改签至 ${newTrain.trainNumber}\n${msg}\n确定支付？`
      })
      submitting.value = true
      await orderApi.payUpgrade(order.value.id, selectedTrainId.value)
      showToast('改签成功')
      setTimeout(() => router.push('/orders'), 800)
    } catch (e) {
      if (e !== 'cancel') showToast(e.message || '改签失败')
    } finally {
      submitting.value = false
      payLoading.value = false
      showPayDialog.value = false
    }
  }
}

async function doReschedule(newTrain, msg) {
  try {
    await showConfirmDialog({
      title: '改签确认',
      message: `将 ${currentTrain.value.trainNumber} 改签至 ${newTrain.trainNumber}\n${msg}\n确定改签？`
    })
    submitting.value = true
    await orderApi.reschedule(order.value.id, selectedTrainId.value)
    showToast('改签成功')
    setTimeout(() => router.push('/orders'), 800)
  } catch (e) {
    if (e !== 'cancel') showToast(e.message || '改签失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
}
.scroll-area {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 0;
}
.bottom-bar {
  position: sticky;
  bottom: 0;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.08);
  z-index: 100;
}
.list-loading { padding: 60px 0; }
.selected-card {
  border: 2px solid #1989fa;
  border-radius: 8px;
}
.price-up { color: #ee0a24; font-size: 13px; font-weight: 600; }
.price-down { color: #07c160; font-size: 13px; font-weight: 600; }
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
