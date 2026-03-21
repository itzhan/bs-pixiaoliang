<template>
  <div class="messages-page">
    <!-- ===== Hero Header ===== -->
    <section class="page-hero">
      <div class="hero-bg">
        <div class="hero-shape hero-shape-1" />
        <div class="hero-shape hero-shape-2" />
      </div>
      <div class="hero-content">
        <div class="hero-icon-circle">
          <n-icon :size="32" color="#10B981"><NotificationsOutline /></n-icon>
        </div>
        <h1 class="hero-title">我的消息</h1>
        <p class="hero-subtitle">查看系统通知和订单消息动态</p>
      </div>
    </section>

    <div class="page-container">
      <!-- ===== Top Bar ===== -->
      <div class="msg-top-bar">
        <div class="unread-info">
          <div v-if="unreadCount > 0" class="unread-badge-row">
            <span class="unread-dot-large" />
            <span class="unread-hint">
              未读 <strong>{{ unreadCount }}</strong> 条
            </span>
          </div>
          <span v-else class="unread-hint all-read">
            <n-icon :size="16" color="#2D6A4F" style="margin-right: 4px; vertical-align: -3px;"><CheckmarkCircleOutline /></n-icon>
            暂无未读消息
          </span>
        </div>
        <n-button
          text
          type="primary"
          size="small"
          :disabled="unreadCount === 0"
          @click="handleMarkAllRead"
        >
          <template #icon><n-icon :size="14"><CheckmarkDoneOutline /></n-icon></template>
          全部标为已读
        </n-button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-wrapper">
        <n-spin size="large" />
      </div>

      <!-- Empty -->
      <n-empty v-else-if="messages.length === 0" description="暂无消息" style="margin-top: 60px" />

      <!-- ===== Message List ===== -->
      <div v-else class="message-list">
        <div
          v-for="item in messages"
          :key="item.id"
          class="message-item"
          :class="{ unread: !item.isRead }"
          @click="handleClickMessage(item)"
        >
          <!-- Type Icon -->
          <div class="msg-type-icon" :style="{ background: getTypeIconBg(item.type) }">
            <n-icon :size="18" :color="getTypeIconColor(item.type)">
              <component :is="getTypeIcon(item.type)" />
            </n-icon>
          </div>

          <div class="msg-body">
            <div class="msg-header">
              <div class="msg-title-row">
                <span v-if="!item.isRead" class="unread-dot" />
                <span class="msg-title" :class="{ 'font-bold': !item.isRead }">{{ item.title }}</span>
              </div>
              <n-space :size="8" align="center">
                <n-tag v-if="item.type" size="tiny" :bordered="false" :type="messageTypeTagType(item.type)">{{ messageTypeLabel(item.type) }}</n-tag>
                <span class="msg-time">{{ formatTime(item.createdAt) }}</span>
              </n-space>
            </div>

            <n-collapse-transition :show="expandedId === item.id">
              <div class="msg-content">
                {{ item.content }}
              </div>
            </n-collapse-transition>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-wrapper" v-if="total > pageSize">
        <n-pagination
          v-model:page="currentPage"
          :page-size="pageSize"
          :item-count="total"
          @update:page="fetchList"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, markRaw } from 'vue'
import { useMessage, NIcon } from 'naive-ui'
import dayjs from 'dayjs'
import { getMessages, getUnreadCount, markAsRead, markAllAsRead } from '@/api/message'
import {
  NotificationsOutline,
  CheckmarkCircleOutline,
  CheckmarkDoneOutline,
  MegaphoneOutline,
  ReceiptOutline,
  CardOutline,
  CalendarOutline,
  InformationCircleOutline,
} from '@vicons/ionicons5'

const msg = useMessage()

function formatTime(t: string) {
  return t ? dayjs(t).format('MM-DD HH:mm') : '—'
}

// --- Message Type ---
const messageTypeMap: Record<string, string> = {
  SYSTEM: '系统通知',
  ORDER: '订单消息',
  PAYMENT: '支付消息',
  RESERVATION: '预约消息',
  NOTICE: '公告',
}

function messageTypeLabel(t: string) {
  return messageTypeMap[t] ?? t ?? '消息'
}

function messageTypeTagType(t: string): 'info' | 'warning' | 'success' | 'default' {
  const map: Record<string, 'info' | 'warning' | 'success' | 'default'> = {
    SYSTEM: 'info',
    ORDER: 'default',
    PAYMENT: 'warning',
    RESERVATION: 'success',
    NOTICE: 'info',
  }
  return map[t] ?? 'default'
}

