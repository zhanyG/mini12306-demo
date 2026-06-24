<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-logo">🚄</div>
      <h1 class="auth-title">欢迎登录 mini12306</h1>
      <p class="auth-subtitle">中国铁路售票系统 · 测试环境</p>

      <div class="test-hint">
        <strong>测试提示：</strong>无需真实身份证，注册时填写用户名和密码即可体验完整流程。
      </div>

      <div v-if="error" class="message message-error">{{ error }}</div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" class="form-input" placeholder="请输入用户名" autocomplete="username" required />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" class="form-input" type="password" placeholder="不少于6位" autocomplete="current-password" required />
        </div>
        <button type="submit" class="btn btn-primary btn-block btn-lg" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <div class="quick-fill">
        <span>快速填充：</span>
        <button type="button" class="link-btn" @click="fillDemo">demo / 123456</button>
      </div>

      <div class="auth-link">
        还没有账号？<router-link to="/register">免费注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '../api/index.js'
import { useAuth } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'

const route = useRoute()
const router = useRouter()
const { setAuth } = useAuth()
const toast = useToast()

const form = reactive({ username: '', password: '' })
const error = ref('')
const loading = ref(false)

function fillDemo() {
  form.username = 'demo'
  form.password = '123456'
}

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const res = await userApi.login(form)
    setAuth(res.data)
    toast.success(`欢迎回来，${res.data.username}`)
    router.push(route.query.redirect || '/')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>
