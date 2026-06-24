<template>
  <div class="page page-narrow">

    <!-- 用户信息卡片 -->
    <div class="card">
      <div class="card-title">个人中心</div>

      <div v-if="loading" class="loading"><span class="spinner" /> 加载中...</div>

      <template v-else-if="user">
        <div class="profile-header">
          <div class="profile-avatar">{{ avatarChar }}</div>
          <div>
            <div class="profile-name">{{ user.realName || user.username }}</div>
            <div class="profile-username">@{{ user.username }}</div>
          </div>
        </div>

        <div class="profile-info-list">
          <div class="profile-info-item">
            <span class="profile-label">用户 ID</span>
            <span>{{ user.id }}</span>
          </div>
          <div class="profile-info-item">
            <span class="profile-label">手机号</span>
            <span>{{ user.phone || '未填写' }}</span>
          </div>
          <div class="profile-info-item">
            <span class="profile-label">真实姓名</span>
            <span>{{ user.realName || '未填写' }}</span>
          </div>
          <div class="profile-info-item">
            <span class="profile-label">身份证号</span>
            <span>{{ user.idCard ? hideIdCard(user.idCard) : '未绑定' }}</span>
          </div>
          <div class="profile-info-item">
            <span class="profile-label">注册时间</span>
            <span>{{ formatDateTime(user.createTime) }}</span>
          </div>
        </div>

        <div class="profile-actions">
          <router-link to="/orders" class="btn btn-outline">我的订单</router-link>
          <router-link to="/bills" class="btn btn-outline">我的账单</router-link>
          <router-link to="/passengers" class="btn btn-outline">常用乘客</router-link>
          <router-link to="/" class="btn btn-primary">去购票</router-link>
        </div>
      </template>

      <div v-else class="empty-card">
        <div class="empty-title">无法加载用户信息</div>
        <button class="btn btn-primary btn-sm" @click="loadUser">重试</button>
      </div>
    </div>

    <!-- 设置分区 -->
    <div class="card">
      <div class="card-title">账号设置</div>
      <div class="settings-list">
        <div class="settings-item">
          <div class="settings-item-left">
            <span class="settings-item-icon">✏️</span>
            <div>
              <div class="settings-item-title">编辑资料</div>
              <div class="settings-item-desc">修改手机号、真实姓名、身份证信息</div>
            </div>
          </div>
          <button class="btn btn-outline btn-sm" @click="showEditModal = true">编辑</button>
        </div>
        <div class="settings-item">
          <div class="settings-item-left">
            <span class="settings-item-icon">🪪</span>
            <div>
              <div class="settings-item-title">实名认证</div>
              <div class="settings-item-desc">{{ user?.idCard ? '已认证' : '未认证，点击完成实名' }}</div>
            </div>
          </div>
          <button class="btn btn-outline btn-sm" @click="showIdCardModal = true">{{ user?.idCard ? '查看' : '认证' }}</button>
        </div>
        <div class="settings-item">
          <div class="settings-item-left">
            <span class="settings-item-icon">🔒</span>
            <div>
              <div class="settings-item-title">修改密码</div>
              <div class="settings-item-desc">定期更换密码可提高账号安全性</div>
            </div>
          </div>
          <button class="btn btn-outline btn-sm" @click="showPasswordModal = true">修改</button>
        </div>
        <div class="settings-item">
          <div class="settings-item-left">
            <span class="settings-item-icon">🚪</span>
            <div>
              <div class="settings-item-title">退出登录</div>
              <div class="settings-item-desc">退出当前账号，返回登录页面</div>
            </div>
          </div>
          <button class="btn btn-danger btn-sm" @click="handleLogout">退出</button>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="card">
      <div class="card-title">快捷入口</div>
      <div class="shortcut-grid">
        <router-link to="/" class="shortcut-item">
          <span class="shortcut-icon">🔍</span>
          <span>车次查询</span>
        </router-link>
        <router-link to="/orders" class="shortcut-item">
          <span class="shortcut-icon">📋</span>
          <span>我的订单</span>
        </router-link>
        <router-link to="/bills" class="shortcut-item">
          <span class="shortcut-icon">💰</span>
          <span>我的账单</span>
        </router-link>
        <router-link to="/passengers" class="shortcut-item">
          <span class="shortcut-icon">👥</span>
          <span>常用乘客</span>
        </router-link>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal-box" style="max-width: 420px;">
        <div class="modal-header">
          <span class="modal-title">编辑资料</span>
          <button class="modal-close" @click="showEditModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">手机号</label>
            <input v-model="editForm.phone" class="form-input" placeholder="请输入手机号" maxlength="20" />
          </div>
          <div class="form-group">
            <label class="form-label">真实姓名</label>
            <input v-model="editForm.realName" class="form-input" placeholder="请输入真实姓名" maxlength="50" />
          </div>
          <div class="form-group">
            <label class="form-label">身份证号</label>
            <input v-model="editForm.idCard" class="form-input" placeholder="请输入身份证号" maxlength="18" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showEditModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="saveProfile">{{ saving ? '保存中...' : '保存' }}</button>
        </div>
      </div>
    </div>

    <!-- 实名认证弹窗 -->
    <div v-if="showIdCardModal" class="modal-overlay" @click.self="showIdCardModal = false">
      <div class="modal-box" style="max-width: 420px;">
        <div class="modal-header">
          <span class="modal-title">实名认证</span>
          <button class="modal-close" @click="showIdCardModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <p class="modal-hint">请输入真实姓名与身份证号码，系统将校验号码合法性。不涉及第三方接口。</p>
          <div class="form-group">
            <label class="form-label">真实姓名 <span class="required">*</span></label>
            <input v-model="idCardForm.realName" class="form-input" placeholder="请输入真实姓名" maxlength="50" />
          </div>
          <div class="form-group">
            <label class="form-label">身份证号 <span class="required">*</span></label>
            <input v-model="idCardForm.idCard" class="form-input" placeholder="请输入18位身份证号" maxlength="18" />
          </div>
          <div v-if="idCardResult" :class="['idcard-result', idCardResult.success ? 'success' : 'fail']">
            {{ idCardResult.message }}
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showIdCardModal = false">取消</button>
          <button class="btn btn-primary" :disabled="idCarding" @click="handleVerifyIdCard">
            {{ idCarding ? '验证中...' : '提交认证' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <div v-if="showPasswordModal" class="modal-overlay" @click.self="showPasswordModal = false">
      <div class="modal-box" style="max-width: 420px;">
        <div class="modal-header">
          <span class="modal-title">修改密码</span>
          <button class="modal-close" @click="showPasswordModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">旧密码</label>
            <input v-model="passwordForm.oldPassword" type="password" class="form-input" placeholder="请输入旧密码" />
          </div>
          <div class="form-group">
            <label class="form-label">新密码</label>
            <input v-model="passwordForm.newPassword" type="password" class="form-input" placeholder="请输入新密码（至少6位）" />
          </div>
          <div class="form-group">
            <label class="form-label">确认新密码</label>
            <input v-model="passwordForm.confirmPassword" type="password" class="form-input" placeholder="请再次输入新密码" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showPasswordModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="changePassword">{{ saving ? '提交中...' : '确认修改' }}</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { userApi } from '../api/index.js'
import { formatDateTime } from '../utils/format.js'
import { useAuth } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'
import { useRouter } from 'vue-router'

const { userId, username, logout } = useAuth()
const toast = useToast()
const router = useRouter()

const user = ref(null)
const loading = ref(false)
const saving = ref(false)

const showEditModal = ref(false)
const showPasswordModal = ref(false)
const showIdCardModal = ref(false)

const editForm = ref({ phone: '', realName: '', idCard: '' })
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const idCardForm = ref({ realName: '', idCard: '' })
const idCarding = ref(false)
const idCardResult = ref(null)

const avatarChar = computed(() => {
  const name = user.value?.realName || user.value?.username || username.value || '?'
  return name.charAt(0).toUpperCase()
})

onMounted(() => loadUser())

/** 监听弹窗打开时，回填当前值 */
watch(showEditModal, (v) => {
  if (v && user.value) {
    editForm.value = {
      phone: user.value.phone || '',
      realName: user.value.realName || '',
      idCard: user.value.idCard || ''
    }
  }
})

watch(showPasswordModal, (v) => {
  if (v) {
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  }
})

watch(showIdCardModal, (v) => {
  if (v && user.value) {
    idCardForm.value = {
      realName: user.value.realName || '',
      idCard: user.value.idCard || ''
    }
    idCardResult.value = null
  }
})

async function loadUser() {
  loading.value = true
  try {
    const res = await userApi.getById(userId.value)
    user.value = res.data
  } catch (e) {
    toast.error('加载用户信息失败：' + e.message)
    user.value = null
  } finally {
    loading.value = false
  }
}

/** 隐藏身份证中间8位 */
function hideIdCard(card) {
  if (!card || card.length < 10) return card || ''
  return card.substring(0, 3) + '********' + card.substring(card.length - 4)
}

/** 保存编辑资料 */
async function saveProfile() {
  saving.value = true
  try {
    await userApi.updateProfile(userId.value, editForm.value)
    toast.success('资料修改成功')
    showEditModal.value = false
    await loadUser()
  } catch (e) {
    toast.error(e.message)
  } finally {
    saving.value = false
  }
}

/** 修改密码 */
async function changePassword() {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value
  if (!oldPassword || !newPassword) {
    toast.error('请填写完整')
    return
  }
  if (newPassword.length < 6) {
    toast.error('新密码长度不能少于6位')
    return
  }
  if (newPassword !== confirmPassword) {
    toast.error('两次输入的新密码不一致')
    return
  }
  saving.value = true
  try {
    await userApi.changePassword(userId.value, { oldPassword, newPassword })
    toast.success('密码修改成功')
    showPasswordModal.value = false
  } catch (e) {
    toast.error(e.message)
  } finally {
    saving.value = false
  }
}

/** 实名认证 */
async function handleVerifyIdCard() {
  const { realName, idCard } = idCardForm.value
  if (!realName.trim() || !idCard.trim()) {
    toast.error('请填写姓名和身份证号')
    return
  }
  idCarding.value = true
  idCardResult.value = null
  try {
    const res = await userApi.verifyIdCard(userId.value, { realName: realName.trim(), idCard: idCard.trim() })
    idCardResult.value = { success: true, message: res.data.message + '（出生日期：' + res.data.birthDate + '，性别：' + res.data.gender + '）' }
    toast.success(res.data.message)
    await loadUser()
  } catch (e) {
    idCardResult.value = { success: false, message: e.message }
    toast.error(e.message)
  } finally {
    idCarding.value = false
  }
}

/** 退出登录 */
function handleLogout() {
  logout()
  router.push('/login')
}
</script>

<style scoped>
.idcard-result {
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 13px;
  margin-top: 8px;
}
.idcard-result.success {
  background: #f0fff0;
  color: #07c160;
  border: 1px solid #b7eb8f;
}
.idcard-result.fail {
  background: #fff2f0;
  color: #ee0a24;
  border: 1px solid #ffccc7;
}
</style>
