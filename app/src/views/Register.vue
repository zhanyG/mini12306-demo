<template>
  <div class="page">
    <van-nav-bar title="注册" left-arrow @click-left="$router.back()" />

    <div class="register-form">
      <van-form @submit="handleRegister">
        <van-cell-group inset>
          <van-field
            v-model="form.username"
            label="用户名"
            placeholder="请输入用户名（必填）"
            :rules="[{ required: true, message: '请输入用户名' }]"
            clearable
          />
          <van-field
            v-model="form.password"
            type="password"
            label="密码"
            placeholder="至少6位（必填）"
            :rules="[
              { required: true, message: '请输入密码' },
              { validator: v => v.length >= 6, message: '密码至少6位' }
            ]"
            clearable
          />
          <van-field
            v-model="form.realName"
            label="真实姓名"
            placeholder="请输入真实姓名（选填）"
            clearable
          />
          <van-field
            v-model="form.phone"
            label="手机号"
            type="tel"
            placeholder="请输入手机号（选填）"
            clearable
          />
        </van-cell-group>

        <div style="margin: 20px 16px;">
          <van-button block type="primary" native-type="submit" :loading="loading">
            注册
          </van-button>
        </div>
      </van-form>

      <div style="text-align: center;">
        <van-button plain hairline size="small" @click="$router.push('/login')">
          已有账号？去登录
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api/index.js'
import { showToast } from 'vant'

const router = useRouter()
const loading = ref(false)

const form = ref({ username: '', password: '', realName: '', phone: '' })

async function handleRegister() {
  loading.value = true
  try {
    await userApi.register(form.value)
    showToast('注册成功')
    setTimeout(() => router.push('/login'), 1000)
  } catch (e) {
    showToast(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-form {
  padding: 20px 0;
}
</style>
