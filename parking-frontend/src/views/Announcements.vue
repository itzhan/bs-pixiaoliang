<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NTag, NSpin, NEmpty } from 'naive-ui'
import { getPublishedAnnouncements } from '@/api/announcement'
import dayjs from 'dayjs'

/* ---------- Types ---------- */
interface AnnouncementItem {
  id: number
  title: string
  content: string
  announcementType: string
  publishedAt: string
}

/* ---------- State ---------- */
const announcements = ref<AnnouncementItem[]>([])
const loading = ref(false)

/* ---------- Helpers ---------- */
const typeTagMap: Record<
  string,
  { label: string; type: 'info' | 'warning' | 'success' | 'default' }
> = {
  NOTICE: { label: '通知', type: 'info' },
  MAINTENANCE: { label: '维护', type: 'warning' },
  PROMOTION: { label: '优惠', type: 'success' },
  OTHER: { label: '其他', type: 'default' },
}

function getTypeTag(type: string) {
  return typeTagMap[type] || typeTagMap.OTHER
}

function formatDate(date: string) {
  return dayjs(date).format('YYYY年MM月DD日')
}

/* ---------- Data loading ---------- */
async function loadAnnouncements() {
  loading.value = true
  try {
    const res: any = await getPublishedAnnouncements()
    announcements.value = res.data || []
  } catch {
    /* handled by interceptor */
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<template>
  <div class="announcements-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">停车场公告</h1>
        <p class="page-subtitle">了解停车场最新通知、维护信息和优惠活动</p>
      </div>

      <n-spin :show="loading">
        <div class="announcements-list">
          <n-card
            v-for="item in announcements"
            :key="item.id"
            hoverable
            class="announcement-card"
          >
            <div class="announcement-header">
              <n-tag
                :type="getTypeTag(item.announcementType).type"
                size="small"
              >
                {{ getTypeTag(item.announcementType).label }}
              </n-tag>
              <span class="announcement-date">
                {{ formatDate(item.publishedAt) }}
              </span>
            </div>
            <h2 class="announcement-title">{{ item.title }}</h2>
            <p class="announcement-content">{{ item.content }}</p>
          </n-card>
        </div>
        <n-empty
          v-if="!loading && announcements.length === 0"
          description="暂无公告"
          style="padding: 80px 0"
        />
      </n-spin>
    </div>
  </div>
</template>

<style scoped>
.announcements-page {
  padding: 32px 0 64px;
  background: #f8f9fa;
  min-height: calc(100vh - 64px);
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.page-subtitle {
  font-size: 15px;
  color: #94a3b8;
}

.announcements-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-card {
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.announcement-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.announcement-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.announcement-date {
  font-size: 13px;
  color: #94a3b8;
}

.announcement-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 10px;
}

.announcement-content {
  font-size: 14px;
  color: #64748b;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
