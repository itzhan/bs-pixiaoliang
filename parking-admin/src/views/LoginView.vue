<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { UserOutlined, LockOutlined, CarOutlined, SafetyCertificateOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const currentTime = ref('')

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

function updateTime() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const h = String(now.getHours()).padStart(2, '0')
  const min = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  const weekDay = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][now.getDay()]
  currentTime.value = `${y}年${m}月${d}日 ${weekDay} ${h}:${min}:${s}`
}

onMounted(() => {
  updateTime()
  setInterval(updateTime, 1000)
})

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
    <!-- Animated background -->
    <div class="bg-gradient"></div>
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>
    <div class="bg-shape shape-3"></div>
    <div class="bg-shape shape-4"></div>

    <!-- Floating particles -->
    <div class="particle" v-for="n in 12" :key="n" :class="`p-${n}`"></div>

    <!-- Login card -->
    <div class="login-container">
      <div class="login-card">
        <!-- Left panel: info -->
        <div class="card-left">
          <div class="brand-section">
            <div class="logo-wrapper">
              <CarOutlined class="logo-icon" />
            </div>
            <h1 class="brand-title">智能停车场</h1>
            <h2 class="brand-subtitle">管理系统</h2>
          </div>

          <!-- Parking Scene SVG Animation -->
          <div class="parking-scene">
            <svg viewBox="0 0 300 120" class="parking-svg">
              <!-- Road -->
              <rect x="0" y="80" width="300" height="40" fill="rgba(255,255,255,0.05)" rx="4"/>
              <!-- Road lines -->
              <line x1="0" y1="100" x2="40" y2="100" stroke="rgba(255,255,255,0.15)" stroke-width="2" stroke-dasharray="12 8"/>
              <line x1="60" y1="100" x2="100" y2="100" stroke="rgba(255,255,255,0.15)" stroke-width="2" stroke-dasharray="12 8"/>
              <line x1="120" y1="100" x2="160" y2="100" stroke="rgba(255,255,255,0.15)" stroke-width="2" stroke-dasharray="12 8"/>
              <line x1="180" y1="100" x2="220" y2="100" stroke="rgba(255,255,255,0.15)" stroke-width="2" stroke-dasharray="12 8"/>
              <line x1="240" y1="100" x2="280" y2="100" stroke="rgba(255,255,255,0.15)" stroke-width="2" stroke-dasharray="12 8"/>
              <!-- Parking spots -->
              <rect x="30" y="40" width="50" height="38" fill="rgba(59,130,246,0.12)" stroke="rgba(59,130,246,0.3)" stroke-width="1" rx="3"/>
              <text x="55" y="63" fill="rgba(59,130,246,0.5)" font-size="10" text-anchor="middle">P1</text>
              <rect x="90" y="40" width="50" height="38" fill="rgba(16,185,129,0.12)" stroke="rgba(16,185,129,0.3)" stroke-width="1" rx="3"/>
              <text x="115" y="63" fill="rgba(16,185,129,0.5)" font-size="10" text-anchor="middle">P2</text>
              <rect x="150" y="40" width="50" height="38" fill="rgba(59,130,246,0.12)" stroke="rgba(59,130,246,0.3)" stroke-width="1" rx="3"/>
              <text x="175" y="63" fill="rgba(59,130,246,0.5)" font-size="10" text-anchor="middle">P3</text>
              <rect x="210" y="40" width="50" height="38" fill="rgba(245,158,11,0.12)" stroke="rgba(245,158,11,0.3)" stroke-width="1" rx="3"/>
              <text x="235" y="63" fill="rgba(245,158,11,0.5)" font-size="10" text-anchor="middle">VIP</text>
              <!-- Gate barrier -->
              <rect x="5" y="70" width="3" height="15" fill="rgba(255,255,255,0.4)" rx="1"/>
              <rect x="5" y="68" width="25" height="3" fill="rgba(239,68,68,0.6)" rx="1" class="gate-arm"/>
              <!-- Animated car -->
              <g class="car-entry">
                <rect x="0" y="86" width="28" height="12" fill="rgba(59,130,246,0.8)" rx="3"/>
                <rect x="5" y="82" width="16" height="6" fill="rgba(59,130,246,0.6)" rx="2"/>
                <circle cx="7" cy="100" r="3" fill="rgba(255,255,255,0.3)"/>
                <circle cx="21" cy="100" r="3" fill="rgba(255,255,255,0.3)"/>
              </g>
            </svg>
          </div>

          <div class="features-list">
            <div class="feature-item">
              <SafetyCertificateOutlined />
              <span>车位实时监控 · 智能调度</span>
            </div>
            <div class="feature-item">
              <SafetyCertificateOutlined />
              <span>自动计费 · 多端聚合支付</span>
            </div>
            <div class="feature-item">
              <SafetyCertificateOutlined />
              <span>数据驾驶舱 · 运营决策</span>
            </div>
          </div>

          <div class="tech-tags">
            <span class="tech-tag">Spring Boot 3</span>
            <span class="tech-tag">Vue 3</span>
            <span class="tech-tag">MyBatis-Plus</span>
            <span class="tech-tag">MySQL 8</span>
            <span class="tech-tag">JWT</span>
          </div>

          <div class="time-display">{{ currentTime }}</div>
        </div>

        <!-- Right panel: form -->
        <div class="card-right">
          <div class="form-header">
            <h3 class="form-title">管理后台登录</h3>
            <p class="form-desc">请使用管理员账号登录系统</p>
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
                <CarOutlined style="margin-right: 6px" />
                登 录
              </a-button>
            </a-form-item>
          </a-form>

          <div class="login-footer">
            <span>© 2026 智能停车场管理系统 · 毕业设计</span>
          </div>
        </div>
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
  position: relative;
  overflow: hidden;
  background: #0f172a;
}

