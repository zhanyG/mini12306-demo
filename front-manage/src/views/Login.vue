<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <span class="login-icon">🛡️</span>
        <h1>管理员登录</h1>
        <p>mini12306 后台管理系统</p>
      </div>
      <div class="login-hint">默认账号：<code>admin</code> / <code>admin123</code></div>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" class="input" required autocomplete="username" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" class="input" type="password" required autocomplete="current-password" />
        </div>
        <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>
      <button type="button" class="link-btn" @click="fillDemo">一键填充测试账号</button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '../api/index.js'
import { useAuth } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'

const router = useRouter()
const { setAuth } = useAuth()
const toast = useToast()
const form = reactive({ username: '', password: '' })
const error = ref('')
const loading = ref(false)

function fillDemo() {
  form.username = 'admin'
  form.password = 'admin123'
}

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const res = await adminApi.login(form)
    setAuth(res.data)
    toast.success('登录成功')
    router.push('/')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>
