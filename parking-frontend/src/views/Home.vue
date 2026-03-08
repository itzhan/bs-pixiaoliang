<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NCard,
  NIcon,
  NProgress,
  NSpin,
  NTag,
  NEmpty,
} from 'naive-ui'
import {
  CalendarOutline,
  FlashOutline,
  CardOutline,
  EyeOutline,
  ArrowForwardOutline,
} from '@vicons/ionicons5'
import { getAreaStats } from '@/api/parking'
import { getPublishedAnnouncements } from '@/api/announcement'
import dayjs from 'dayjs'

const router = useRouter()

/* ---------- Types ---------- */
interface AreaStat {
  id: number
  name: string
  totalSpaces: number
  availableSpaces: number
  occupiedSpaces: number
  occupancyRate: number
}

interface AnnouncementItem {
  id: number
  title: string
  content: string
  announcementType: string
  publishedAt: string
}

/* ---------- State ---------- */
const areaStats = ref<AreaStat[]>([])
const announcements = ref<AnnouncementItem[]>([])
const statsLoading = ref(false)
const announcementsLoading = ref(false)

/* ---------- Features data ---------- */
const features = [
  {
    icon: CalendarOutline,
    title: '在线预约',
    desc: '提前预约车位，到达即停，无需排队等候',
  },
  {
    icon: FlashOutline,
    title: '快速入场',
    desc: '智能识别车牌，自动抬杆放行，秒速通过',
  },
  {
    icon: CardOutline,
    title: '便捷支付',
    desc: '支持多种支付方式，自动计费，轻松结算',
  },
  {
    icon: EyeOutline,
    title: '实时查看',
    desc: '实时查看各区域车位情况，随时掌握停车动态',
  },
]

/* ---------- Helpers ---------- */
function getOccupancyColor(rate: number): string {
  if (rate < 50) return '#52c41a'
  if (rate < 80) return '#faad14'
  return '#ff4d4f'
}

function getAnnouncementTypeTag(type: string) {
  const map: Record<string, { label: string; type: 'info' | 'warning' | 'success' | 'default' }> = {
    NOTICE: { label: '通知', type: 'info' },
    MAINTENANCE: { label: '维护', type: 'warning' },
    PROMOTION: { label: '优惠', type: 'success' },
    OTHER: { label: '其他', type: 'default' },
  }
  return map[type] || map.OTHER
}

function formatDate(date: string) {
  return dayjs(date).format('YYYY-MM-DD')
}

/* ---------- Data loading ---------- */
async function loadAreaStats() {
  statsLoading.value = true
  try {
    const res: any = await getAreaStats()
    areaStats.value = res.data || []
  } catch {
    /* handled by interceptor */
  } finally {
    statsLoading.value = false
  }
}

async function loadAnnouncements() {
  announcementsLoading.value = true
  try {
    const res: any = await getPublishedAnnouncements()
    announcements.value = (res.data || []).slice(0, 3)
  } catch {
    /* handled by interceptor */
  } finally {
    announcementsLoading.value = false
  }
}

onMounted(() => {
  loadAreaStats()
  loadAnnouncements()
})
</script>

