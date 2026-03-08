<template>
  <div class="page-container">
    <h2 class="section-title">我的消息</h2>

    <!-- Top Bar -->
    <div class="msg-top-bar">
      <span class="unread-hint" v-if="unreadCount > 0">
        未读 <strong>{{ unreadCount }}</strong> 条
      </span>
      <span class="unread-hint" v-else>暂无未读消息</span>
      <n-button
        text
        type="primary"
        size="small"
        :disabled="unreadCount === 0"
        @click="handleMarkAllRead"
      >
        全部标为已读
      </n-button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-wrapper">
      <n-spin size="large" />
    </div>

    <!-- Empty -->
    <n-empty v-else-if="messages.length === 0" description="暂无消息" style="margin-top: 60px" />

    <!-- Message List -->
    <div v-else class="message-list">
      <div
        v-for="item in messages"
        :key="item.id"
        class="message-item card-hover"
        :class="{ unread: !item.isRead }"
        @click="handleClickMessage(item)"
      >
        <div class="msg-header">
          <div class="msg-title-row">
            <span v-if="!item.isRead" class="unread-dot"></span>
            <span class="msg-title" :class="{ 'font-bold': !item.isRead }">{{ item.title }}</span>
          </div>
          <n-space :size="8" align="center">
            <n-tag v-if="item.type" size="tiny" :bordered="false">{{ messageTypeLabel(item.type) }}</n-tag>
            <span class="msg-time">{{ formatTime(item.createTime) }}</span>
          </n-space>
        </div>

        <n-collapse-transition :show="expandedId === item.id">
          <div class="msg-content">
            {{ item.content }}
          </div>
        </n-collapse-transition>
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
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import dayjs from 'dayjs'
import { getMessages, getUnreadCount, markAsRead, markAllAsRead } from '@/api/message'

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
.msg-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-sm);
}

.unread-hint {
  font-size: 14px;
  color: var(--text-secondary);
}

.unread-hint strong {
  color: var(--accent);
  font-size: 16px;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-item {
  padding: 14px 18px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.message-item:hover {
  background: rgba(45, 106, 79, 0.04);
}

.message-item.unread {
  background: rgba(45, 106, 79, 0.06);
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

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
</style>
