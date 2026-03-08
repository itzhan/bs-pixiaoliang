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
} from '@ant-design/icons-vue'
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

/* ──────────────────── Helpers ──────────────────── */

function formatMoney(val: number): string {
  if (val >= 10000) {
    return `¥${(val / 10000).toFixed(2)}万`
  }
  return `¥${val.toFixed(2)}`
}

function formatNumber(val: number): string {
  return val.toLocaleString()
}

function formatHour(hour: number): string {
  return `${hour.toString().padStart(2, '0')}:00`
}

/* ──────────────────── Stat cards config ──────────────────── */

const row1Cards = computed(() => [
  {
    title: '总用户数',
    value: formatNumber(dashboard.value.totalUsers),
    icon: TeamOutlined,
    color: '#1677ff',
    bg: 'rgba(22, 119, 255, 0.08)',
  },
  {
    title: '总车位 / 可用车位',
    value: `${formatNumber(dashboard.value.totalSpaces)} / ${formatNumber(dashboard.value.availableSpaces)}`,
    icon: CarOutlined,
    color: '#52c41a',
    bg: 'rgba(82, 196, 26, 0.08)',
  },
  {
    title: '今日订单',
    value: formatNumber(dashboard.value.todayOrders),
    icon: FileTextOutlined,
    color: '#fa8c16',
    bg: 'rgba(250, 140, 22, 0.08)',
  },
  {
    title: '今日收入',
    value: formatMoney(dashboard.value.todayRevenue),
    icon: MoneyCollectOutlined,
    color: '#f5222d',
    bg: 'rgba(245, 34, 45, 0.08)',
  },
])

const row2Cards = computed(() => [
  {
    title: '占用率',
    value: `${dashboard.value.occupancyRate.toFixed(1)}%`,
    icon: PieChartOutlined,
    color: '#722ed1',
    bg: 'rgba(114, 46, 209, 0.08)',
  },
  {
    title: '本月收入',
    value: formatMoney(dashboard.value.monthRevenue),
    icon: RiseOutlined,
    color: '#52c41a',
    bg: 'rgba(82, 196, 26, 0.08)',
  },
  {
    title: '总收入',
    value: formatMoney(dashboard.value.totalRevenue),
    icon: DollarOutlined,
    color: '#1677ff',
    bg: 'rgba(22, 119, 255, 0.08)',
  },
  {
    title: '活跃预约',
    value: formatNumber(dashboard.value.activeReservations),
    icon: ClockCircleOutlined,
    color: '#13c2c2',
    bg: 'rgba(19, 194, 194, 0.08)',
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
    <div class="page-header">
      <h2>数据概览</h2>
    </div>

    <a-spin :spinning="loading">
      <!-- ──────── Row 1: Stat cards ──────── -->
      <a-row :gutter="16" class="stat-row">
        <a-col :span="6" v-for="card in row1Cards" :key="card.title">
          <div class="stat-card">
            <div class="stat-card-icon" :style="{ background: card.bg }">
              <component :is="card.icon" :style="{ fontSize: '28px', color: card.color }" />
            </div>
            <div class="stat-card-info">
              <div class="stat-card-title">{{ card.title }}</div>
              <div class="stat-card-value" :style="{ color: card.color }">{{ card.value }}</div>
            </div>
          </div>
        </a-col>
      </a-row>

      <!-- ──────── Row 2: Stat cards ──────── -->
      <a-row :gutter="16" class="stat-row">
        <a-col :span="6" v-for="card in row2Cards" :key="card.title">
          <div class="stat-card">
            <div class="stat-card-icon" :style="{ background: card.bg }">
              <component :is="card.icon" :style="{ fontSize: '28px', color: card.color }" />
            </div>
            <div class="stat-card-info">
              <div class="stat-card-title">{{ card.title }}</div>
              <div class="stat-card-value" :style="{ color: card.color }">{{ card.value }}</div>
            </div>
          </div>
        </a-col>
      </a-row>

      <!-- ──────── Row 3: Charts ──────── -->
      <a-row :gutter="16" class="stat-row">
        <!-- Revenue Trend -->
        <a-col :span="16">
          <a-card title="近7日营收趋势" :bordered="false" class="chart-card">
            <div class="bar-chart" v-if="revenueTrend.length">
              <div
                class="bar-item"
                v-for="item in revenueTrend"
                :key="item.date"
              >
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

        <!-- Hourly Stats -->
        <a-col :span="8">
          <a-card title="24小时车流分布" :bordered="false" class="chart-card">
            <div class="bar-chart compact" v-if="hourlyStats.length">
              <div
                class="bar-item"
                v-for="item in hourlyStats"
                :key="item.hour"
              >
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

.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.88);
}

/* ──── Stat Cards ──── */
.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.25s, transform 0.25s;
  cursor: default;
}
.stat-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stat-card-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 14px;
}

.stat-card-info {
  min-width: 0;
}

.stat-card-title {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.45);
  margin-bottom: 6px;
  white-space: nowrap;
}

.stat-card-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ──── Chart Cards ──── */
.chart-card {
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  min-height: 320px;
}

/* ──── Bar Chart ──── */
.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bar-chart.compact {
  gap: 6px;
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
  height: 22px;
  background: #f5f5f5;
  border-radius: 6px;
  overflow: hidden;
}

.compact .bar-track {
  height: 16px;
}

.bar-fill {
  height: 100%;
  border-radius: 6px;
  min-width: 4px;
  transition: width 0.6s cubic-bezier(0.23, 1, 0.32, 1);
}

.revenue-bar {
  background: linear-gradient(90deg, #4f8ef7 0%, #7c5cf5 100%);
}

.hourly-bar {
  background: linear-gradient(90deg, #13c2c2 0%, #36cfc9 100%);
}

.bar-value {
  width: 64px;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.65);
  text-align: right;
}

.compact .bar-value {
  width: 36px;
  font-size: 11px;
}

/* ──── Responsive ──── */
@media (max-width: 1200px) {
  .stat-card-value {
    font-size: 20px;
  }
}
</style>
