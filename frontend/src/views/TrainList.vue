<template>
  <div class="page">
    <!-- 搜索区域 -->
    <section class="search-hero">
      <h2 class="search-hero-title">车次查询</h2>
      <form class="search-form" @submit.prevent="handleSearch">
        <div class="search-field">
          <label>出发站</label>
          <input v-model="start" class="form-input" placeholder="如：北京南" list="station-list" />
        </div>
        <button type="button" class="swap-btn" title="交换起止站" @click="swapStations">⇄</button>
        <div class="search-field">
          <label>到达站</label>
          <input v-model="end" class="form-input" placeholder="如：上海虹桥" list="station-list" />
        </div>
        <div class="search-field search-field-sm">
          <label>出发日期</label>
          <input v-model="travelDate" class="form-input" type="date" />
        </div>
        <button type="submit" class="btn btn-primary btn-lg search-btn" :disabled="loading">
          {{ loading ? '查询中...' : '查 询' }}
        </button>
      </form>
      <datalist id="station-list">
        <option v-for="s in stations" :key="s" :value="s" />
      </datalist>
    </section>

    <!-- 结果统计 -->
    <div v-if="!loading && searched" class="result-bar">
      <span>共找到<strong>{{ trains.length }}</strong> 趟车次</span>
      <button type="button" class="link-btn" @click="resetSearch">显示全部车次</button>
    </div>

    <!-- 车次列表 -->
    <div v-if="loading" class="card"><div class="loading"><span class="spinner" /> 正在查询车次...</div></div>

    <div v-else-if="trains.length === 0" class="card empty-card">
      <div class="empty-icon">🚙</div>
      <div class="empty-title">暂无符合条件的车次</div>
      <div class="empty-desc">请调整出发站或到达站后重试</div>
    </div>

    <div v-else class="train-list">
      <div v-for="train in trains" :key="train.id" class="train-card">
        <div class="train-card-main">
          <div class="train-number">{{ train.trainNumber }}</div>
          <div class="train-route">
            <div class="station-block">
              <div class="station-date">{{ formatDate(train.departureTime) }}</div>
              <div class="station-time">{{ formatTime(train.departureTime) }}</div>
              <div class="station-name">{{ train.startStation }}</div>
            </div>
            <div class="route-mid">
              <div class="route-duration">{{ calcDuration(train.departureTime, train.arrivalTime) }}</div>
              <div class="route-line"><span /></div>
            </div>
            <div class="station-block station-block-end">
              <div class="station-date">{{ formatDate(train.arrivalTime) }}</div>
              <div class="station-time">{{ formatTime(train.arrivalTime) }}</div>
              <div class="station-name">{{ train.endStation }}</div>
            </div>
          </div>
        </div>
        <div class="train-card-side">
          <div class="price-block">
            <span class="price-label">二等座</span>
            <span class="price-value">¥{{ train.price }}</span>
          </div>
          <div class="seats-block" :class="{ 'seats-low': train.availableSeats <= 10, 'seats-none': train.availableSeats <= 0 }">
            {{ seatText(train.availableSeats) }}
          </div>
          <button
            class="btn btn-book"
            :disabled="train.availableSeats <= 0"
            @click="buyTicket(train)"
          >
            {{ train.availableSeats > 0 ? '预订' : '无票' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { trainApi } from '../api/index.js'
import { formatTime, formatDate, calcDuration } from '../utils/format.js'
import { useToast } from '../composables/useToast.js'

const router = useRouter()
const toast = useToast()

const trains = ref([])
const start = ref('')
const end = ref('')
const travelDate = ref(todayStr())
const loading = ref(false)
const searched = ref(false)
const stations = ref([])

onMounted(() => loadAll())

function todayStr() {
  return new Date().toISOString().slice(0, 10)
}

function extractStations(list) {
  const set = new Set()
  list.forEach(t => {
    if (t.startStation) set.add(t.startStation)
    if (t.endStation) set.add(t.endStation)
  })
  stations.value = [...set].sort()
}

async function loadAll() {
  loading.value = true
  searched.value = false
  try {
    const res = await trainApi.getAll()
    trains.value = res.data
    extractStations(res.data)
  } catch (e) {
    toast.error('加载车次失败：' + e.message)
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  loading.value = true
  searched.value = true
  try {
    const res = await trainApi.search(start.value.trim(), end.value.trim(), travelDate.value)
    trains.value = res.data
  } catch (e) {
    toast.error('查询失败：' + e.message)
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  start.value = ''
  end.value = ''
  loadAll()
}

function swapStations() {
  const tmp = start.value
  start.value = end.value
  end.value = tmp
}

function seatText(n) {
  if (n <= 0) return '无票'
  if (n <= 10) return `仅剩 ${n} 张`
  return `有票 ${n} 张`
}

function buyTicket(train) {
  router.push(`/buy/${train.id}`)
}
</script>
