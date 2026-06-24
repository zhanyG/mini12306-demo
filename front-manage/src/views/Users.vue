<template>
  <div class="panel">
    <div class="panel-header">
      <div>
        <h3>用户列表</h3>
        <span class="text-muted">共 {{ users.length }} 位用户</span>
      </div>
      <button class="btn btn-primary" @click="openCreate">+ 新增用户</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="users.length === 0" class="empty">暂无用户</div>
    <div v-else class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>姓名</th>
            <th>手机号</th>
            <th>角色</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td><strong>{{ u.username }}</strong></td>
            <td>{{ u.realName || '—' }}</td>
            <td>{{ u.phone || '—' }}</td>
            <td>
              <span :class="u.role === 'ADMIN' ? 'tag tag-admin' : 'tag tag-user'">
                {{ u.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </span>
            </td>
            <td>{{ formatDateTime(u.createTime) }}</td>
            <td class="actions">
              <button class="btn btn-sm btn-outline" @click="openEdit(u)">编辑</button>
              <button
                class="btn btn-sm btn-danger"
                :disabled="u.username === 'admin'"
                :title="u.username === 'admin' ? '默认管理员不可删除' : ''"
                @click="handleDelete(u)"
              >
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <AppModal v-if="showModal" :title="editingId ? '编辑用户' : '新增用户'" @close="closeModal">
      <div class="form-group">
        <label>用户名 *</label>
        <input v-model="form.username" class="input" :disabled="editingId && form.username === 'admin'" />
      </div>
      <div class="form-group">
        <label>{{ editingId ? '新密码（留空不修改）' : '密码 *' }}</label>
        <input v-model="form.password" class="input" type="password" placeholder="不少于6位" />
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>真实姓名</label>
          <input v-model="form.realName" class="input" />
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input v-model="form.phone" class="input" />
        </div>
      </div>
      <div class="form-group">
        <label>角色</label>
        <select v-model="form.role" class="input">
          <option value="USER">普通用户</option>
          <option value="ADMIN">管理员</option>
        </select>
      </div>
      <template #footer>
        <button class="btn btn-ghost" @click="closeModal">取消</button>
        <button class="btn btn-primary" :disabled="saving" @click="handleSave">
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '../api/index.js'
import { formatDateTime } from '../utils/format.js'
import { useToast } from '../composables/useToast.js'
import AppModal from '../components/AppModal.vue'

const toast = useToast()
const users = ref([])
const loading = ref(false)
const showModal = ref(false)
const editingId = ref(null)
const saving = ref(false)

const emptyForm = () => ({ username: '', password: '', phone: '', realName: '', role: 'USER' })
const form = reactive(emptyForm())

onMounted(loadUsers)

async function loadUsers() {
  loading.value = true
  try {
    const res = await adminApi.getUsers()
    users.value = res.data.sort((a, b) => (b.id || 0) - (a.id || 0))
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  showModal.value = true
}

function openEdit(u) {
  editingId.value = u.id
  Object.assign(form, {
    username: u.username,
    password: '',
    phone: u.phone || '',
    realName: u.realName || '',
    role: u.role || 'USER'
  })
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingId.value = null
}

async function handleSave() {
  if (!form.username.trim()) {
    toast.error('请填写用户名')
    return
  }
  if (!editingId.value && (!form.password || form.password.length < 6)) {
    toast.error('密码不能少于6位')
    return
  }
  saving.value = true
  try {
    const payload = {
      username: form.username.trim(),
      phone: form.phone.trim() || undefined,
      realName: form.realName.trim() || undefined,
      role: form.role
    }
    if (form.password) payload.password = form.password

    if (editingId.value) {
      await adminApi.updateUser(editingId.value, payload)
      toast.success('用户已更新')
    } else {
      await adminApi.createUser(payload)
      toast.success('用户已创建')
    }
    closeModal()
    loadUsers()
  } catch (e) {
    toast.error(e.message)
  } finally {
    saving.value = false
  }
}

async function handleDelete(u) {
  if (!confirm(`确定删除用户 ${u.username} 吗？`)) return
  try {
    await adminApi.deleteUser(u.id)
    toast.success('已删除')
    loadUsers()
  } catch (e) {
    toast.error(e.message)
  }
}
</script>
