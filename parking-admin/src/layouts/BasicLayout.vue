<script setup lang="ts">
import { ref, computed, watch, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  DashboardOutlined,
  CarOutlined,
  MoneyCollectOutlined,
  TeamOutlined,
  FileTextOutlined,
  LogoutOutlined,
  UserOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons-vue'
import type { ItemType } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const collapsed = ref(false)

// Current selected key derived from route
const selectedKeys = computed(() => {
  const path = route.path
  return [path]
})

// Open keys for sub-menus
const openKeys = ref<string[]>([])

// Determine initial open keys from current route
watch(
  () => route.path,
  (path) => {
    const menuGroupMap: Record<string, string> = {
      '/parking-areas': 'parking',
      '/parking-spaces': 'parking',
      '/reservations': 'parking',
      '/orders': 'parking',
      '/entry-exit-logs': 'parking',
      '/billing-rules': 'billing',
      '/payments': 'billing',
      '/users': 'user',
      '/vehicles': 'user',
      '/blacklist': 'user',
      '/announcements': 'content',
      '/reviews': 'content',
    }
    const group = menuGroupMap[path]
    if (group && !openKeys.value.includes(group)) {
      openKeys.value = [...openKeys.value, group]
    }
  },
  { immediate: true },
)

// Sidebar menu items using Ant Design Vue 4 items prop
const menuItems = computed<ItemType[]>(() => [
  {
    key: '/dashboard',
    icon: () => h(DashboardOutlined),
    label: '数据概览',
  },
  {
    key: 'parking',
    icon: () => h(CarOutlined),
    label: '停车管理',
    children: [
      { key: '/parking-areas', label: '停车区域' },
      { key: '/parking-spaces', label: '车位管理' },
      { key: '/reservations', label: '预约管理' },
      { key: '/orders', label: '停车订单' },
      { key: '/entry-exit-logs', label: '出入日志' },
    ],
  },
  {
    key: 'billing',
    icon: () => h(MoneyCollectOutlined),
    label: '计费管理',
    children: [
      { key: '/billing-rules', label: '计费规则' },
      { key: '/payments', label: '支付记录' },
    ],
  },
  {
    key: 'user',
    icon: () => h(TeamOutlined),
    label: '用户管理',
    children: [
      { key: '/users', label: '用户列表' },
      { key: '/vehicles', label: '车辆管理' },
      { key: '/blacklist', label: '黑名单' },
    ],
  },
  {
    key: 'content',
    icon: () => h(FileTextOutlined),
    label: '内容管理',
    children: [
      { key: '/announcements', label: '公告管理' },
      { key: '/reviews', label: '评价管理' },
    ],
  },

])

function onMenuClick({ key }: { key: string }) {
  if (key.startsWith('/')) {
    router.push(key)
  }
}

function handleLogout() {
  userStore.logout()
}
</script>

<template>
  <a-layout class="admin-layout">
    <!-- Sidebar -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      class="admin-sider"
      :trigger="null"
      collapsible
      theme="dark"
      :width="220"
    >
      <div class="logo">
        <CarOutlined style="font-size: 24px; margin-right: 8px" />
        <span v-show="!collapsed">智慧停车管理</span>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        mode="inline"
        theme="dark"
        :items="menuItems"
        @click="onMenuClick"
      />
    </a-layout-sider>

    <!-- Main area -->
    <a-layout :style="{ marginLeft: collapsed ? '80px' : '220px', transition: 'margin-left 0.2s' }">
      <!-- Header -->
      <a-layout-header class="admin-header">
        <div class="header-left">
          <component
            :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined"
            style="font-size: 18px; cursor: pointer"
            @click="collapsed = !collapsed"
          />
          <a-breadcrumb>
            <a-breadcrumb-item>首页</a-breadcrumb-item>
            <a-breadcrumb-item>{{ route.meta.title }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <div class="header-right">
          <a-dropdown>
            <div class="user-info">
              <a-avatar :size="32">
                <template #icon><UserOutlined /></template>
              </a-avatar>
              <span>{{ userStore.realName || userStore.username || '管理员' }}</span>
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined />
                  <span style="margin-left: 8px">退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- Content -->
      <a-layout-content class="admin-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
