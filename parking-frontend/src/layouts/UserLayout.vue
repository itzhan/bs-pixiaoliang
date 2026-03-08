<script setup lang="ts">
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NMenu,
  NButton,
  NDropdown,
  NAvatar,
  NSpace,
  NIcon,
} from 'naive-ui'
import {
  HomeOutline,
  CarSportOutline,
  MegaphoneOutline,
  PersonCircleOutline,
  CalendarOutline,
  ReceiptOutline,
  LogOutOutline,
  CarOutline,
} from '@vicons/ionicons5'
import type { MenuOption } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// ---- Navigation Menu ----
const menuOptions: MenuOption[] = [
  {
    label: '首页',
    key: '/',
    icon: () => h(NIcon, null, { default: () => h(HomeOutline) }),
  },
  {
    label: '停车位查询',
    key: '/parking',
    icon: () => h(NIcon, null, { default: () => h(CarSportOutline) }),
  },
  {
    label: '公告',
    key: '/announcements',
    icon: () => h(NIcon, null, { default: () => h(MegaphoneOutline) }),
  },
]

const activeKey = computed(() => {
  const path = route.path
  if (path === '/') return '/'
  // Match first-level path
  const matched = menuOptions.find((opt) => path.startsWith(opt.key as string) && opt.key !== '/')
  return matched ? (matched.key as string) : ''
})

function handleMenuSelect(key: string) {
  router.push(key)
}

// ---- User Dropdown ----
const dropdownOptions = [
  { label: '个人中心', key: 'profile', icon: () => h(NIcon, { size: 16 }, { default: () => h(PersonCircleOutline) }) },
  { label: '我的车辆', key: 'vehicles', icon: () => h(NIcon, { size: 16 }, { default: () => h(CarOutline) }) },
  { label: '我的预约', key: 'reservation', icon: () => h(NIcon, { size: 16 }, { default: () => h(CalendarOutline) }) },
  { label: '我的订单', key: 'orders', icon: () => h(NIcon, { size: 16 }, { default: () => h(ReceiptOutline) }) },
  { type: 'divider' as const, key: 'd1' },
  { label: '退出登录', key: 'logout', icon: () => h(NIcon, { size: 16 }, { default: () => h(LogOutOutline) }) },
]

function handleDropdownSelect(key: string) {
  if (key === 'logout') {
    userStore.logout()
  } else {
    router.push(`/${key}`)
  }
}

const displayName = computed(() => userStore.realName || userStore.username || '用户')
</script>

<template>
  <n-layout style="min-height: 100vh;">
    <!-- ===== Top Navbar ===== -->
    <n-layout-header bordered class="navbar">
      <div class="navbar-inner">
        <!-- Left: Logo -->
        <div class="navbar-brand" @click="router.push('/')">
          <svg class="logo-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="10" fill="#2D6A4F" />
            <text x="10" y="28" font-size="22" font-weight="bold" fill="#FFFFFF" font-family="Arial">P</text>
          </svg>
          <span class="brand-text">智能停车场</span>
        </div>

        <!-- Center: Navigation -->
        <div class="navbar-menu">
          <n-menu
            mode="horizontal"
            :options="menuOptions"
            :value="activeKey"
            @update:value="handleMenuSelect"
          />
        </div>

        <!-- Right: User Area -->
        <div class="navbar-user">
          <template v-if="userStore.isLoggedIn">
            <n-dropdown
              :options="dropdownOptions"
              trigger="click"
              @select="handleDropdownSelect"
            >
              <div class="user-trigger">
                <n-avatar
                  round
                  :size="32"
                  :src="userStore.avatar || undefined"
                  :style="{ backgroundColor: '#2D6A4F', cursor: 'pointer' }"
                >
                  {{ displayName.charAt(0) }}
                </n-avatar>
                <span class="user-name">{{ displayName }}</span>
              </div>
            </n-dropdown>
          </template>
          <template v-else>
            <n-space :size="8">
              <n-button
                size="small"
                quaternary
                @click="router.push('/login')"
                style="color: var(--primary);"
              >
                登录
              </n-button>
              <n-button
                size="small"
                type="primary"
                @click="router.push('/register')"
              >
                注册
              </n-button>
            </n-space>
          </template>
        </div>
      </div>
    </n-layout-header>

    <!-- ===== Main Content ===== -->
    <n-layout-content class="main-content">
      <router-view />
    </n-layout-content>

    <!-- ===== Footer ===== -->
    <n-layout-footer class="footer">
      <div class="footer-inner">
        <span>© {{ new Date().getFullYear() }} 智能停车场管理系统 — 毕业设计</span>
      </div>
    </n-layout-footer>
  </n-layout>
</template>

<style scoped>
/* ===== Navbar ===== */
.navbar {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
}

/* Brand */
.navbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
}

.brand-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary);
  white-space: nowrap;
}

/* Center menu */
.navbar-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.navbar-menu :deep(.n-menu) {
  background: transparent;
}

.navbar-menu :deep(.n-menu-item-content) {
  font-size: 15px;
}

/* User area */
.navbar-user {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background-color 150ms ease;
}

.user-trigger:hover {
  background-color: rgba(45, 106, 79, 0.06);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

/* ===== Main Content ===== */
.main-content {
  min-height: calc(100vh - 60px - 60px);
  background-color: var(--bg-page);
}

/* ===== Footer ===== */
.footer {
  background: #fff;
  border-top: 1px solid var(--border-color);
  padding: 0;
}

.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 18px 24px;
  text-align: center;
  color: var(--text-tertiary);
  font-size: 13px;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .navbar-inner {
    padding: 0 12px;
  }

  .brand-text {
    display: none;
  }

  .user-name {
    display: none;
  }
}
</style>
