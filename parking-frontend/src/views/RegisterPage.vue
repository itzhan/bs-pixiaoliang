<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard,
  NForm,
  NFormItem,
  NInput,
  NButton,
  NIcon,
  useMessage,
} from 'naive-ui'
import type { FormInst } from 'naive-ui'
import { CarSportOutline } from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const formData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度须在3-50个字符之间', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度须在6-100个字符之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator(_rule: any, value: string) {
        if (value !== formData.password) {
          return new Error('两次输入密码不一致')
        }
        return true
      },
      trigger: 'blur',
    },
  ],
  phone: {
    pattern: /^1[3-9]\d{9}$/,
    message: '手机号格式不正确',
    trigger: 'blur',
  },
  email: {
    type: 'email' as const,
    message: '邮箱格式不正确',
    trigger: 'blur',
  },
}

async function handleRegister() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await userStore.register({
      username: formData.username,
      password: formData.password,
      realName: formData.realName || undefined,
      phone: formData.phone || undefined,
      email: formData.email || undefined,
    })
    message.success('注册成功')
    router.push('/')
  } catch {
    /* handled by interceptor */
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-container">
      <n-card class="register-card" :bordered="false">
        <div class="register-header">
          <div class="register-logo">
            <n-icon size="40" color="#2D6A4F">
              <CarSportOutline />
            </n-icon>
          </div>
          <h1 class="register-title">智能停车场</h1>
          <p class="register-subtitle">创建您的账号，开始智能停车体验</p>
        </div>

        <n-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-placement="left"
          :show-label="false"
          size="large"
        >
          <n-form-item path="username">
            <n-input
              v-model:value="formData.username"
              placeholder="请输入用户名 *"
            />
          </n-form-item>
          <n-form-item path="password">
            <n-input
              v-model:value="formData.password"
              type="password"
              show-password-on="click"
              placeholder="请输入密码 *"
            />
          </n-form-item>
          <n-form-item path="confirmPassword">
            <n-input
              v-model:value="formData.confirmPassword"
              type="password"
              show-password-on="click"
              placeholder="请确认密码 *"
            />
          </n-form-item>
          <n-form-item path="realName">
            <n-input
              v-model:value="formData.realName"
              placeholder="真实姓名（选填）"
            />
          </n-form-item>
          <n-form-item path="phone">
            <n-input
              v-model:value="formData.phone"
              placeholder="手机号（选填）"
            />
          </n-form-item>
          <n-form-item path="email">
            <n-input
              v-model:value="formData.email"
              placeholder="邮箱（选填）"
            />
          </n-form-item>
          <n-button
            type="primary"
            block
            :loading="loading"
            size="large"
            style="margin-top: 8px; border-radius: 8px"
            @click="handleRegister"
          >
            注 册
          </n-button>
        </n-form>

        <div class="register-footer">
          已有账号？
          <router-link to="/login" class="link">立即登录</router-link>
        </div>
      </n-card>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f7f4 0%, #e8f0f6 50%, #f5f0e8 100%);
  padding: 24px;
}

.register-container {
  width: 100%;
  max-width: 420px;
}

.register-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  padding: 16px;
}

.register-header {
  text-align: center;
  margin-bottom: 24px;
}

.register-logo {
  margin-bottom: 12px;
}

.register-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
}

.register-subtitle {
  font-size: 14px;
  color: #94a3b8;
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #64748b;
}

.link {
  color: #2d6a4f;
  font-weight: 500;
  text-decoration: none;
}

.link:hover {
  color: #1d5a3f;
}
</style>
