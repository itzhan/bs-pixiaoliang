<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
  TeamOutlined,
  CarOutlined,
  FileTextOutlined,
  MoneyCollectOutlined,
  PieChartOutlined,
  RiseOutlined,
  DollarOutlined,
  ClockCircleOutlined,
  DashboardOutlined,
  CalendarOutlined,
  ThunderboltOutlined,
  WarningOutlined,
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { getDashboard, getRevenueTrend, getHourlyStats } from '@/api/statistics'

/* ──────────────────── Types ──────────────────── */

interface DashboardData {
  totalUsers: number
  totalSpaces: number
  availableSpaces: number
  todayOrders: number
  todayRevenue: number
  occupancyRate: number
  monthRevenue: number
  totalRevenue: number
  activeReservations: number
}

interface RevenueTrendItem {
  date: string
  revenue: number
}

interface HourlyStatsItem {
  hour: number
  count: number
}

/* ──────────────────── State ──────────────────── */

const loading = ref(true)
const userStore = useUserStore()

const dashboard = ref<DashboardData>({
  totalUsers: 0,
  totalSpaces: 0,
  availableSpaces: 0,
  todayOrders: 0,
  todayRevenue: 0,
  occupancyRate: 0,
  monthRevenue: 0,
  totalRevenue: 0,
  activeReservations: 0,
})

const revenueTrend = ref<RevenueTrendItem[]>([])
const hourlyStats = ref<HourlyStatsItem[]>([])

/* ──────────────────── Computed ──────────────────── */

const revenueMax = computed(() => {
  if (!revenueTrend.value.length) return 1
  return Math.max(...revenueTrend.value.map((i) => i.revenue), 1)
})

const hourlyMax = computed(() => {
  if (!hourlyStats.value.length) return 1
  return Math.max(...hourlyStats.value.map((i) => i.count), 1)
})

const occupiedSpaces = computed(() =>
  dashboard.value.totalSpaces - dashboard.value.availableSpaces,
)

const occupancyDeg = computed(() =>
  (dashboard.value.occupancyRate / 100) * 360,
)

const occupancyColor = computed(() => {
  const rate = dashboard.value.occupancyRate
  if (rate < 50) return '#52c41a'
  if (rate < 80) return '#faad14'
  return '#ff4d4f'
})

const currentGreeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const currentDateStr = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
})

/* ──────────────────── Helpers ──────────────────── */

function formatMoney(val: number): string {
  if (val >= 10000) return `¥${(val / 10000).toFixed(2)}万`
  return `¥${val.toFixed(2)}`
}

function formatNumber(val: number): string {
  return val.toLocaleString()
}

function formatHour(hour: number): string {
  return `${hour.toString().padStart(2, '0')}:00`
}

/* ──────────────────── Stat cards config ──────────────────── */

const statCards = computed(() => [
  {
    title: '总用户数',
    value: formatNumber(dashboard.value.totalUsers),
    icon: TeamOutlined,
    color: '#3b82f6',
    gradient: 'linear-gradient(135deg, #3b82f6 0%, #2dd4bf 100%)',
  },
  {
    title: '总车位',
    value: formatNumber(dashboard.value.totalSpaces),
    sub: `可用 ${formatNumber(dashboard.value.availableSpaces)}`,
    icon: CarOutlined,
    color: '#10b981',
    gradient: 'linear-gradient(135deg, #10b981 0%, #34d399 100%)',
  },
  {
    title: '今日订单',
    value: formatNumber(dashboard.value.todayOrders),
    icon: FileTextOutlined,
    color: '#f59e0b',
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%)',
  },
  {
    title: '今日收入',
    value: formatMoney(dashboard.value.todayRevenue),
    icon: MoneyCollectOutlined,
    color: '#ef4444',
    gradient: 'linear-gradient(135deg, #ef4444 0%, #f97316 100%)',
  },
  {
    title: '本月收入',
    value: formatMoney(dashboard.value.monthRevenue),
    icon: RiseOutlined,
    color: '#8b5cf6',
    gradient: 'linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%)',
  },
  {
    title: '总收入',
    value: formatMoney(dashboard.value.totalRevenue),
    icon: DollarOutlined,
    color: '#06b6d4',
    gradient: 'linear-gradient(135deg, #06b6d4 0%, #22d3ee 100%)',
  },
  {
    title: '活跃预约',
    value: formatNumber(dashboard.value.activeReservations),
    icon: ClockCircleOutlined,
    color: '#14b8a6',
    gradient: 'linear-gradient(135deg, #14b8a6 0%, #2dd4bf 100%)',
  },
  {
    title: '占用率',
    value: `${dashboard.value.occupancyRate.toFixed(1)}%`,
    icon: PieChartOutlined,
    color: occupancyColor.value,
    gradient: `linear-gradient(135deg, ${occupancyColor.value} 0%, ${occupancyColor.value}88 100%)`,
  },
])

