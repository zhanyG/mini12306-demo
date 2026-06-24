<template>
  <div class="page">
    <van-nav-bar title="我的账单" left-arrow @click-left="$router.back()" />

    <van-loading v-if="loading" class="list-loading" size="24px">加载中...</van-loading>

    <van-pull-refresh v-model="refreshing" @refresh="loadBills">
      <van-empty v-if="bills.length === 0 && !loading" description="暂无账单" />

      <div v-for="b in bills" :key="b.id" class="bill-card">
        <van-cell-group inset>
          <van-cell>
            <template #title>
              <div class="bill-header">
                <van-tag :type="tagType(b.type)" size="medium">{{ labelMap[b.type] || b.type }}</van-tag>
                <span :class="amountClass(b.type)">{{ b.type === 'PAYMENT' || b.type === 'CHANGE_UPGRADE' ? '-' : '+' }}¥{{ b.amount.toFixed(2) }}</span>
              </div>
            </template>
          </van-cell>
          <van-cell :title="b.description" :value="formatDateTime(b.createTime)" />
        </van-cell-group>
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { billApi } from '../api/index.js'
import { formatDateTime } from '../utils/format.js'
import { showToast } from 'vant'

const bills = ref([])
const loading = ref(false)
const refreshing = ref(false)
const userId = localStorage.getItem('userId')

const labelMap = {
  PAYMENT: '购票付款',
  REFUND: '退票退款',
  CHANGE_UPGRADE: '改签补差价',
  CHANGE_REFUND: '改签退款'
}

onMounted(() => loadBills())

function tagType(type) {
  if (type === 'PAYMENT' || type === 'CHANGE_UPGRADE') return 'danger'
  if (type === 'REFUND' || type === 'CHANGE_REFUND') return 'success'
  return 'default'
}

function amountClass(type) {
  if (type === 'PAYMENT' || type === 'CHANGE_UPGRADE') return 'amount-expense'
  return 'amount-income'
}

async function loadBills() {
  loading.value = true
  try {
    const res = await billApi.listByUser(userId)
    bills.value = res.data
  } catch (e) {
    showToast('加载账单失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}
</script>

<style scoped>
.list-loading { padding: 60px 0; }
.bill-card { margin: 12px 0; }
.bill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.amount-expense {
  color: #ee0a24;
  font-size: 16px;
  font-weight: 600;
}
.amount-income {
  color: #07c160;
  font-size: 16px;
  font-weight: 600;
}
</style>
