<template>
  <div class="page">
    <van-nav-bar title="车次查询" />

    <!-- 搜索区域 -->
    <van-search
      v-model="startStation"
      placeholder="出发站"
      clearable
      @search="handleSearch"
    />
    <div class="switch-row">
      <van-search
        v-model="endStation"
        placeholder="到达站"
        clearable
        @search="handleSearch"
      />
      <van-button icon="exchange" size="small" class="switch-btn" @click="switchStations" />
    </div>
    <div style="padding:0 16px 8px;font-size:12px;color:#969799;">
      可选车站：北京南、北京西、上海虹桥、广州南、深圳北、武汉、杭州东、南京南、成都东、西安北、郑州东、长沙南、重庆北、合肥南、福州南、沈阳北、哈尔滨西、贵阳北、昆明南
    </div>

    <van-cell-group inset style="margin: 0 12px 12px;">
      <van-field
        v-model="dateStr"
        is-link
        readonly
        label="出发日期"
        placeholder="选择日期"
        @click="showDatePicker = true"
      />
    </van-cell-group>

    <div style="display:flex;justify-content:center;margin-bottom:12px;">
      <van-button round type="primary" size="small" @click="handleSearch" style="width:200px;">
        查询车次
      </van-button>
    </div>

    <!-- 日期选择器 -->
    <van-popup v-model:show="showDatePicker" position="bottom">
      <van-date-picker
        v-model="selectedDate"
        title="选择日期"
        :min-date="minDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>

    <!-- 加载中 -->
    <van-loading v-if="loading" class="list-loading" size="24px">加载中...</van-loading>

    <!-- 车次列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="loadTrains">
      <div v-if="trains.length === 0 && !loading" style="text-align: center; padding: 60px 0; color: #969799;">
        <van-icon name="search" size="48" style="display:block;margin-bottom:12px;" />
        <div style="font-size:16px;margin-bottom:8px;">查询车次</div>
        <div style="font-size:13px;">输入出发站、到达站或选择日期后查询</div>
      </div>

      <van-card
        v-for="t in trains"
        :key="t.id"
        :title="t.trainNumber"
        :desc="`${t.startStation} → ${t.endStation}`"
        :price="`¥${t.price}`"
        :tag="t.availableSeats > 10 ? '有票' : t.availableSeats > 0 ? '少量' : '无票'"
        :origin-price="calcDuration(t.departureTime, t.arrivalTime)"
        :thumb="getThumb(t.trainNumber)"
        @click="goBuy(t)"
      >
        <template #tags>
          <van-tag plain style="margin-right:4px;">{{ formatDate(t.departureTime) }}</van-tag>
          <van-tag plain>{{ formatTime(t.departureTime) }}-{{ formatTime(t.arrivalTime) }}</van-tag>
        </template>
      </van-card>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { trainApi } from '../api/index.js'
import { formatTime, formatDate, calcDuration } from '../utils/format.js'
import { showToast } from 'vant'

const router = useRouter()
const trains = ref([])
const loading = ref(false)
const refreshing = ref(false)

const startStation = ref('')
const endStation = ref('')
const dateStr = ref('')
const showDatePicker = ref(false)
const selectedDate = ref([])
const minDate = new Date(2025, 0, 1)

// 不自动加载全部车次（数据量太大），等用户主动搜索
const stations = [
  '北京南', '北京西', '上海虹桥', '南京南', '杭州东', '广州南',
  '深圳北', '武汉', '成都东', '重庆北', '西安北', '长沙南',
  '郑州东', '合肥南', '福州南', '沈阳北', '哈尔滨西', '贵阳北', '昆明南'
]

function getThumb(num) {
  const c = num.charAt(0)
  if (c === 'G') return 'https://img.yzcdn.cn/vant/custom-icon-fire.png'
  if (c === 'D') return 'https://img.yzcdn.cn/vant/custom-icon-fire.png'
  return ''
}

function switchStations() {
  const tmp = startStation.value
  startStation.value = endStation.value
  endStation.value = tmp
}

function onDateConfirm({ selectedValues }) {
  // 补零：["2026","6","15"] → "2026-06-15"
  const pad = (v) => String(v).padStart(2, '0')
  dateStr.value = selectedValues.map((v, i) => i === 0 ? v : pad(v)).join('-')
  showDatePicker.value = false
}

async function loadTrains() {
  const params = {}
  if (startStation.value) params.start = startStation.value
  if (endStation.value) params.end = endStation.value
  if (dateStr.value) params.date = dateStr.value

  // 必须有搜索条件，避免加载全部车次
  if (Object.keys(params).length === 0) {
    showToast('请至少输入出发站、到达站或选择日期')
    return
  }

  loading.value = true
  try {
    const res = await trainApi.search(params)
    trains.value = res.data
  } catch (e) {
    showToast(e.message)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function handleSearch() {
  loadTrains()
}

function goBuy(train) {
  if (train.availableSeats <= 0) {
    showToast('该车次已售罄')
    return
  }
  router.push(`/buy/${train.id}`)
}
</script>

<style scoped>
.switch-row {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-right: 12px;
}
.switch-btn {
  flex-shrink: 0;
  margin-bottom: 8px;
}
.list-loading {
  padding: 60px 0;
}
</style>
