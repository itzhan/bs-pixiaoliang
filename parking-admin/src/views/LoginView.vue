<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { UserOutlined, LockOutlined, CarOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)

const formState = reactive({
  username: '',
  password: '',
})

const rules: Record<string, Rule[]> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度 2-20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, max: 30, message: '密码长度 4-30 个字符', trigger: 'blur' },
  ],
}

const formRef = ref()

async function handleLogin() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await userStore.login(formState.username, formState.password)
    message.success('登录成功')
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch (err: any) {
    message.error(err?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <!-- Decorative shapes -->
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>
    <div class="bg-shape shape-3"></div>

    <div class="login-card">
      <div class="login-header">
        <div class="logo-wrapper">
          <CarOutlined class="logo-icon" />
        </div>
        <h1 class="login-title">智能停车场管理系统</h1>
        <p class="login-subtitle">Smart Parking Management System</p>
      </div>

      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        layout="vertical"
        @finish="handleLogin"
      >
        <a-form-item name="username">
          <a-input
            v-model:value="formState.username"
            size="large"
            placeholder="请输入用户名"
            allow-clear
            @press-enter="handleLogin"
          >
            <template #prefix>
              <UserOutlined style="color: rgba(0, 0, 0, 0.25)" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="password">
          <a-input-password
            v-model:value="formState.password"
            size="large"
            placeholder="请输入密码"
            @press-enter="handleLogin"
          >
            <template #prefix>
              <LockOutlined style="color: rgba(0, 0, 0, 0.25)" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item style="margin-bottom: 0">
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            :loading="loading"
            block
            class="login-btn"
          >
            登 录
          </a-button>
        </a-form-item>
      </a-form>

      <div class="login-footer">
        <span>© 2026 智能停车场管理系统</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e0f0ff 0%, #d4e4ff 30%, #e8d5f5 70%, #f0e0ff 100%);
  position: relative;
  overflow: hidden;
}

/* Decorative floating shapes */
.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
  pointer-events: none;
}
.shape-1 {
  width: 400px;
  height: 400px;
  background: #6c8ef5;
  top: -100px;
  left: -100px;
}
.shape-2 {
  width: 300px;
  height: 300px;
  background: #a78bfa;
  bottom: -80px;
  right: -60px;
}
.shape-3 {
  width: 200px;
  height: 200px;
  background: #67b8f5;
  top: 50%;
  right: 15%;
}

.login-card {
  width: 420px;
  padding: 48px 40px 32px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.06),
    0 12px 48px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.8);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #4f8ef7 0%, #7c5cf5 100%);
  margin-bottom: 16px;
}

.logo-icon {
  font-size: 32px;
  color: #fff;
}

.login-title {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.88);
  letter-spacing: 1px;
}

.login-subtitle {
  margin: 0;
  color: rgba(0, 0, 0, 0.4);
  font-size: 13px;
  letter-spacing: 0.5px;
}

.login-btn {
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #4f8ef7 0%, #7c5cf5 100%);
  border: none;
  letter-spacing: 4px;
}
.login-btn:hover {
  background: linear-gradient(135deg, #3d7ef0 0%, #6a4ae8 100%);
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  color: rgba(0, 0, 0, 0.3);
  font-size: 12px;
}

/* Override Ant input radius */
:deep(.ant-input-affix-wrapper) {
  border-radius: 8px;
}
:deep(.ant-input-password) {
  border-radius: 8px;
}
</style>
