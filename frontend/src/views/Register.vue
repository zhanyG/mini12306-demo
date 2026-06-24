<template>
  <div class="auth-page">
    <div class="auth-card auth-card-wide">
      <div class="auth-logo">🚄</div>
      <h1 class="auth-title">注册新账号</h1>
      <p class="auth-subtitle">测试环境简化注册，身份证非必填</p>

      <div class="test-hint">
        <strong>测试提示：</strong>仅需用户名和密码（≥6位）即可完成注册，手机号和姓名可选填。
      </div>

      <div v-if="error" class="message message-error">{{ error }}</div>

      <form @submit.prevent="handleRegister">
        <div class="form-row">
          <div class="form-group">
            <label>用户名 <span class="required">*</span></label>
            <input v-model="form.username" class="form-input" placeholder="设置登录用户名" required />
          </div>
          <div class="form-group">
            <label>密码 <span class="required">*</span></label>
            <input v-model="form.password" class="form-input" type="password" placeholder="不少于6位" required />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>真实姓名</label>
            <input v-model="form.realName" class="form-input" placeholder="可选，如：张三" />
          </div>
          <div class="form-group">
            <label>手机号</label>
            <input v-model="form.phone" class="form-input" placeholder="可选，如：13800138000" />
          </div>
        </div>
        <button type="submit" class="btn btn-success btn-block btn-lg" :disabled="loading">
          {{ loading ? '注册中...' : '立即注册' }}
        </button>
      </form>

      <div class="auth-link">
        已有账号？<router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api/index.js'
import { useToast } from '../composables/useToast.js'

const router = useRouter()
const toast = useToast()

const form = reactive({ username: '', password: '', phone: '', realName: '' })
const error = ref('')
const loading = ref(false)

async function handleRegister() {
  error.value = ''
  if (form.password.length < 6) {
    error.value = '密码长度不能少于6位'
    return
  }
  loading.value = true
  try {
    await userApi.register(form)
    toast.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>
