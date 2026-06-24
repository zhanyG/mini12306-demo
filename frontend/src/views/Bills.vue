<template>
  <div class="page page-narrow">
    <div class="card">
      <div class="card-title">我的账单</div>

      <div v-if="loading" class="loading"><span class="spinner" /> 加载中...</div>

      <div v-else-if="bills.length === 0" class="empty-card">
        <div class="empty-icon">💰</div>
        <div class="empty-title">暂无账单</div>
        <div class="empty-desc">购票后会自动生成账单记录</div>
      </div>

      <div v-else class="bill-list">
        <div v-for="b in bills" :key="b.id" class="bill-item">
          <div class="bill-item-top">
            <span :class="'bill-type bill-type-' + b.type.toLowerCase()">{{ typeLabel(b.type) }}</span>
            <span :class="'bill-amount ' + (isIncome(b.type) ? 'amount-income' : 'amount-expense')">
              {{ isIncome(b.type) ? '+' : '−' }}¥{{ b.amount.toFixed(2) }}
            </span>
          </div>
          <div class="bill-item-desc">{{ b.description || b.billNo }}</div>
          <div class="bill-item-bottom">
            <span class="bill-no">{{ b.billNo }}</span>
            <span class="bill-time">{{ formatDateTime(b.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { billApi } from '../api/index.js'
import { formatDateTime } from '../utils/format.js'
import { useAuth } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'

const { userId } = useAuth()
const toast = useToast()

const bills = ref([])
const loading = ref(false)

function typeLabel(type) {
  const map = {
    PAYMENT: '购票付款',
    REFUND: '退票退款',
    CHANGE_UPGRADE: '改签补差价',
    CHANGE_REFUND: '改签退款'
  }
  return map[type] || type
}

function isIncome(type) {
  return type === 'REFUND' || type === 'CHANGE_REFUND'
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await billApi.listByUser(userId.value)
    bills.value = res.data
  } catch (e) {
    toast.error('加载账单失败：' + e.message)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.bill-list {
  display: flex;
  flex-direction: column;
}

.bill-item {
  padding: 14px 0;
  border-bottom: 1px solid var(--border);
}

.bill-item:last-child {
  border-bottom: none;
}

.bill-item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.bill-type {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.bill-type-payment { background: #e8f4fd; color: #1677ff; }
.bill-type-refund { background: #f0faf0; color: #07c160; }
.bill-type-change_upgrade { background: #fff2e8; color: #fa541c; }
.bill-type-change_refund { background: #f0faf0; color: #07c160; }

.bill-amount {
  font-size: 18px;
  font-weight: 700;
}

.amount-expense { color: #ee0a24; }
.amount-income { color: #07c160; }

.bill-item-desc {
  font-size: 14px;
  color: var(--text);
  margin-bottom: 4px;
}

.bill-item-bottom {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
}

.bill-no {
  font-family: monospace;
}
</style>
