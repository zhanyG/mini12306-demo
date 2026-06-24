<template>
  <div class="page">
    <van-nav-bar title="个人中心" />

    <!-- 用户信息 -->
    <div class="user-card">
      <van-cell-group inset>
        <van-cell>
          <template #title>
            <div class="user-info">
              <van-icon name="contact" size="36" color="#ee0a24" />
              <div class="user-detail">
                <div class="user-name">{{ user?.realName || user?.username || '用户' }}</div>
                <div class="user-sub">@{{ user?.username }}</div>
              </div>
            </div>
          </template>
        </van-cell>
        <van-cell title="手机号" :value="user?.phone || '未填写'" />
        <van-cell title="真实姓名" :value="user?.realName || '未填写'" />
        <van-cell title="注册时间" :value="formatDateTime(user?.createTime)" />
      </van-cell-group>
    </div>

    <!-- 快捷入口 -->
    <van-cell-group inset style="margin: 12px;">
      <van-cell title="我的订单" icon="orders-o" is-link to="/orders" />
      <van-cell title="我的账单" icon="gold-coin-o" is-link to="/bills" />
      <van-cell title="常用乘客" icon="contact" is-link to="/passengers" />
    </van-cell-group>

    <!-- 设置 -->
    <van-cell-group inset style="margin: 12px;">
      <van-cell title="实名认证" icon="records" is-link @click="showIdCard = true" :value="user?.idCard ? '已认证' : '未认证'" />
      <van-cell title="编辑资料" icon="edit" is-link @click="showEdit = true" />
      <van-cell title="修改密码" icon="lock" is-link @click="showPassword = true" />
      <van-cell title="退出登录" icon="logout" class="logout-cell" @click="handleLogout" />
    </van-cell-group>

    <!-- 实名认证弹窗 -->
    <van-dialog v-model:show="showIdCard" title="实名认证" show-cancel-button @confirm="handleVerifyIdCard" confirm-button-text="提交认证">
      <van-form @submit.prevent>
        <van-cell-group inset>
          <van-field v-model="idCardForm.realName" label="真实姓名" placeholder="请输入真实姓名" clearable />
          <van-field v-model="idCardForm.idCard" label="身份证号" placeholder="请输入18位身份证号" maxlength="18" clearable />
        </van-cell-group>
        <div v-if="idCardResult" :class="['idcard-result', idCardResult.success ? 'success' : 'fail']" style="margin:0 16px 12px;text-align:center;">
          {{ idCardResult.message }}
        </div>
      </van-form>
    </van-dialog>

    <!-- 编辑资料弹窗 -->
    <van-dialog v-model:show="showEdit" title="编辑资料" show-cancel-button @confirm="saveProfile" confirm-button-text="保存">
      <van-form @submit.prevent>
        <van-cell-group inset>
          <van-field v-model="editForm.phone" label="手机号" placeholder="请输入手机号" clearable />
          <van-field v-model="editForm.realName" label="真实姓名" placeholder="请输入真实姓名" clearable />
          <van-field v-model="editForm.idCard" label="身份证" placeholder="请输入身份证号" clearable />
        </van-cell-group>
      </van-form>
    </van-dialog>

    <!-- 修改密码弹窗 -->
    <van-dialog v-model:show="showPassword" title="修改密码" show-cancel-button @confirm="changePassword" confirm-button-text="确认">
      <van-form @submit.prevent>
        <van-cell-group inset>
          <van-field v-model="passwordForm.oldPassword" type="password" label="旧密码" placeholder="请输入旧密码" />
          <van-field v-model="passwordForm.newPassword" type="password" label="新密码" placeholder="至少6位" />
          <van-field v-model="passwordForm.confirmPassword" type="password" label="确认密码" placeholder="再次输入新密码" />
        </van-cell-group>
      </van-form>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api/index.js'
import { formatDateTime } from '../utils/format.js'
import { showToast } from 'vant'

const router = useRouter()
const userId = localStorage.getItem('userId')

const user = ref(null)
const showEdit = ref(false)
const showPassword = ref(false)
const showIdCard = ref(false)
const editForm = ref({ phone: '', realName: '', idCard: '' })
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const idCardForm = ref({ realName: '', idCard: '' })
const idCardResult = ref(null)

onMounted(() => loadUser())

watch(showEdit, (v) => {
  if (v && user.value) {
    editForm.value = {
      phone: user.value.phone || '',
      realName: user.value.realName || '',
      idCard: user.value.idCard || ''
    }
  }
})

watch(showIdCard, (v) => {
  if (v && user.value) {
    idCardForm.value = { realName: user.value.realName || '', idCard: user.value.idCard || '' }
    idCardResult.value = null
  }
})

async function loadUser() {
  try {
    const res = await userApi.getById(userId)
    user.value = res.data
  } catch (e) {
    showToast('加载用户信息失败')
  }
}

async function saveProfile() {
  try {
    await userApi.updateProfile(userId, editForm.value)
    showToast('保存成功')
    showEdit.value = false
    loadUser()
  } catch (e) {
    showToast(e.message)
  }
}

async function changePassword() {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value
  if (!oldPassword || !newPassword) { showToast('请填写完整'); return }
  if (newPassword.length < 6) { showToast('密码至少6位'); return }
  if (newPassword !== confirmPassword) { showToast('两次密码不一致'); return }

  try {
    await userApi.changePassword(userId, { oldPassword, newPassword })
    showToast('密码修改成功')
    showPassword.value = false
  } catch (e) {
    showToast(e.message)
  }
}

async function handleVerifyIdCard() {
  const { realName, idCard } = idCardForm.value
  if (!realName.trim() || !idCard.trim()) { showToast('请填写姓名和身份证号'); return }
  idCardResult.value = null
  try {
    const res = await userApi.verifyIdCard(userId, { realName: realName.trim(), idCard: idCard.trim() })
    idCardResult.value = { success: true, message: res.data.message + '（' + res.data.birthDate + '，' + res.data.gender + '）' }
    showToast(res.data.message)
    showIdCard.value = false
    loadUser()
  } catch (e) {
    idCardResult.value = { success: false, message: e.message }
  }
}

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  showToast('已退出')
  router.push('/login')
}
</script>

<style scoped>
.user-card {
  margin: 12px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-detail {
  display: flex;
  flex-direction: column;
}
.user-name {
  font-size: 16px;
  font-weight: 600;
}
.user-sub {
  font-size: 12px;
  color: #969799;
}
.logout-cell {
  color: #ee0a24;
}
.idcard-result {
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
}
.idcard-result.success {
  background: #f0fff0;
  color: #07c160;
}
.idcard-result.fail {
  background: #fff2f0;
  color: #ee0a24;
}
</style>
