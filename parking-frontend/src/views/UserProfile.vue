<template>
  <div class="page-container">
    <h2 class="section-title">个人中心</h2>

    <!-- User Info Card -->
    <n-card class="profile-card" :bordered="false">
      <div class="user-avatar-section">
        <n-avatar :size="72" round :style="{ backgroundColor: 'var(--primary)', fontSize: '28px' }">
          {{ avatarText }}
        </n-avatar>
        <div class="user-basic">
          <h3 class="user-name">{{ userStore.username }}</h3>
          <n-tag size="small" :type="userStore.role === 'ADMIN' ? 'warning' : 'info'" round>
            {{ roleLabel }}
          </n-tag>
        </div>
      </div>

      <div class="user-detail-grid">
        <div class="detail-item">
          <span class="label">用户名</span>
          <span class="value">{{ userStore.username || '—' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">真实姓名</span>
          <span class="value">{{ userStore.realName || '未设置' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">手机号</span>
          <span class="value">{{ userStore.phone || '未绑定' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">邮箱</span>
          <span class="value">{{ userStore.email || '未绑定' }}</span>
        </div>
      </div>
    </n-card>

    <!-- Edit Profile -->
    <n-card title="编辑资料" class="form-card" :bordered="false" style="margin-top: 20px">
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

    <!-- Change Password -->
    <n-card title="修改密码" class="form-card" :bordered="false" style="margin-top: 20px">
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
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useMessage, type FormInst } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import { updateProfile } from '@/api/user'
import { updatePassword } from '@/api/user'

const message = useMessage()
const userStore = useUserStore()

const avatarText = computed(() => {
  return (userStore.realName || userStore.username || '用')[0]
})

const roleLabel = computed(() => {
  const map: Record<string, string> = { ADMIN: '管理员', USER: '普通用户' }
  return map[userStore.role] ?? (userStore.role || '普通用户')
})

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
    // Refresh store
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
    // Optionally force re-login
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
.profile-card {
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.user-avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.user-basic {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.user-detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item .label {
  font-size: 12px;
  color: var(--text-tertiary);
}

.detail-item .value {
  font-size: 14px;
  color: var(--text-primary);
}

.form-card {
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.form-card :deep(.n-form) {
  max-width: 480px;
}

@media (max-width: 768px) {
  .user-detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
