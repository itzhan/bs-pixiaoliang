<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <div class="error-illustration">
        <svg width="200" height="140" viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="30" y="40" width="140" height="80" rx="8" fill="#F1F5F9" stroke="#CBD5E1" stroke-width="2"/>
          <circle cx="75" cy="72" r="12" fill="#E2E8F0"/>
          <circle cx="125" cy="72" r="12" fill="#E2E8F0"/>
          <path d="M72 71 L78 77 M78 71 L72 77" stroke="#94A3B8" stroke-width="2" stroke-linecap="round"/>
          <path d="M122 71 L128 77 M128 71 L122 77" stroke="#94A3B8" stroke-width="2" stroke-linecap="round"/>
          <path d="M80 98 Q100 88 120 98" stroke="#94A3B8" stroke-width="2" stroke-linecap="round" fill="none"/>
          <rect x="90" y="20" width="20" height="24" rx="4" fill="#2D6A4F" opacity="0.2"/>
          <line x1="100" y1="8" x2="100" y2="20" stroke="#2D6A4F" stroke-width="2" opacity="0.3"/>
        </svg>
      </div>
      <h2 class="error-title">页面未找到</h2>
      <p class="error-desc">抱歉，您访问的页面不存在或已被移除</p>
      <n-button type="primary" size="large" @click="goHome">
        返回首页
      </n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
  padding: 20px;
}

.not-found-content {
  text-align: center;
  max-width: 400px;
}

.error-code {
  font-size: 96px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-light) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 16px;
}

.error-illustration {
  margin-bottom: 24px;
}

.error-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.error-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 32px 0;
}
</style>
