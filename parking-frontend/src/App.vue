<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NConfigProvider,
  NButton,
  NIcon,
  NAvatar,
  NDropdown,
  NMessageProvider,
  NDialogProvider,
} from 'naive-ui'
import { CarSportOutline, PersonOutline } from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const showNav = computed(() => !route.meta.hideNav)

const themeOverrides = {
  common: {
    primaryColor: '#2D6A4F',
    primaryColorHover: '#3D8B6A',
    primaryColorPressed: '#1D5A3F',
    primaryColorSuppl: '#2D6A4F',
  },
}

const navLinks = [
  { label: '首页', path: '/' },
  { label: '车位查询', path: '/parking' },
  { label: '停车公告', path: '/announcements' },
]

const userMenuOptions = [
  { label: '个人中心', key: 'profile' },
  { label: '我的订单', key: 'orders' },
  { label: '我的预约', key: 'reservations' },
  { label: '我的车辆', key: 'vehicles' },
  { label: '我的评价', key: 'reviews' },
  { label: '我的消息', key: 'messages' },
  { type: 'divider', key: 'd1' },
  { label: '退出登录', key: 'logout' },
]

function handleUserMenu(key: string | number) {
  if (key === 'logout') {
    userStore.logout()
    router.push('/')
  } else if (typeof key === 'string') {
    router.push(`/${key}`)
  }
}

function navigateTo(path: string) {
  router.push(path)
}
</script>

<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider>
    <n-dialog-provider>
      <div class="app-wrapper">
        <header v-if="showNav" class="app-header">
          <div class="header-inner">
            <router-link to="/" class="logo">
              <n-icon size="28" color="#2D6A4F">
                <CarSportOutline />
              </n-icon>
              <span class="logo-text">智能停车场</span>
            </router-link>

            <nav class="nav-links">
              <router-link
                v-for="link in navLinks"
                :key="link.path"
                :to="link.path"
                :class="['nav-link', { active: route.path === link.path }]"
              >
                {{ link.label }}
              </router-link>
            </nav>

            <div class="auth-area">
              <template v-if="userStore.isLoggedIn">
                <n-dropdown :options="userMenuOptions" @select="handleUserMenu">
                  <div class="user-info-trigger">
                    <n-avatar
                      round
                      :size="32"
                      :style="{ backgroundColor: '#2D6A4F', cursor: 'pointer' }"
                    >
                      {{ (userStore.userInfo?.username || 'U').charAt(0).toUpperCase() }}
                    </n-avatar>
                    <span class="username">
                      {{ userStore.userInfo?.realName || userStore.userInfo?.username }}
                    </span>
                  </div>
                </n-dropdown>
              </template>
              <template v-else>
                <n-button quaternary @click="navigateTo('/login')">
                  <template #icon>
                    <n-icon><PersonOutline /></n-icon>
                  </template>
                  登录
                </n-button>
              </template>
            </div>
          </div>
        </header>

        <main>
          <router-view />
        </main>
      </div>
    </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #2d6a4f;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-link {
  font-size: 15px;
  color: #64748b;
  transition: color 0.2s;
  text-decoration: none;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
}

.nav-link:hover,
.nav-link.active {
  color: #2d6a4f;
}

.nav-link.active {
  border-bottom-color: #2d6a4f;
}

.auth-area {
  display: flex;
  align-items: center;
}

.user-info-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #1e293b;
}

main {
  flex: 1;
}

@media (max-width: 640px) {
  .nav-links {
    gap: 16px;
  }
  .nav-link {
    font-size: 14px;
  }
  .username {
    display: none;
  }
}
</style>
