<template>
  <div class="page">
    <van-nav-bar title="常用乘客" right-text="添加" @click-right="showAdd = true" />

    <van-loading v-if="loading" class="list-loading" size="24px">加载中...</van-loading>

    <van-pull-refresh v-model="refreshing" @refresh="loadPassengers">
      <van-empty v-if="passengers.length === 0 && !loading" description="暂无常用乘客" />

      <van-swipe-cell v-for="p in passengers" :key="p.id">
        <van-cell
          :title="p.name"
          :label="`${maskIdCard(p.idCard)}  ${p.phone || ''}`"
          is-link
          @click="editPassenger(p)"
        >
          <template #icon>
            <van-icon name="contact" size="20" style="margin-right: 8px; color: #ee0a24;" />
          </template>
        </van-cell>
        <template #right>
          <van-button square type="danger" text="删除" @click="removePassenger(p.id)" />
        </template>
      </van-swipe-cell>
    </van-pull-refresh>

    <!-- 添加弹窗 -->
    <van-dialog v-model:show="showAdd" title="添加乘车人" show-cancel-button @confirm="addPassenger" confirm-button-text="添加">
      <van-form @submit.prevent>
        <van-cell-group inset>
          <van-field v-model="form.name" label="姓名" placeholder="请输入姓名" clearable />
          <van-field v-model="form.idCard" label="身份证" placeholder="请输入身份证号" clearable />
          <van-field v-model="form.phone" label="手机号" placeholder="请输入手机号" clearable />
        </van-cell-group>
      </van-form>
    </van-dialog>

    <!-- 编辑弹窗 -->
    <van-dialog v-model:show="showEdit" title="编辑乘车人" show-cancel-button @confirm="updatePassenger" confirm-button-text="保存">
      <van-form @submit.prevent>
        <van-cell-group inset>
          <van-field v-model="editForm.name" label="姓名" placeholder="请输入姓名" clearable />
          <van-field v-model="editForm.idCard" label="身份证" placeholder="请输入身份证号" clearable />
          <van-field v-model="editForm.phone" label="手机号" placeholder="请输入手机号" clearable />
        </van-cell-group>
      </van-form>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { passengerApi } from '../api/index.js'
import { maskIdCard } from '../utils/format.js'
import { showToast } from 'vant'

const userId = localStorage.getItem('userId')
const passengers = ref([])
const loading = ref(false)
const refreshing = ref(false)

const showAdd = ref(false)
const showEdit = ref(false)
const form = ref({ name: '', idCard: '', phone: '' })
const editForm = ref({ name: '', idCard: '', phone: '' })
const editingId = ref(null)

onMounted(() => loadPassengers())

async function loadPassengers() {
  loading.value = true
  try {
    const res = await passengerApi.list(userId)
    passengers.value = res.data
  } catch (e) {
    showToast('加载失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function addPassenger() {
  if (!form.value.name) { showToast('请输入姓名'); return }
  try {
    await passengerApi.add({ ...form.value, userId: Number(userId) })
    showToast('添加成功')
    form.value = { name: '', idCard: '', phone: '' }
    loadPassengers()
  } catch (e) {
    showToast(e.message)
  }
}

function editPassenger(p) {
  editingId.value = p.id
  editForm.value = { name: p.name, idCard: p.idCard, phone: p.phone }
  showEdit.value = true
}

async function updatePassenger() {
  try {
    await passengerApi.update(editingId.value, editForm.value)
    showToast('更新成功')
    loadPassengers()
  } catch (e) {
    showToast(e.message)
  }
}

async function removePassenger(id) {
  try {
    await passengerApi.remove(id)
    showToast('删除成功')
    loadPassengers()
  } catch (e) {
    showToast(e.message)
  }
}
</script>

<style scoped>
.list-loading { padding: 60px 0; }
</style>