/* ──────────────────── Fetch ──────────────────── */

async function fetchData() {
  loading.value = true
  try {
    const [dashRes, trendRes, hourlyRes] = await Promise.all([
      getDashboard(),
      getRevenueTrend(7),
      getHourlyStats(),
    ])
    dashboard.value = (dashRes as any).data
    revenueTrend.value = (trendRes as any).data || []
    hourlyStats.value = (hourlyRes as any).data || []
  } catch {
    // errors handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="dashboard-page">
    <a-spin :spinning="loading">
      <!-- ──────── Welcome Banner ──────── -->
      <div class="welcome-banner">
        <div class="welcome-left">
          <h2 class="welcome-title">
            {{ currentGreeting }}，{{ userStore.realName || userStore.username || '管理员' }}
          </h2>
          <p class="welcome-desc">
            <CalendarOutlined style="margin-right: 6px" />
            {{ currentDateStr }} · 智能停车场管理系统运行中
          </p>
        </div>
        <div class="welcome-right">
          <div class="quick-stat">
            <DashboardOutlined style="font-size: 18px; color: #3b82f6" />
            <div>
              <div class="quick-stat-value">{{ dashboard.totalSpaces }}</div>
              <div class="quick-stat-label">总车位</div>
            </div>
          </div>
          <div class="quick-stat">
            <ThunderboltOutlined style="font-size: 18px; color: #10b981" />
            <div>
              <div class="quick-stat-value">{{ dashboard.availableSpaces }}</div>
              <div class="quick-stat-label">空闲车位</div>
            </div>
          </div>
          <div class="quick-stat">
            <WarningOutlined style="font-size: 18px; color: #f59e0b" />
            <div>
              <div class="quick-stat-value">{{ dashboard.activeReservations }}</div>
              <div class="quick-stat-label">活跃预约</div>
            </div>
          </div>
        </div>
      </div>

      <!-- ──────── Stat Cards Grid ──────── -->
      <div class="stat-grid">
        <div class="stat-card" v-for="card in statCards" :key="card.title">
          <div class="stat-card-icon" :style="{ background: card.gradient }">
            <component :is="card.icon" style="font-size: 22px; color: #fff" />
          </div>
          <div class="stat-card-info">
            <div class="stat-card-title">{{ card.title }}</div>
            <div class="stat-card-value" :style="{ color: card.color }">{{ card.value }}</div>
            <div v-if="card.sub" class="stat-card-sub">{{ card.sub }}</div>
          </div>
        </div>
      </div>

      <!-- ──────── Middle Row: Charts + Occupancy ──────── -->
      <a-row :gutter="16" class="chart-row">
        <!-- Revenue Trend -->
        <a-col :span="11">
          <a-card title="近7日营收趋势" :bordered="false" class="chart-card">
            <template #extra>
              <RiseOutlined style="color: #8b5cf6" />
            </template>
            <div class="bar-chart" v-if="revenueTrend.length">
              <div class="bar-item" v-for="item in revenueTrend" :key="item.date">
                <div class="bar-label">{{ item.date.slice(5) }}</div>
                <div class="bar-track">
                  <div
                    class="bar-fill revenue-bar"
                    :style="{ width: (item.revenue / revenueMax) * 100 + '%' }"
                  ></div>
                </div>
                <div class="bar-value">¥{{ item.revenue.toFixed(0) }}</div>
              </div>
            </div>
            <a-empty v-else description="暂无数据" />
          </a-card>
        </a-col>

        <!-- Occupancy Ring -->
        <a-col :span="5">
          <a-card title="车位占用率" :bordered="false" class="chart-card occupancy-card">
            <div class="occupancy-ring-wrapper">
              <div
                class="occupancy-ring"
                :style="{
                  background: `conic-gradient(${occupancyColor} 0deg, ${occupancyColor} ${occupancyDeg}deg, #f0f0f0 ${occupancyDeg}deg, #f0f0f0 360deg)`,
                }"
              >
                <div class="occupancy-inner">
                  <div class="occupancy-percent" :style="{ color: occupancyColor }">
                    {{ dashboard.occupancyRate.toFixed(1) }}%
                  </div>
                  <div class="occupancy-label">占用率</div>
                </div>
              </div>
              <div class="occupancy-stats">
                <div class="occupancy-stat-item">
                  <span class="dot" style="background: #52c41a"></span>
                  <span>空闲 {{ dashboard.availableSpaces }}</span>
                </div>
                <div class="occupancy-stat-item">
                  <span class="dot" :style="{ background: occupancyColor }"></span>
                  <span>占用 {{ occupiedSpaces }}</span>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- Hourly Stats -->
        <a-col :span="8">
          <a-card title="24小时车流分布" :bordered="false" class="chart-card">
            <template #extra>
              <ClockCircleOutlined style="color: #14b8a6" />
            </template>
            <div class="bar-chart compact" v-if="hourlyStats.length">
              <div class="bar-item" v-for="item in hourlyStats" :key="item.hour">
                <div class="bar-label">{{ formatHour(item.hour) }}</div>
                <div class="bar-track">
                  <div
                    class="bar-fill hourly-bar"
                    :style="{ width: (item.count / hourlyMax) * 100 + '%' }"
                  ></div>
                </div>
                <div class="bar-value">{{ item.count }}</div>
              </div>
            </div>
            <a-empty v-else description="暂无数据" />
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<style scoped>
.dashboard-page {
  padding: 0;
}

/* ──── Welcome Banner ──── */
.welcome-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a8c 50%, #1a365d 100%);
  border-radius: 14px;
  margin-bottom: 20px;
  color: #fff;
  box-shadow: 0 4px 20px rgba(30, 58, 95, 0.25);
}