/* Animated gradient background */
.bg-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 30%, #1a365d 50%, #0f172a 100%);
  animation: gradientShift 8s ease-in-out infinite alternate;
}

@keyframes gradientShift {
  0% { background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 30%, #1a365d 50%, #0f172a 100%); }
  100% { background: linear-gradient(135deg, #0f172a 0%, #1a365d 40%, #1e3a5f 60%, #0f172a 100%); }
}

/* Floating decorative shapes */
.bg-shape {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.shape-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.15) 0%, transparent 70%);
  top: -150px;
  left: -100px;
  animation: float1 12s ease-in-out infinite;
}

.shape-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.12) 0%, transparent 70%);
  bottom: -120px;
  right: -80px;
  animation: float2 15s ease-in-out infinite;
}

.shape-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.1) 0%, transparent 70%);
  top: 60%;
  right: 20%;
  animation: float3 10s ease-in-out infinite;
}

.shape-4 {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.08) 0%, transparent 70%);
  top: 10%;
  left: 60%;
  animation: float1 18s ease-in-out infinite reverse;
}

@keyframes float1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -40px) scale(1.05); }
}

@keyframes float2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-25px, 30px) scale(1.08); }
}

@keyframes float3 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(20px, -20px); }
}

/* Floating particles */
.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  pointer-events: none;
}

.p-1 { top: 10%; left: 15%; animation: sparkle 4s ease-in-out infinite 0s; }
.p-2 { top: 25%; left: 80%; animation: sparkle 5s ease-in-out infinite 0.5s; width: 3px; height: 3px; }
.p-3 { top: 50%; left: 10%; animation: sparkle 6s ease-in-out infinite 1s; width: 5px; height: 5px; }
.p-4 { top: 70%; left: 85%; animation: sparkle 4.5s ease-in-out infinite 1.5s; }
.p-5 { top: 85%; left: 30%; animation: sparkle 5.5s ease-in-out infinite 2s; width: 3px; height: 3px; }
.p-6 { top: 15%; left: 50%; animation: sparkle 4s ease-in-out infinite 2.5s; width: 6px; height: 6px; }
.p-7 { top: 40%; left: 90%; animation: sparkle 6s ease-in-out infinite 0.8s; }
.p-8 { top: 60%; left: 5%; animation: sparkle 5s ease-in-out infinite 1.2s; width: 5px; height: 5px; }
.p-9 { top: 90%; left: 70%; animation: sparkle 4.5s ease-in-out infinite 1.8s; width: 3px; height: 3px; }
.p-10 { top: 5%; left: 35%; animation: sparkle 5.5s ease-in-out infinite 0.3s; }
.p-11 { top: 35%; left: 65%; animation: sparkle 4s ease-in-out infinite 2.2s; width: 4px; height: 4px; }
.p-12 { top: 75%; left: 45%; animation: sparkle 6s ease-in-out infinite 1.6s; width: 3px; height: 3px; }

