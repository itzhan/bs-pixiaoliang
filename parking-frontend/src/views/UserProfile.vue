<template>
  <div class="profile-page">
    <!-- ===== Hero Header ===== -->
    <section class="page-hero">
      <div class="hero-bg">
        <div class="hero-shape hero-shape-1" />
        <div class="hero-shape hero-shape-2" />
        <div class="hero-shape hero-shape-3" />
      </div>
      <div class="hero-content">
        <n-avatar :size="88" round :style="{ backgroundColor: '#fff', color: '#2D6A4F', fontSize: '34px', fontWeight: 700, border: '3px solid rgba(255,255,255,0.6)', boxShadow: '0 4px 20px rgba(0,0,0,0.12)' }">
          {{ avatarText }}
        </n-avatar>
        <h1 class="hero-name">{{ userStore.realName || userStore.username }}</h1>
        <n-tag :type="userStore.role === 'ADMIN' ? 'warning' : 'info'" round size="small" :bordered="false">
          {{ roleLabel }}
        </n-tag>
      </div>
    </section>

    <div class="page-container">
      <!-- ===== User Detail Card ===== -->
      <n-card class="detail-card" :bordered="false">
        <template #header>
          <div class="card-title-row">
            <n-icon :size="20" color="#2D6A4F"><PersonOutline /></n-icon>
            <span>基本信息</span>
          </div>
        </template>
        <div class="user-detail-grid">
          <div class="detail-item">
            <div class="detail-icon-wrapper" style="background: rgba(45,106,79,0.08);">
              <n-icon :size="18" color="#2D6A4F"><PersonCircleOutline /></n-icon>
            </div>
            <div class="detail-text">
              <span class="label">用户名</span>
              <span class="value">{{ userStore.username || '—' }}</span>
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-icon-wrapper" style="background: rgba(59,130,246,0.08);">
              <n-icon :size="18" color="#3B82F6"><IdCardOutline /></n-icon>
            </div>
            <div class="detail-text">
              <span class="label">真实姓名</span>
              <span class="value">{{ userStore.realName || '未设置' }}</span>
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-icon-wrapper" style="background: rgba(245,158,11,0.08);">
              <n-icon :size="18" color="#F59E0B"><CallOutline /></n-icon>
            </div>
            <div class="detail-text">
              <span class="label">手机号</span>
              <span class="value">{{ userStore.phone || '未绑定' }}</span>
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-icon-wrapper" style="background: rgba(224,122,95,0.08);">
              <n-icon :size="18" color="#E07A5F"><MailOutline /></n-icon>
            </div>
            <div class="detail-text">
              <span class="label">邮箱</span>
              <span class="value">{{ userStore.email || '未绑定' }}</span>
            </div>
          </div>
        </div>
      </n-card>

      <!-- ===== Quick Navigation ===== -->
      <div class="quick-nav-section">
        <h3 class="sub-section-title">快捷入口</h3>
        <div class="quick-nav">
          <div
            class="nav-item"
            v-for="item in quickNavItems"
            :key="item.path"
            @click="$router.push(item.path)"
          >
            <div class="nav-icon-circle" :style="{ background: item.bg }">
              <n-icon :size="22" :color="item.color" :component="item.icon" />
            </div>
            <span class="nav-label">{{ item.label }}</span>
          </div>
        </div>
      </div>

      <!-- ===== Edit Profile ===== -->
      <n-card class="form-card" :bordered="false">
        <template #header>
          <div class="card-title-row">
            <n-icon :size="20" color="#2D6A4F"><CreateOutline /></n-icon>
            <span>编辑资料</span>
          </div>
        </template>
        <n-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-placement="left" label-width="80">
          <n-form-item label="真实姓名" path="realName">
            <n-input v-model:value="profileForm.realName" placeholder="请输入真实姓名" />
          </n-form-item>
          <n-form-item label="手机号" path="phone">
            <n-input v-model:value="profileForm.phone" placeholder="请输入手机号" />
          </n-form-item>
          <n-form-item label="邮箱" path="email">
            <n-input v-model:value="profileForm.email" placeholder="请输入邮箱" />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" :loading="profileLoading" @click="handleProfileSave">
              保存资料
            </n-button>
          </n-form-item>
        </n-form>
      </n-card>

      <!-- ===== Change Password ===== -->
      <n-card class="form-card" :bordered="false">
        <template #header>
          <div class="card-title-row">
            <n-icon :size="20" color="#E07A5F"><LockClosedOutline /></n-icon>
            <span>修改密码</span>
          </div>
        </template>
        <n-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-placement="left" label-width="80">
          <n-form-item label="原密码" path="oldPassword">
            <n-input v-model:value="pwdForm.oldPassword" type="password" show-password-on="click" placeholder="请输入原密码" />
          </n-form-item>
          <n-form-item label="新密码" path="newPassword">
            <n-input v-model:value="pwdForm.newPassword" type="password" show-password-on="click" placeholder="请输入新密码（6位以上）" />
          </n-form-item>
          <n-form-item label="确认密码" path="confirmPassword">
            <n-input v-model:value="pwdForm.confirmPassword" type="password" show-password-on="click" placeholder="请再次输入新密码" />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" :loading="pwdLoading" @click="handlePwdSave">
              修改密码
            </n-button>
          </n-form-item>
        </n-form>
      </n-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h } from 'vue'
import { useMessage, NIcon, type FormInst } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import { updateProfile } from '@/api/user'
import { updatePassword } from '@/api/user'
import {
  ReceiptOutline,
  CalendarOutline,
  CarOutline,
  CardOutline,
  StarOutline,
  ChatboxEllipsesOutline,
  PersonOutline,
  PersonCircleOutline,
  CallOutline,
  MailOutline,
  CreateOutline,
  LockClosedOutline,
  IdCardOutline,
} from '@vicons/ionicons5'

