<template>
  <div class="page">
    <van-nav-bar title="登录" />

    <div class="login-form">
      <div class="logo-area">
        <van-icon name="logistics" size="48" color="#ee0a24" />
        <h2>Mini12306</h2>
        <p class="subtitle">便捷出行，从这里开始</p>
      </div>

      <van-form @submit="handleLogin">
        <van-cell-group inset>
          <van-field
            v-model="username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请输入用户名' }]"
            clearable
          />
          <van-field
            v-model="password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
            clearable
          />
        </van-cell-group>

        <div style="margin: 20px 16px;">
          <van-button block type="primary" native-type="submit" :loading="loading">
            登录
          </van-button>
        </div>
      </van-form>

      <div class="login-links">
        <van-button plain hairline size="small" @click="$router.push('/register')">
          没有账号？去注册
        </van-button>
        <van-button plain hairline size="small" @click="quickFill">
          快速填充 demo/123456
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { userApi } from '../api/index.js'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()

const username = ref('')
const password = ref('')
const loading = ref(false)

function quickFill() {
  username.value = 'demo'
  password.value = '123456'
}

async function handleLogin() {
  loading.value = true
  try {
    const res = await userApi.login({ username: username.value, password: password.value })
    const { token, userId, username: uname } = res.data
    localStorage.setItem('token', token)
    localStorage.setItem('userId', userId)
    localStorage.setItem('username', uname)
    showToast('登录成功')
    setTimeout(() => router.push(route.query.redirect || '/'), 500)
  } catch (e) {
    showToast(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-form {
  padding: 40px 16px 0;
}
.logo-area {
  text-align: center;
  padding: 30px 0;
}
.logo-area h2 {
  margin: 12px 0 4px;
  font-size: 24px;
}
.subtitle {
  color: #969799;
  font-size: 14px;
  margin: 0;
}
.login-links {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}
</style>