@keyframes sparkle {
  0%, 100% { opacity: 0; transform: translateY(0) scale(1); }
  50% { opacity: 1; transform: translateY(-20px) scale(1.5); }
}

/* Login container */
.login-container {
  position: relative;
  z-index: 1;
  padding: 20px;
}

.login-card {
  display: flex;
  width: 860px;
  min-height: 520px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(24px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.06);
  overflow: hidden;
  animation: cardAppear 0.6s ease-out;
}

@keyframes cardAppear {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Left panel */
.card-left {
  width: 380px;
  padding: 48px 36px;
  background: linear-gradient(160deg, rgba(59, 130, 246, 0.15) 0%, rgba(99, 102, 241, 0.08) 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  flex-direction: column;
  color: #fff;
}

.brand-section {
  margin-bottom: 40px;
}

.logo-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.4);
}

.logo-icon {
  font-size: 28px;
  color: #fff;
}

.brand-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2px;
  line-height: 1.3;
}

.brand-subtitle {
  margin: 4px 0 0;
  font-size: 22px;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 4px;
}

.features-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.65);
}

.feature-item :deep(.anticon) {
  font-size: 14px;
  color: #3b82f6;
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 32px;
}

.tech-tag {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.04);
}

.time-display {
  margin-top: 16px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
  font-family: 'Courier New', monospace;
}

/* Right panel: form */
.card-right {
  flex: 1;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.97);
  display: flex;
  flex-direction: column;
}

.form-header {
  margin-bottom: 32px;
}

.form-title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #1e293b;
}

.form-desc {
  margin: 0;
  font-size: 14px;
  color: #94a3b8;
}

.login-btn {
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  border: none;
  letter-spacing: 3px;
  transition: all 0.3s;
}

.login-btn:hover {
  background: linear-gradient(135deg, #2563eb 0%, #4f46e5 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.3);
}

.login-footer {
  text-align: center;
  margin-top: auto;
  padding-top: 24px;
  color: #94a3b8;
  font-size: 12px;
}

/* Override Ant input styles */
:deep(.ant-input-affix-wrapper) {
  border-radius: 10px;
  height: 46px;
  border-color: #e2e8f0;
}

:deep(.ant-input-affix-wrapper:hover),
:deep(.ant-input-affix-wrapper-focused) {
  border-color: #3b82f6;
}

:deep(.ant-input-password) {
  border-radius: 10px;
  height: 46px;
  border-color: #e2e8f0;
}

/* Parking Scene */
.parking-scene {
  margin: 16px 0;
  padding: 0 8px;
}
.parking-svg {
  width: 100%;
  height: auto;
}
.car-entry {
  animation: carDrive 6s ease-in-out infinite;
}
@keyframes carDrive {
  0% { transform: translateX(-40px); opacity: 0; }
  10% { opacity: 1; }
  50% { transform: translateX(100px); opacity: 1; }
  60% { transform: translateX(100px); opacity: 1; }
  70% { transform: translateX(100px); opacity: 0; }
  100% { transform: translateX(100px); opacity: 0; }
}
.gate-arm {
  transform-origin: 5px 69px;
  animation: gateOpen 6s ease-in-out infinite;
}
@keyframes gateOpen {
  0%, 5% { transform: rotate(0deg); }
  10%, 55% { transform: rotate(-80deg); }
  60%, 100% { transform: rotate(0deg); }
}

/* Responsive */
@media (max-width: 900px) {
  .login-card {
    flex-direction: column;
    width: 420px;
    min-height: auto;
  }
  .card-left {
    width: 100%;
    padding: 32px 28px;
  }
  .features-list, .tech-tags, .parking-scene {
    display: none;
  }
  .brand-section {
    margin-bottom: 0;
    display: flex;
    align-items: center;
    gap: 16px;
  }
  .logo-wrapper {
    margin-bottom: 0;
  }
  .brand-title {
    font-size: 20px;
  }
  .brand-subtitle {
    font-size: 16px;
    margin: 0;
  }
  .time-display {
    display: none;
  }
}
</style>