const message = useMessage()
const userStore = useUserStore()

const avatarText = computed(() => {
  return (userStore.realName || userStore.username || '用')[0]
})

const roleLabel = computed(() => {
  const map: Record<string, string> = { ADMIN: '管理员', USER: '普通用户' }
  return map[userStore.role] ?? (userStore.role || '普通用户')
})

const quickNavItems = [
  { label: '我的订单', path: '/orders', icon: ReceiptOutline, color: '#2D6A4F', bg: 'rgba(45,106,79,0.08)' },
  { label: '我的预约', path: '/reservations', icon: CalendarOutline, color: '#3B82F6', bg: 'rgba(59,130,246,0.08)' },
  { label: '我的车辆', path: '/vehicles', icon: CarOutline, color: '#F59E0B', bg: 'rgba(245,158,11,0.08)' },
  { label: '我的支付', path: '/payments', icon: CardOutline, color: '#E07A5F', bg: 'rgba(224,122,95,0.08)' },
  { label: '我的评价', path: '/reviews', icon: StarOutline, color: '#8B5CF6', bg: 'rgba(139,92,246,0.08)' },
  { label: '我的消息', path: '/messages', icon: ChatboxEllipsesOutline, color: '#10B981', bg: 'rgba(16,185,129,0.08)' },
]

// --- Edit Profile ---
const profileFormRef = ref<FormInst | null>(null)
const profileLoading = ref(false)

const profileForm = reactive({
  realName: '',
  phone: '',
  email: '',
})

const profileRules = {
  phone: {
    pattern: /^1[3-9]\d{9}$/,
    message: '请输入有效手机号',
    trigger: 'blur',
  },
  email: {
    type: 'email' as const,
    message: '请输入有效邮箱',
    trigger: 'blur',
  },
}

function initProfileForm() {
  profileForm.realName = userStore.realName || ''
  profileForm.phone = userStore.phone || ''
  profileForm.email = userStore.email || ''
}

async function handleProfileSave() {
  try {
    await profileFormRef.value?.validate()
  } catch {
    return
  }

  profileLoading.value = true
  try {
    await updateProfile({
      realName: profileForm.realName || undefined,
      phone: profileForm.phone || undefined,
      email: profileForm.email || undefined,
    })
    await userStore.getUserInfo()
    message.success('资料更新成功')
  } catch (e: any) {
    message.error(e.message || '更新失败')
  } finally {
    profileLoading.value = false
  }
}

// --- Change Password ---
const pwdFormRef = ref<FormInst | null>(null)
const pwdLoading = ref(false)

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const pwdRules = {
  oldPassword: { required: true, message: '请输入原密码', trigger: 'blur' },
  newPassword: {
    required: true,
    message: '请输入新密码（至少6位）',
    trigger: 'blur',
    min: 6,
  },
  confirmPassword: {
    required: true,
    trigger: 'blur',
    validator(_rule: any, value: string) {
      if (!value) return new Error('请确认新密码')
      if (value !== pwdForm.newPassword) return new Error('两次输入的密码不一致')
      return true
    },
  },
}

async function handlePwdSave() {
  try {
    await pwdFormRef.value?.validate()
  } catch {
    return
  }

  pwdLoading.value = true
  try {
    await updatePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    message.success('密码修改成功，请重新登录')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    userStore.logout()
  } catch (e: any) {
    message.error(e.message || '密码修改失败')
  } finally {
    pwdLoading.value = false
  }
}

onMounted(async () => {
  if (!userStore.username) {
    try {
      await userStore.getUserInfo()
    } catch {
      // handled by interceptor
    }
  }
  initProfileForm()
})
</script>

<style scoped>
/* ===== Hero Header ===== */
.page-hero {
  position: relative;
  z-index: 0;
  padding: 48px 24px 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, #2D6A4F 0%, #40916C 50%, #52B788 100%);
}

.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.hero-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  background: #fff;
}

.hero-shape-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -60px;
}

.hero-shape-2 {
  width: 200px;
  height: 200px;
  bottom: -80px;
  left: -40px;
}

.hero-shape-3 {
  width: 120px;
  height: 120px;
  top: 40%;
  left: 20%;
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.hero-name {
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  margin: 0;
  letter-spacing: 1px;
}

/* ===== Page Container ===== */
.profile-page .page-container {
  max-width: 900px;
  margin: -32px auto 0;
  padding: 0 24px 48px;
  position: relative;
  z-index: 2;
  pointer-events: auto;
}

/* ===== Detail Card ===== */
.detail-card {
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  margin-bottom: 24px;
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.user-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.detail-item:hover {
  background: rgba(45, 106, 79, 0.04);
}

.detail-icon-wrapper {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.detail-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-text .label {
  font-size: 12px;
  color: var(--text-tertiary);
}

.detail-text .value {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 500;
}

/* ===== Quick Navigation ===== */
.quick-nav-section {
  margin-bottom: 24px;
}

.sub-section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 3px solid var(--primary);
}

.quick-nav {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 12px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: box-shadow var(--transition-normal), transform var(--transition-normal);
}

.nav-item:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-3px);
}

.nav-icon-circle {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* ===== Form Card ===== */
.form-card {
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  margin-bottom: 20px;
}

.form-card :deep(.n-form) {
  max-width: 480px;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .user-detail-grid {
    grid-template-columns: 1fr;
  }

  .quick-nav {
    grid-template-columns: repeat(3, 1fr);
  }

  .page-hero {
    padding: 36px 16px 44px;
  }

  .hero-name {
    font-size: 22px;
  }

  .profile-page .page-container {
    padding: 0 16px 40px;
  }
}
</style>
