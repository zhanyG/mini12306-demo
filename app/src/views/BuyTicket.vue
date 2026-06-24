<template>
  <div class="page">
    <van-nav-bar title="购票确认" left-arrow @click-left="$router.back()" />

    <van-loading v-if="pageLoading" class="list-loading" size="24px">加载中...</van-loading>

    <template v-else>
      <!-- 车次信息 -->
      <van-cell-group inset style="margin: 12px;">
        <van-cell title="车次" :value="train?.trainNumber" />
        <van-cell title="出发" :value="`${train?.startStation} ${formatTime(train?.departureTime)}`" />
        <van-cell title="到达" :value="`${train?.endStation} ${formatTime(train?.arrivalTime)}`" />
        <van-cell title="票价" :value="`¥${train?.price}`" />
        <van-cell title="余票" :value="`${train?.availableSeats} 张`" />
      </van-cell-group>

      <!-- 选择乘车人 -->
      <van-cell-group inset style="margin: 12px;">
        <van-cell title="选择乘车人" />
        <van-radio-group v-model="selectedPassengerId">
          <van-cell
            v-for="p in passengers"
            :key="p.id"
            :title="p.name"
            :label="`身份证: ${maskIdCard(p.idCard)}`"
            clickable
            @click="selectedPassengerId = p.id"
          >
            <template #right-icon>
              <van-radio :name="p.id" />
            </template>
          </van-cell>
        </van-radio-group>
        <van-cell v-if="passengers.length === 0" title="暂未添加乘车人" />

        <van-cell>
          <van-button plain hairline size="small" @click="showAddPassenger = true">
            + 添加乘车人
          </van-button>
        </van-cell>
      </van-cell-group>

      <div style="margin: 20px 16px;">
        <van-button block type="primary" @click="handleBuy" :loading="buyLoading" :disabled="!selectedPassengerId">
          提交订单 ¥{{ train?.price }}
        </van-button>
      </div>
    </template>

    <!-- 支付弹窗 -->
    <van-dialog v-model:show="showPayDialog" title="选择支付方式" :show-cancel-button="false" :close-on-click-overlay="false">
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
          确认支付 ¥{{ order?.price || train?.price }}
        </van-button>
      </template>
    </van-dialog>

    <!-- 添加乘车人弹窗 -->
    <van-dialog v-model:show="showAddPassenger" title="添加乘车人" show-cancel-button @confirm="addPassenger" confirm-button-text="添加">
      <van-form @submit.prevent>
        <van-cell-group inset>
          <van-field v-model="newPassenger.name" label="姓名" placeholder="请输入姓名" clearable />
          <van-field v-model="newPassenger.idCard" label="身份证" placeholder="请输入身份证号" clearable />
          <van-field v-model="newPassenger.phone" label="手机号" placeholder="请输入手机号" clearable />
        </van-cell-group>
      </van-form>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { trainApi, passengerApi, orderApi } from '../api/index.js'
import { formatTime, maskIdCard } from '../utils/format.js'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()

const train = ref(null)
const order = ref(null)
const passengers = ref([])
const selectedPassengerId = ref(null)
const pageLoading = ref(true)
const buyLoading = ref(false)
const showAddPassenger = ref(false)
const newPassenger = ref({ name: '', idCard: '', phone: '' })
const showPayDialog = ref(false)
const selectedPayType = ref('ALIPAY')
const payLoading = ref(false)

const userId = localStorage.getItem('userId')

onMounted(async () => {
  try {
    const [trainRes, passRes] = await Promise.all([
      trainApi.getById(route.params.trainId),
      passengerApi.list(userId)
    ])
    train.value = trainRes.data
    passengers.value = passRes.data
  } catch (e) {
    showToast('加载数据失败')
    router.back()
  } finally {
    pageLoading.value = false
  }
})

async function addPassenger() {
  if (!newPassenger.value.name) {
    showToast('请输入姓名')
    return
  }
  try {
    const res = await passengerApi.add({ ...newPassenger.value, userId: Number(userId) })
    passengers.value.push(res.data)
    showToast('添加成功')
    newPassenger.value = { name: '', idCard: '', phone: '' }
  } catch (e) {
    showToast(e.message)
  }
}

async function handleBuy() {
  if (!selectedPassengerId.value) {
    showToast('请选择乘车人')
    return
  }
  buyLoading.value = true
  try {
    const res = await orderApi.create({
      userId: Number(userId),
      trainId: Number(route.params.trainId),
      passengerId: selectedPassengerId.value
    })
    order.value = res.data
    showPayDialog.value = true
  } catch (e) {
    showToast(e.message || '创建订单失败')
  } finally {
    buyLoading.value = false
  }
}

async function handlePay() {
  if (!order.value) return
  payLoading.value = true
  try {
    if (selectedPayType.value === 'ALIPAY') {
      // 打开支付宝沙箱支付页面（显示原价但实付0.10元）
      showPayDialog.value = false
      showToast('正在打开支付宝支付页面...')
      window.open(`/api/pay/alipay/page/${order.value.id}`, '_blank')
      setTimeout(() => router.push('/orders'), 500)
    } else {
      // 微信支付/现金 → 直接模拟支付
      await orderApi.simulatePay(order.value.id)
      showToast('支付成功')
      showPayDialog.value = false
      setTimeout(() => router.push('/orders'), 800)
    }
  } catch (e) {
    showToast(e.message || '支付失败')
  } finally {
    payLoading.value = false
  }
}
</script>

<style scoped>
.list-loading { padding: 60px 0; }
.pay-options {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.pay-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #ebedf0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}
.pay-option.selected {
  border-color: #1989fa;
  background: #f0f7ff;
}
.pay-icon {
  display: inline-flex;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
</style>
