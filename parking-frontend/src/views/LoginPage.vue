<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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

const route = useRoute()
const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const formData = reactive({
  username: '',
  password: '',
})

const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' },
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await userStore.login(formData.username, formData.password)
    message.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    /* handled by interceptor */
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <n-card class="login-card" :bordered="false">
        <div class="login-header">
          <div class="login-logo">
            <n-icon size="40" color="#2D6A4F">
              <CarSportOutline />
            </n-icon>
          </div>
          <h1 class="login-title">智能停车场</h1>
          <p class="login-subtitle">欢迎回来，请登录您的账号</p>
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
              placeholder="请输入用户名"
              @keyup.enter="handleLogin"
            />
          </n-form-item>
          <n-form-item path="password">
            <n-input
              v-model:value="formData.password"
              type="password"
              show-password-on="click"
              placeholder="请输入密码"
              @keyup.enter="handleLogin"
            />
          </n-form-item>
          <n-button
            type="primary"
            block
            :loading="loading"
            size="large"
            style="margin-top: 8px; border-radius: 8px"
            @click="handleLogin"
          >
            登 录
          </n-button>
        </n-form>

        <div class="login-footer">
          没有账号？
          <router-link to="/register" class="link">立即注册</router-link>
        </div>
      </n-card>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f7f4 0%, #e8f0f6 50%, #f5f0e8 100%);
  padding: 24px;
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  padding: 16px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  margin-bottom: 12px;
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #94a3b8;
}

.login-footer {
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