<template>
  <!-- ==================== Hero Section ==================== -->
  <section class="hero">
    <div class="hero-bg">
      <div class="hero-shape hero-shape-1" />
      <div class="hero-shape hero-shape-2" />
      <div class="hero-shape hero-shape-3" />
    </div>
    <div class="hero-content">
      <h1 class="hero-title">智能停车 · 轻松出行</h1>
      <p class="hero-subtitle">
        实时查看车位状况，在线预约专属车位，让停车不再烦恼
      </p>
      <n-button
        type="primary"
        size="large"
        round
        @click="router.push('/parking')"
      >
        查看空位
        <template #icon>
          <n-icon><ArrowForwardOutline /></n-icon>
        </template>
      </n-button>
    </div>
  </section>

  <!-- ==================== Stats Section ==================== -->
  <section class="section stats-section">
    <div class="container">
      <h2 class="section-title">车位实况</h2>
      <p class="section-subtitle">各区域实时车位信息，一目了然</p>

      <n-spin :show="statsLoading">
        <div class="stats-grid">
          <n-card
            v-for="stat in areaStats"
            :key="stat.id"
            hoverable
            class="stat-card"
          >
            <div class="stat-header">
              <span class="stat-name">{{ stat.name }}</span>
              <n-tag
                :type="stat.occupancyRate < 50 ? 'success' : stat.occupancyRate < 80 ? 'warning' : 'error'"
                size="small"
                round
              >
                {{ stat.occupancyRate }}%
              </n-tag>
            </div>

            <div class="stat-numbers">
              <div class="stat-item">
                <span class="stat-value available">{{ stat.availableSpaces }}</span>
                <span class="stat-label">空闲</span>
              </div>
              <div class="stat-divider" />
              <div class="stat-item">
                <span class="stat-value occupied">{{ stat.occupiedSpaces }}</span>
                <span class="stat-label">已占</span>
              </div>
              <div class="stat-divider" />
              <div class="stat-item">
                <span class="stat-value total">{{ stat.totalSpaces }}</span>
                <span class="stat-label">总数</span>
              </div>
            </div>

            <n-progress
              type="line"
              :percentage="stat.occupancyRate"
              :color="getOccupancyColor(stat.occupancyRate)"
              :show-indicator="false"
            />
          </n-card>
        </div>
        <n-empty
          v-if="!statsLoading && areaStats.length === 0"
          description="暂无区域数据"
        />
      </n-spin>
    </div>
  </section>

  <!-- ==================== Features Section ==================== -->
  <section class="section features-section">
    <div class="container">
      <h2 class="section-title">服务特色</h2>
      <p class="section-subtitle">智慧停车，全方位服务体验</p>

      <div class="features-grid">
        <div
          v-for="(feature, index) in features"
          :key="index"
          class="feature-card"
        >
          <div class="feature-icon">
            <n-icon size="32" color="#2D6A4F">
              <component :is="feature.icon" />
            </n-icon>
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.desc }}</p>
        </div>
      </div>
    </div>
  </section>

  <!-- ==================== Announcements Section ==================== -->
  <section class="section announcements-home-section">
    <div class="container">
      <div class="section-header">
        <div>
          <h2 class="section-title">最新公告</h2>
          <p class="section-subtitle">了解停车场最新动态</p>
        </div>
        <n-button text type="primary" @click="router.push('/announcements')">
          查看全部
          <template #icon>
            <n-icon><ArrowForwardOutline /></n-icon>
          </template>
        </n-button>
      </div>

      <n-spin :show="announcementsLoading">
        <div class="announcements-grid">
          <n-card
            v-for="item in announcements"
            :key="item.id"
            hoverable
            class="announcement-card"
            @click="router.push('/announcements')"
          >
            <div class="announcement-meta">
              <n-tag
                :type="getAnnouncementTypeTag(item.announcementType).type"
                size="small"
              >
                {{ getAnnouncementTypeTag(item.announcementType).label }}
              </n-tag>
              <span class="announcement-date">{{ formatDate(item.publishedAt) }}</span>
            </div>
            <h3 class="announcement-title">{{ item.title }}</h3>
            <p class="announcement-content">{{ item.content }}</p>
          </n-card>
        </div>
        <n-empty
          v-if="!announcementsLoading && announcements.length === 0"
          description="暂无公告"
        />
      </n-spin>
    </div>
  </section>
</template>

<style scoped>
/* ========== Hero ========== */
.hero {
  position: relative;
  min-height: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, #f0f7f4 0%, #e8f0f6 50%, #f5f0e8 100%);
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
  width: 420px;
  height: 420px;
  background: #2d6a4f;
  top: -120px;
  right: -100px;
}

.hero-shape-2 {
  width: 320px;
  height: 320px;
  background: #e07a5f;
  bottom: -100px;
  left: -80px;
}

.hero-shape-3 {
  width: 200px;
  height: 200px;
  background: #2d6a4f;
  top: 55%;
  right: 18%;
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 60px 24px;
}

.hero-title {
  font-size: 44px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 16px;
  letter-spacing: 2px;
}

.hero-subtitle {
  font-size: 18px;
  color: #64748b;
  margin-bottom: 40px;
  max-width: 520px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.7;
}

/* ========== Sections ========== */
.section {
  padding: 72px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.section-title {
  font-size: 28px;
  font-weight: 600;
  color: #1e293b;
  text-align: center;
  margin-bottom: 8px;
}

.section-subtitle {
  font-size: 15px;
  color: #94a3b8;
  text-align: center;
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 32px;
}

.section-header .section-title,
.section-header .section-subtitle {
  text-align: left;
  margin-bottom: 0;
}

.section-header .section-subtitle {
  margin-top: 4px;
}

/* ========== Stats ========== */
.stats-section {
  background: #fff;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.stat-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.stat-numbers {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
}

.stat-value.available {
  color: #2d6a4f;
}

.stat-value.occupied {
  color: #e07a5f;
}

.stat-value.total {
  color: #64748b;
}

.stat-label {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: #e8e8e8;
}

/* ========== Features ========== */
.features-section {
  background: #f8f9fa;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.feature-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: rgba(45, 106, 79, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.feature-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.feature-desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
}

/* ========== Announcements ========== */
.announcements-home-section {
  background: #fff;
}

.announcements-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.announcement-card {
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.announcement-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.announcement-meta {
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
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement-content {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ========== Responsive ========== */
@media (max-width: 900px) {
  .stats-grid,
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .announcements-grid {
    grid-template-columns: 1fr;
  }
  .hero-title {
    font-size: 32px;
  }
}

@media (max-width: 540px) {
  .stats-grid,
  .features-grid {
    grid-template-columns: 1fr;
  }
  .hero-title {
    font-size: 26px;
  }
  .hero-subtitle {
    font-size: 15px;
  }
}
</style>