function getTypeIcon(type: string) {
  const map: Record<string, any> = {
    SYSTEM: markRaw(InformationCircleOutline),
    ORDER: markRaw(ReceiptOutline),
    PAYMENT: markRaw(CardOutline),
    RESERVATION: markRaw(CalendarOutline),
    NOTICE: markRaw(MegaphoneOutline),
  }
  return map[type] ?? markRaw(InformationCircleOutline)
}

function getTypeIconColor(type: string): string {
  const map: Record<string, string> = {
    SYSTEM: '#3B82F6',
    ORDER: '#2D6A4F',
    PAYMENT: '#E07A5F',
    RESERVATION: '#10B981',
    NOTICE: '#8B5CF6',
  }
  return map[type] ?? '#94A3B8'
}

function getTypeIconBg(type: string): string {
  const map: Record<string, string> = {
    SYSTEM: 'rgba(59,130,246,0.08)',
    ORDER: 'rgba(45,106,79,0.08)',
    PAYMENT: 'rgba(224,122,95,0.08)',
    RESERVATION: 'rgba(16,185,129,0.08)',
    NOTICE: 'rgba(139,92,246,0.08)',
  }
  return map[type] ?? 'rgba(148,163,184,0.08)'
}

// --- Data ---
const loading = ref(false)
const messages = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const unreadCount = ref(0)
const expandedId = ref<number | null>(null)

async function fetchList() {
  loading.value = true
  try {
    const res: any = await getMessages({ page: currentPage.value, size: pageSize })
    messages.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } catch (e: any) {
    msg.error(e.message || '获取消息列表失败')
  } finally {
    loading.value = false
  }
}

async function fetchUnreadCount() {
  try {
    const res: any = await getUnreadCount()
    unreadCount.value = res.data ?? 0
  } catch {
    // silent
  }
}

async function handleClickMessage(item: any) {
  // Toggle expand
  if (expandedId.value === item.id) {
    expandedId.value = null
    return
  }
  expandedId.value = item.id

  // Mark as read if unread
  if (!item.isRead) {
    try {
      await markAsRead(item.id)
      item.isRead = true
      if (unreadCount.value > 0) unreadCount.value--
    } catch {
      // silent
    }
  }
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    messages.value.forEach((m) => (m.isRead = true))
    unreadCount.value = 0
    msg.success('已全部标为已读')
  } catch (e: any) {
    msg.error(e.message || '操作失败')
  }
}

onMounted(() => {
  fetchList()
  fetchUnreadCount()
})
</script>

<style scoped>
/* ===== Hero Header ===== */
.page-hero {
  position: relative;
  z-index: 0;
  padding: 40px 24px 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 50%, #f0f7f4 100%);
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
  opacity: 0.12;
}

.hero-shape-1 {
  width: 280px;
  height: 280px;
  background: #10B981;
  top: -80px;
  right: -60px;
}

.hero-shape-2 {
  width: 180px;
  height: 180px;
  background: #2d6a4f;
  bottom: -60px;
  left: -40px;
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.hero-icon-circle {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: rgba(16, 185, 129, 0.10);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.hero-subtitle {
  font-size: 15px;
  color: #94a3b8;
  margin: 0;
}

/* ===== Container ===== */
.messages-page .page-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px 48px;
  position: relative;
  z-index: 1;
}

/* ===== Top Bar ===== */
.msg-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 14px 20px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.unread-badge-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.unread-dot-large {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--accent);
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.2); }
}

.unread-hint {
  font-size: 14px;
  color: var(--text-secondary);
}

.unread-hint strong {
  color: var(--accent);
  font-size: 16px;
}

.unread-hint.all-read {
  color: var(--primary);
  font-weight: 500;
}

/* ===== Message List ===== */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-item {
  display: flex;
  gap: 14px;
  padding: 16px 18px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition-normal), transform var(--transition-normal);
}

.message-item:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.message-item.unread {
  background: linear-gradient(90deg, rgba(45, 106, 79, 0.03) 0%, var(--bg-card) 100%);
  border-left: 3px solid var(--primary);
}

.msg-type-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.msg-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
  flex-shrink: 0;
}

.msg-title {
  font-size: 14px;
  color: var(--text-primary);
}

.font-bold {
  font-weight: 600;
}

.msg-time {
  font-size: 12px;
  color: var(--text-tertiary);
  white-space: nowrap;
}

.msg-content {
  margin-top: 10px;
  padding: 12px 14px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .page-hero {
    padding: 32px 16px 40px;
  }

  .hero-title {
    font-size: 22px;
  }

  .messages-page .page-container {
    padding: 24px 16px 40px;
  }

  .msg-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