.welcome-title {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.welcome-desc {
  margin: 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.welcome-right {
  display: flex;
  gap: 32px;
}

.quick-stat {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quick-stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}

.quick-stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

/* ──── Stat Cards Grid ──── */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  transition: all 0.25s;
  cursor: default;
}

.stat-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stat-card-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
}

.stat-card-info {
  min-width: 0;
}

.stat-card-title {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 4px;
  white-space: nowrap;
}

.stat-card-value {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-card-sub {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
}

/* ──── Chart Cards ──── */
.chart-row {
  margin-bottom: 16px;
}

.chart-card {
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  min-height: 380px;
}

.occupancy-card {
  min-height: 380px;
}

/* ──── Bar Chart ──── */
.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bar-chart.compact {
  gap: 5px;
}

.bar-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bar-label {
  width: 52px;
  flex-shrink: 0;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.55);
  text-align: right;
}

.compact .bar-label {
  width: 42px;
  font-size: 11px;
}

.bar-track {
  flex: 1;
  height: 20px;
  background: #f5f5f5;
  border-radius: 6px;
  overflow: hidden;
}

.compact .bar-track {
  height: 14px;
}

.bar-fill {
  height: 100%;
  border-radius: 6px;
  min-width: 4px;
  transition: width 0.8s cubic-bezier(0.23, 1, 0.32, 1);
}

.revenue-bar {
  background: linear-gradient(90deg, #8b5cf6 0%, #a78bfa 100%);
}

.hourly-bar {
  background: linear-gradient(90deg, #14b8a6 0%, #2dd4bf 100%);
}

.bar-value {
  width: 60px;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.65);
  text-align: right;
}

.compact .bar-value {
  width: 32px;
  font-size: 11px;
}

/* ──── Occupancy Ring ──── */
.occupancy-ring-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 20px;
}

.occupancy-ring {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.6s;
}

.occupancy-inner {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.occupancy-percent {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.occupancy-label {
  font-size: 12px;
  color: #94a3b8;
}

.occupancy-stats {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.occupancy-stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

/* ──── Responsive ──── */
@media (max-width: 1400px) {
  .stat-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1200px) {
  .stat-card-value {
    font-size: 18px;
  }
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
